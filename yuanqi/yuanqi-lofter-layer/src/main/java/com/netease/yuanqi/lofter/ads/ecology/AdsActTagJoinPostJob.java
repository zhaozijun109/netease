package com.netease.yuanqi.lofter.ads.ecology;

import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.pojo.config.KafkaConfig;
import com.netease.yuanqi.common.sink.kafka.KafkaBaseSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import com.netease.yuanqi.lofter.operator.ads.ecology.post.ActJoinTagPostBroadcastProcessFunction;
import com.netease.yuanqi.lofter.operator.ads.ecology.post.AdsActJoinTagPostHotReduceFunction;
import com.netease.yuanqi.lofter.operator.ads.ecology.post.AdsActJoinTagPostProcessWindowFunction;
import com.netease.yuanqi.lofter.operator.ads.ecology.post.AdsActJoinTagPostUserStatisticsRichFlatMapFunction;
import com.netease.yuanqi.lofter.operator.ads.ecology.post.AdsActTagJoinPostHotRichFlatMapFunction;
import com.netease.yuanqi.lofter.operator.ads.ecology.post.AdsActTagJoinPostRichFlatMapFunction;
import com.netease.yuanqi.lofter.pojo.ads.ecology.post.ActJoinTagPost;
import com.netease.yuanqi.lofter.pojo.ads.ecology.post.ActJoinTagPostHot;
import com.netease.yuanqi.lofter.pojo.ads.ecology.post.PostHot;
import java.time.Duration;
import java.util.Properties;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdsActTagJoinPostJob {
    private static final Logger LOG = LoggerFactory.getLogger(AdsActTagJoinPostJob.class);
    private static final MapStateDescriptor<Long, ActJoinTagPost> ACT_TAG_JOIN_POST_BROADCAST_DESC =
            new MapStateDescriptor<>(
                    "ActTagJoinPost",
                    BasicTypeInfo.LONG_TYPE_INFO,
                    TypeInformation.of(new TypeHint<ActJoinTagPost>() {}));

    private static void adsActTagJoinPostJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        Properties kafkaConsumerProps = new Properties();
        kafkaConsumerProps.setProperty("partition.discovery.interval.ms", "60000");

        Properties kafkaProducerProps = new Properties();

        DataStream<String> binlogNdcStream =
                env.fromSource(
                                new KafkaCommonSource(
                                                "lofter.binlog.online",
                                                "AdsActTagJoinPostJob",
                                                kafkaConsumerProps)
                                        .createLogSource(),
                                WatermarkStrategy.noWatermarks(),
                                "NdcBinlogSource")
                        .setParallelism(8)
                        .uid("NdcBinlogKafkaSource")
                        .name("NdcBinlogKafkaSource");

        DataStream<ActJoinTagPost> actJoinTagPostStream =
                binlogNdcStream
                        .flatMap(new AdsActTagJoinPostRichFlatMapFunction())
                        .setParallelism(16)
                        .name("ActJoinTagPostRichFlatMapFunction")
                        .uid("ActJoinTagPostRichFlatMapFunction");

        BroadcastStream<ActJoinTagPost> actJoinTagPostBroadcastStream =
                actJoinTagPostStream.broadcast(ACT_TAG_JOIN_POST_BROADCAST_DESC);

        DataStream<PostHot> postHotStream =
                binlogNdcStream
                        .flatMap(new AdsActTagJoinPostHotRichFlatMapFunction())
                        .setParallelism(16)
                        .name("ActJoinTagPostHotRichFlatMapFunction")
                        .uid("ActJoinTagPostHotRichFlatMapFunction");

        postHotStream
                .connect(actJoinTagPostBroadcastStream)
                .process(new ActJoinTagPostBroadcastProcessFunction())
                .setParallelism(16)
                .name("ActJoinTagPostBroadcastProcessFunction")
                .uid("ActJoinTagPostBroadcastProcessFunction")
                .keyBy(
                        new KeySelector<ActJoinTagPostHot, String>() {
                            @Override
                            public String getKey(ActJoinTagPostHot actJoinTagPostHot)
                                    throws Exception {
                                return actJoinTagPostHot.getActTaskId()
                                        + "_"
                                        + actJoinTagPostHot.getBlogId()
                                        + "_"
                                        + actJoinTagPostHot.getPostId();
                            }
                        })
                .window(TumblingProcessingTimeWindows.of(Duration.ofSeconds(10)))
                .reduce(
                        new AdsActJoinTagPostHotReduceFunction(),
                        new AdsActJoinTagPostProcessWindowFunction())
                .setParallelism(16)
                .name("ActJoinTagPostHotReduceFunction")
                .uid("ActJoinTagPostHotReduceFunction")
                .keyBy(
                        new KeySelector<ActJoinTagPostHot, String>() {
                            @Override
                            public String getKey(ActJoinTagPostHot actJoinTagPostHot)
                                    throws Exception {
                                return actJoinTagPostHot.getActTaskId()
                                        + "_"
                                        + actJoinTagPostHot.getBlogId();
                            }
                        })
                .flatMap(new AdsActJoinTagPostUserStatisticsRichFlatMapFunction())
                .setParallelism(16)
                .name("AdsActJoinTagPostUserStatisticsRichFlatMapFunction")
                .uid("AdsActJoinTagPostUserStatisticsRichFlatMapFunction")
                .sinkTo(
                        new KafkaBaseSink(
                                        KafkaConfig.builder()
                                                .setBootstrapServers(
                                                        ClusterConfigOptions
                                                                .getKafkaBootStrapServers(
                                                                        ClusterConfigOptions
                                                                                .KafkaBootstrapServersEnum
                                                                                .LOFTER_BACKEND))
                                                .setTopic("lofter.common.businessData")
                                                .setProperties(kafkaProducerProps)
                                                .build())
                                .createLogSink())
                .setParallelism(4)
                .name("AdsActJoinTagPostUserStatisticsSink")
                .uid("AdsActJoinTagPostUserStatisticsSink");

        env.execute("AdsActTagJoinPostJob");
    }

    public static void main(String[] args) throws Exception {
        adsActTagJoinPostJob(ParameterTool.fromArgs(args));
    }
}
