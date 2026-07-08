package com.netease.yuanqi.lofter.operator.ads.ecology.post;

import com.netease.yuanqi.lofter.pojo.ads.ecology.post.ActJoinTagPost;
import com.netease.yuanqi.lofter.pojo.ads.ecology.post.ActJoinTagPostHot;
import com.netease.yuanqi.lofter.pojo.ads.ecology.post.PostHot;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.mortbay.log.Log;

public class ActJoinTagPostBroadcastProcessFunction
        extends BroadcastProcessFunction<PostHot, ActJoinTagPost, ActJoinTagPostHot> {
    private static final MapStateDescriptor<Long, ActJoinTagPost> ACT_TAG_JOIN_POST_BROADCAST_DESC =
            new MapStateDescriptor<>(
                    "ActTagJoinPost",
                    BasicTypeInfo.LONG_TYPE_INFO,
                    TypeInformation.of(new TypeHint<ActJoinTagPost>() {}));

    @Override
    public void processElement(
            PostHot postHot,
            BroadcastProcessFunction<PostHot, ActJoinTagPost, ActJoinTagPostHot>.ReadOnlyContext
                    readOnlyContext,
            Collector<ActJoinTagPostHot> collector)
            throws Exception {
        // 热度事件驱动
        ReadOnlyBroadcastState<Long, ActJoinTagPost> readOnlyBroadcastState =
                readOnlyContext.getBroadcastState(ACT_TAG_JOIN_POST_BROADCAST_DESC);
        if (readOnlyBroadcastState.get(postHot.getPostId()) != null) {
            ActJoinTagPost actJoinTagPost = readOnlyBroadcastState.get(postHot.getPostId());
            ActJoinTagPostHot actJoinTagPostHot = new ActJoinTagPostHot();
            actJoinTagPostHot.setTag("");
            actJoinTagPostHot.setPostId(actJoinTagPost.getPostId());
            actJoinTagPostHot.setBlogId(actJoinTagPost.getBlogId());
            actJoinTagPostHot.setPostStatus(actJoinTagPost.getStatus());
            actJoinTagPostHot.setPostModifyTime(actJoinTagPost.getModifyTime());
            actJoinTagPostHot.setHot(postHot.getStatus() == -1 ? -1L : 1L);
            actJoinTagPost
                    .getActTaskIds()
                    .forEach(
                            actTaskId -> {
                                actJoinTagPostHot.setActTaskId(actTaskId); // 同一篇文章参加两个活动
                                collector.collect(actJoinTagPostHot);
                            });
        }
    }

    @Override
    public void processBroadcastElement(
            ActJoinTagPost actJoinTagPost,
            BroadcastProcessFunction<PostHot, ActJoinTagPost, ActJoinTagPostHot>.Context context,
            Collector<ActJoinTagPostHot> collector)
            throws Exception {
        BroadcastState<Long, ActJoinTagPost> actJoinTagPostBroadcastState =
                context.getBroadcastState(ACT_TAG_JOIN_POST_BROADCAST_DESC);

        if (actJoinTagPost.getStatus() == -2) {
            List<Long> deleteKey = new ArrayList<>(100);
            Iterator<Map.Entry<Long, ActJoinTagPost>> currentState =
                    actJoinTagPostBroadcastState.iterator();
            while (currentState.hasNext()) {
                Map.Entry<Long, ActJoinTagPost> entry = currentState.next();
                Long key = entry.getKey();
                ActJoinTagPost value = entry.getValue();
                for (Long actTaskId : actJoinTagPost.getActTaskIds()) {
                    if (value.getActTaskIds().contains(actTaskId)) {
                        deleteKey.add(key);
                    }
                }
            }
            for (Long postId : deleteKey) {
                ActJoinTagPost state = actJoinTagPostBroadcastState.get(postId);
                if (state != null) {
                    for (Long actTaskId : actJoinTagPost.getActTaskIds()) {
                        if (state.getActTaskIds().contains(actTaskId)) {
                            state.getActTaskIds().remove(actTaskId);
                            if (state.getActTaskIds().isEmpty()) {
                                actJoinTagPostBroadcastState.remove(postId);
                            }
                        }
                    }
                }
            }
            Log.info(
                    "===========清除过期KeySet===========: {}",
                    actJoinTagPost.getActTaskIds().toString());
        } else {
            actJoinTagPostBroadcastState.put(actJoinTagPost.getPostId(), actJoinTagPost);

            // 非热度事件驱动
            if (actJoinTagPost.getStatus() == -1) {
                ActJoinTagPostHot actJoinTagPostHot = new ActJoinTagPostHot();
                actJoinTagPostHot.setTag("");
                actJoinTagPostHot.setPostId(actJoinTagPost.getPostId());
                actJoinTagPostHot.setBlogId(actJoinTagPost.getBlogId());
                actJoinTagPostHot.setPostStatus(actJoinTagPost.getStatus());
                actJoinTagPostHot.setPostModifyTime(actJoinTagPost.getModifyTime());
                actJoinTagPostHot.setHot(0L);
                actJoinTagPost
                        .getActTaskIds()
                        .forEach(
                                actTaskId -> {
                                    actJoinTagPostHot.setActTaskId(actTaskId); // 同一篇文章参加两个活动
                                    collector.collect(actJoinTagPostHot);
                                });
            }
        }
    }
}
