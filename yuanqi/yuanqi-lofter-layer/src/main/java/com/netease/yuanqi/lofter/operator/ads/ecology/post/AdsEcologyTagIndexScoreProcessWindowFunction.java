package com.netease.yuanqi.lofter.operator.ads.ecology.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.lofter.pojo.ads.ecology.post.TagIndexScore;
import com.netease.yuanqi.lofter.pojo.ads.ecology.post.TagPostUserHotEvent;
import java.util.Iterator;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 离线 SQL ads_ecology_tag_index_score_di 实时化的第二步：10min 滚动窗口按 (tag, postId, blogId) 做 sum 聚合， 直接输出
 * JSON String 写 Kafka，对应离线 SQL 最外层 group by：
 *
 * <pre>
 * select tag, postId, blogId,
 *        sum(newUserPostHot) as newHot,
 *        sum(oldUserPostHot) as oldPostHot,
 *        sum(newUserPostScore) as newScore
 * from postUserHotSource lateral view explode(tags) ... left join tagUserShipSource
 * group by tag, postId, blogId
 * </pre>
 *
 * <p>上游 FlatMap 每条事件仅 1 个 cnt 为 1，其余为 0。窗口内对 6 个 cnt 各自 sum，得到该 (tag, postId, blogId) 在 10min 内的
 * like/reproduce/recommend/collect/comment/underscoreCircle 累计次数；同时由于 newScore 需对每条事件按 shipNum 单独算
 * （同 (tag, postId, blogId) 下不同 userId 的 shipNum 不同），故在窗口循环里逐条累加 newUserPostScore.
 */
public class AdsEcologyTagIndexScoreProcessWindowFunction
        extends ProcessWindowFunction<TagPostUserHotEvent, String, String, TimeWindow> {
    private static final Logger LOG =
            LoggerFactory.getLogger(AdsEcologyTagIndexScoreProcessWindowFunction.class);

    // newUserPostHot 公式系数: like*0.2 + collect*5 + reproduce*5 + recommend*2 + comment*2 +
    // underscoreCircle*0.5
    private static final double WEIGHT_LIKE = 0.2;
    private static final double WEIGHT_COLLECT = 5.0;
    private static final double WEIGHT_REPRODUCE = 5.0;
    private static final double WEIGHT_RECOMMEND = 2.0;
    private static final double WEIGHT_COMMENT = 2.0;
    private static final double WEIGHT_UNDERSCORE_CIRCLE_COMMENT = 0.5;

    /** TagIndexScore.type: 增量(实时窗口聚合)输出值；存量(离线回填)用 0. */
    private static final int TYPE_INCREMENTAL = 1;

    private transient ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void process(
            String key,
            ProcessWindowFunction<TagPostUserHotEvent, String, String, TimeWindow>.Context context,
            Iterable<TagPostUserHotEvent> elements,
            Collector<String> out)
            throws Exception {
        Iterator<TagPostUserHotEvent> iter = elements.iterator();
        if (!iter.hasNext()) {
            return;
        }

        // 首元素用于取 (tag, postId, blogId) 维度（同 key 下三者必然相同）
        TagPostUserHotEvent first = iter.next();
        String tag = first.getTag();
        Long postId = first.getPostId();
        Long blogId = first.getBlogId();

        long likeSum = 0L;
        long reproduceSum = 0L;
        long recommendSum = 0L;
        long collectSum = 0L;
        long commentSum = 0L;
        long underscoreCircleSum = 0L;
        double newScoreSum = 0.0;

        TagPostUserHotEvent e = first;
        while (true) {
            long like = nullSafe(e.getLikeCnt());
            long reproduce = nullSafe(e.getReproduceCnt());
            long recommend = nullSafe(e.getRecommendCnt());
            long collect = nullSafe(e.getCollectCnt());
            long comment = nullSafe(e.getCommentCnt());
            long underscoreCircle = nullSafe(e.getUnderscoreCircleCommentCnt());
            likeSum += like;
            reproduceSum += reproduce;
            recommendSum += recommend;
            collectSum += collect;
            commentSum += comment;
            underscoreCircleSum += underscoreCircle;

            // newScore 必须逐事件按 shipNum 单独算再 sum (shipNum 与 userId 绑定, 同 key 下 userId 不同则 shipNum 不同)
            double eventNewPostHot =
                    computeNewHot(like, reproduce, recommend, collect, comment, underscoreCircle);
            long shipNum = nullSafe(e.getShipNum());
            newScoreSum += eventNewPostHot * (1.0 + Math.log(1.0 + shipNum));

            if (!iter.hasNext()) {
                break;
            }
            e = iter.next();
        }

        // sum(newUserPostHot) 等价于把 6 个 cnt sum 后再代入公式（线性叠加）
        double newHot =
                computeNewHot(
                        likeSum,
                        reproduceSum,
                        recommendSum,
                        collectSum,
                        commentSum,
                        underscoreCircleSum);
        // sum(oldUserPostHot) = sum(like + collect + reproduce + recommend)
        long oldPostHot = likeSum + collectSum + reproduceSum + recommendSum;

        TagIndexScore result = new TagIndexScore();
        result.setTag(tag);
        result.setPostId(postId);
        result.setBlogId(blogId);
        result.setNewHot(newHot);
        result.setOldPostHot(oldPostHot);
        result.setNewScore(newScoreSum);
        result.setType(TYPE_INCREMENTAL);

        out.collect(objectMapper.writeValueAsString(result));
    }

    private static double computeNewHot(
            long like,
            long reproduce,
            long recommend,
            long collect,
            long comment,
            long underscoreCircle) {
        return like * WEIGHT_LIKE
                + collect * WEIGHT_COLLECT
                + reproduce * WEIGHT_REPRODUCE
                + recommend * WEIGHT_RECOMMEND
                + comment * WEIGHT_COMMENT
                + underscoreCircle * WEIGHT_UNDERSCORE_CIRCLE_COMMENT;
    }

    private static long nullSafe(Long v) {
        return v == null ? 0L : v;
    }
}
