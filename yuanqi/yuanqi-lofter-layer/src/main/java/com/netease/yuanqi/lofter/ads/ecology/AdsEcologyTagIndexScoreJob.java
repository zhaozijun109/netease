package com.netease.yuanqi.lofter.ads.ecology;

import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.pojo.config.KafkaConfig;
import com.netease.yuanqi.common.sink.kafka.KafkaBaseSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import com.netease.yuanqi.lofter.operator.ads.ecology.post.AdsEcologyTagIndexScoreProcessWindowFunction;
import com.netease.yuanqi.lofter.operator.ads.ecology.post.AdsEcologyTagIndexScoreRichFlatMapFunction;
import com.netease.yuanqi.lofter.pojo.ads.ecology.post.TagPostUserHotEvent;
import java.time.Duration;
import java.util.Properties;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 离线 SQL ads_ecology_tag_index_score_di 的实时实现，分两步：
 *
 * <ol>
 *   <li>{@link AdsEcologyTagIndexScoreRichFlatMapFunction} —— 解析 binlog，通过 postCache / shipNumCache
 *       与 {@link com.netease.yuanqi.lofter.config.AccompanyTagConfig#ACCOMPANY_TAG_SET} 白名单过滤 + 组装，
 *       输出单事件粒度的 TagPostUserHotEvent（6 个 cnt 中只有一个为 1）。
 *   <li>{@link AdsEcologyTagIndexScoreProcessWindowFunction} —— 按 (tag, postId, blogId) keyBy 后做
 *       10min 滚动窗口 sum 各 cnt 并按公式计算 newPostHot/oldPostHot/newUserScore，输出 JSON 字符串写到后端 Kafka 集群
 *       topic.
 * </ol>
 */
public class AdsEcologyTagIndexScoreJob {
    private static final Logger LOG = LoggerFactory.getLogger(AdsEcologyTagIndexScoreJob.class);

    private static void adsEcologyTagIndexScoreJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        Properties properties = new Properties();
        properties.setProperty("partition.discovery.interval.ms", "60000");

        DataStream<String> binlogStream =
                env.fromSource(
                                new KafkaCommonSource(
                                                "lofter.binlog.online",
                                                "AdsEcologyTagIndexScoreJob",
                                                properties)
                                        .createLogSource(),
                                WatermarkStrategy.noWatermarks(),
                                "NdcBinlogSource")
                        .setParallelism(8)
                        .uid("NdcBinlogKafkaSource")
                        .name("NdcBinlogKafkaSource");

        // 第一步：解析 binlog + 白名单过滤 + 通过 caffeine 缓存补维度 → 单事件 TagPostUserHotEvent
        DataStream<TagPostUserHotEvent> filteredStream =
                binlogStream
                        .flatMap(new AdsEcologyTagIndexScoreRichFlatMapFunction())
                        .setParallelism(8)
                        .name("AdsEcologyTagIndexScoreRichFlatMap")
                        .uid("AdsEcologyTagIndexScoreRichFlatMap");

        // 第二步：按 (tag, postId, blogId) keyBy → 10min 滚动窗口 sum 6 个 cnt + 算公式 → JSON 字符串
        filteredStream
                .keyBy(
                        new KeySelector<TagPostUserHotEvent, String>() {
                            @Override
                            public String getKey(TagPostUserHotEvent tagPostUserHotEvent)
                                    throws Exception {
                                return tagPostUserHotEvent.getTag()
                                        + "_"
                                        + tagPostUserHotEvent.getPostId()
                                        + "_"
                                        + tagPostUserHotEvent.getBlogId();
                            }
                        })
                .window(TumblingProcessingTimeWindows.of(Duration.ofMinutes(15)))
                .process(new AdsEcologyTagIndexScoreProcessWindowFunction())
                .setParallelism(8)
                .name("AdsEcologyTagIndexScoreProcessWindow")
                .uid("AdsEcologyTagIndexScoreProcessWindow")
                .sinkTo(
                        new KafkaBaseSink(
                                        KafkaConfig.builder()
                                                .setBootstrapServers(
                                                        ClusterConfigOptions
                                                                .getKafkaBootStrapServers(
                                                                        ClusterConfigOptions
                                                                                .KafkaBootstrapServersEnum
                                                                                .LOFTER_BACKEND))
                                                .setTopic("lofter.tag.index.score")
                                                .setProperties(properties)
                                                .build())
                                .createLogSink())
                .setParallelism(8)
                .name("AdsEcologyTagIndexScoreKafkaSink")
                .uid("AdsEcologyTagIndexScoreKafkaSink");

        env.execute("AdsEcologyTagIndexScoreJob");
    }

    public static void main(String[] args) throws Exception {
        adsEcologyTagIndexScoreJob(ParameterTool.fromArgs(args));
    }
}
