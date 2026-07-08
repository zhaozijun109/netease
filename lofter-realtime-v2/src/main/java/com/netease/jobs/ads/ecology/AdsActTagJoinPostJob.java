package com.netease.jobs.ads.ecology;

import com.netease.operator.ads.ecology.post.ActJoinTagPostBroadcastProcessFunction;
import com.netease.operator.ads.ecology.post.AdsActJoinTagPostHotReduceFunction;
import com.netease.operator.ads.ecology.post.AdsActJoinTagPostProcessWindowFunction;
import com.netease.operator.ads.ecology.post.AdsActJoinTagPostUserStatisticsRichFlatMapFunction;
import com.netease.operator.ads.ecology.post.AdsActTagJoinPostHotRichFlatMapFunction;
import com.netease.operator.ads.ecology.post.AdsActTagJoinPostRichFlatMapFunction;
import com.netease.pojo.ecology.post.ActJoinTagPost;
import com.netease.pojo.ecology.post.ActJoinTagPostHot;
import com.netease.pojo.ecology.post.PostHot;
import com.netease.sink.KafkaProducerSink;
import com.netease.source.KafkaConsumerSource;
import com.netease.util.LoadConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.apache.flink.streaming.api.windowing.time.Time;

import java.util.Properties;

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

        DataStream<String> dwdBinlogNdcStream =
                env.fromSource(
                                new KafkaConsumerSource(
                                                params.getRequired("kafka.bootstrap.servers"),
                                                params.getRequired(
                                                        "kafka.lofter.dwd_binlog_ndc_topic"),
                                                "AdsActTagJoinPostJob",
                                                kafkaConsumerProps)
                                        .createLatestKafkaSource(),
                                WatermarkStrategy.noWatermarks(),
                                "DwdBinlogNdcSource")
                        .setParallelism(6)
                        .name("AdsActTagJoinPostJobSource")
                        .uid("AdsActTagJoinPostJobSource");

        DataStream<ActJoinTagPost> actJoinTagPostStream =
                dwdBinlogNdcStream
                        .flatMap(new AdsActTagJoinPostRichFlatMapFunction())
                        .setParallelism(12)
                        .name("ActJoinTagPostRichFlatMapFunction")
                        .uid("ActJoinTagPostRichFlatMapFunction");

        BroadcastStream<ActJoinTagPost> actJoinTagPostBroadcastStream =
                actJoinTagPostStream.broadcast(ACT_TAG_JOIN_POST_BROADCAST_DESC);

        DataStream<PostHot> postHotStream =
                dwdBinlogNdcStream
                        .flatMap(new AdsActTagJoinPostHotRichFlatMapFunction())
                        .setParallelism(12)
                        .name("ActJoinTagPostHotRichFlatMapFunction")
                        .uid("ActJoinTagPostHotRichFlatMapFunction");

        postHotStream
                .connect(actJoinTagPostBroadcastStream)
                .process(new ActJoinTagPostBroadcastProcessFunction())
                .setParallelism(12)
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
                .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
                .reduce(
                        new AdsActJoinTagPostHotReduceFunction(),
                        new AdsActJoinTagPostProcessWindowFunction())
                .setParallelism(12)
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
                .setParallelism(12)
                .name("AdsActJoinTagPostUserStatisticsRichFlatMapFunction")
                .uid("AdsActJoinTagPostUserStatisticsRichFlatMapFunction")
                .sinkTo(
                        new KafkaProducerSink(
                                        params.getRequired("kafka.bootstrap.servers.backend"),
                                        params.getRequired("kafka.lofter.backend_common_topic"),
                                        kafkaProducerProps)
                                .createKafkaProducerSink())
                .setParallelism(6)
                .name("AdsActJoinTagPostUserStatisticsSink")
                .uid("AdsActJoinTagPostUserStatisticsSink");

        env.execute("AdsActTagJoinPostJob");
    }

    public static void main(String[] args) throws Exception {
        ParameterTool params = new LoadConfigurationUtils().loadConfiguration(args);
        adsActTagJoinPostJob(params);
    }
}
