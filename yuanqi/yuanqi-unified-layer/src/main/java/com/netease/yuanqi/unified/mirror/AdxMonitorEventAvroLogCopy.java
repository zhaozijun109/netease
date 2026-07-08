package com.netease.yuanqi.unified.mirror;

import com.netease.wm.hubble.avro.AdxMonitorEvent;
import com.netease.yuanqi.common.pojo.config.KafkaConfig;
import com.netease.yuanqi.common.sink.kafka.KafkaCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaBaseSource;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

@Deprecated
public class AdxMonitorEventAvroLogCopy {
    private static void adxMonitorEventAvroLogCopy(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(600000); // 10min

        KafkaConfig kafkaConfig =
                KafkaConfig.builder()
                        .setBootstrapServers(
                                "lofter-kafka3.jd.163.org:9092,lofter-kafka4.jd.163.org:9092,lofter-kafka5.jd.163.org:9092")
                        .setTopics("adx.server.avro")
                        .setGroupId("AdxMonitorEventAvroLogCopyTest")
                        .setStartingOffsetsInitializer(OffsetsInitializer.latest())
                        .build();

        env.fromSource(
                        new KafkaBaseSource(kafkaConfig)
                                .createCommonSpecificAvroSource(AdxMonitorEvent.class),
                        WatermarkStrategy.noWatermarks(),
                        "AdxMonitorEventAvroSource")
                .setParallelism(6)
                .uid("AdxMonitorEventAvroKafkaSource")
                .name("AdxMonitorEventAvroKafkaSource")
                .rebalance()
                .sinkTo(
                        new KafkaCommonSink("adx.server.avro")
                                .createCommonSpecificAvroSink(AdxMonitorEvent.class))
                .setParallelism(6)
                .uid("AdxMonitorEventAvroKafkaSink")
                .name("AdxMonitorEventAvroKafkaSink");

        env.execute("AdxMonitorEventAvroLogCopy");
    }

    public static void main(String[] args) throws Exception {
        adxMonitorEventAvroLogCopy(ParameterTool.fromArgs(args));
    }
}
