package com.netease.yuanqi.unified.ods;

import com.netease.yuanqi.common.sink.kafka.KafkaCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import com.netease.yuanqi.unified.operator.ods.mda.HubbleMdaLogClientMdaLogAvroRichFlatMapFunction;
import com.netease.yuanqi.unified.operator.ods.mda.HubbleMdaLogProcessFunction;
import com.netease.yuanqi.unified.operator.ods.mda.HubbleMdaLogWapMdaLogAvroRichMapFunction;
import com.netease.yuanqi.unified.operator.ods.mda.HubbleMdaLogWebMdaLogAvroRichMapFunction;
import com.netease.yuanqi.unified.operator.ods.mda.LofterMdaLogTransformJsonRichFlatMapFunction;
import java.util.Properties;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.OutputTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HubbleMdaLogEtlJob {
    private static final Logger LOG = LoggerFactory.getLogger(HubbleMdaLogEtlJob.class);
    public static final OutputTag<String> USELESS_MDA_LOG_OUTPUT_TAG =
            new OutputTag<String>("UselessMdaEventLog") {};
    public static final OutputTag<String> CLIENT_MDA_LOG_OUTPUT_TAG =
            new OutputTag<String>("ClientMdaEventLog") {};
    public static final OutputTag<String> WAP_MDA_LOG_OUTPUT_TAG =
            new OutputTag<String>("WapMdaEventLog") {};
    public static final OutputTag<String> WEB_MDA_LOG_OUTPUT_TAG =
            new OutputTag<String>("WebMdaEventLog") {};

    private static void hubbleMdaLogEtlJob(ParameterTool params) {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.getConfig().enableObjectReuse();
        env.setMaxParallelism(512);
        env.enableCheckpointing(120000);

        Properties properties = new Properties();
        properties.setProperty("partition.discovery.interval.ms", "120000"); // default 5min -> 2min
        properties.setProperty("compression.type", "lz4");

        SingleOutputStreamOperator<Tuple2<String, String>> hubbleLogStream =
                env.fromSource(
                                new KafkaCommonSource(
                                                "hubble.log.online",
                                                "HubbleMdaLogEtlJob",
                                                properties)
                                        .createLogSource(),
                                WatermarkStrategy.noWatermarks(),
                                "HubbleMdaLogSource")
                        .setParallelism(32)
                        .uid("HubbleMdaLogKafkaSource")
                        .name("HubbleMdaLogKafkaSource")
                        .disableChaining()
                        .process(new HubbleMdaLogProcessFunction())
                        .setParallelism(32)
                        .uid("HubbleMdaLogProcessFunction")
                        .name("HubbleMdaLogProcessFunction")
                        .disableChaining();

        hubbleLogStream
                .sinkTo(new KafkaCommonSink(properties).createMdaLogWithTopicSink())
                .setParallelism(16)
                .uid("MdaLogStringKafkaSink")
                .name("MdaLogStringKafkaSink");

        // hubbleLogStream
        //         .getSideOutput(USELESS_MDA_LOG_OUTPUT_TAG)
        //         .sinkTo(new KafkaCommonSink(getUselessMdaLogTopic(), properties).createLogSink())
        //         .setParallelism(8)
        //         .uid("HubbleUselessMdaLogKafkaSink")
        //         .name("HubbleUselessMdaLogKafkaSink");

        hubbleLogStream
                .getSideOutput(CLIENT_MDA_LOG_OUTPUT_TAG)
                .flatMap(new HubbleMdaLogClientMdaLogAvroRichFlatMapFunction())
                .setParallelism(32)
                .uid("ClientMdaLogAvroRichMapFunction")
                .name("ClientMdaLogAvroRichMapFunction")
                .sinkTo(new KafkaCommonSink(properties).createClientMdaLogAvroSink())
                .setParallelism(32)
                .uid("ClientMdaLogAvroKafkaSink")
                .name("ClientMdaLogAvroKafkaSink");

        hubbleLogStream
                .getSideOutput(WAP_MDA_LOG_OUTPUT_TAG)
                .map(new HubbleMdaLogWapMdaLogAvroRichMapFunction())
                .setParallelism(8)
                .uid("WapMdaLogAvroRichMapFunction")
                .name("WapMdaLogAvroRichMapFunction")
                .sinkTo(new KafkaCommonSink(properties).createWapMdaLogAvroSink())
                .setParallelism(8)
                .uid("WapMdaLogAvroKafkaSink")
                .name("WapMdaLogAvroKafkaSink");

        hubbleLogStream
                .getSideOutput(WEB_MDA_LOG_OUTPUT_TAG)
                .map(new HubbleMdaLogWebMdaLogAvroRichMapFunction())
                .setParallelism(4)
                .uid("WebMdaLogAvroRichMapFunction")
                .name("WebMdaLogAvroRichMapFunction")
                .sinkTo(new KafkaCommonSink(properties).createWebMdaLogAvroSink())
                .setParallelism(4)
                .uid("WebMdaLogAvroKafkaSink")
                .name("WebMdaLogAvroKafkaSink");

        // Temporarily add etl logic for lofter.mda.online.json
        hubbleLogStream
                .getSideOutput(CLIENT_MDA_LOG_OUTPUT_TAG)
                .flatMap(new LofterMdaLogTransformJsonRichFlatMapFunction())
                .setParallelism(32)
                .uid("LofterMdaLogTransformJsonRichFlatMapFunction")
                .name("LofterMdaLogTransformJsonRichFlatMapFunction")
                .sinkTo(new KafkaCommonSink("lofter.mda.online.json", properties).createLogSink())
                .setParallelism(32)
                .uid("LofterMdaLogTransformJsonKafkaSink")
                .name("LofterMdaLogTransformJsonKafkaSink");

        try {
            env.execute("HubbleMdaLogETLJob");
        } catch (Exception e) {
            throw new RuntimeException("HubbleMdaLogETLJob execution exception.", e);
        }
    }

    public static void main(String[] args) {
        hubbleMdaLogEtlJob(ParameterTool.fromArgs(args));
    }
}
