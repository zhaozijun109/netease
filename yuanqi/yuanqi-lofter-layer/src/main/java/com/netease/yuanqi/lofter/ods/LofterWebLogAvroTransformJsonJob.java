package com.netease.yuanqi.lofter.ods;

import com.netease.yuanqi.common.pojo.avro.ods.WebMdaLogAvro;
import com.netease.yuanqi.common.sink.kafka.KafkaCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import java.util.Properties;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LofterWebLogAvroTransformJsonJob {
    private static final Logger LOG =
            LoggerFactory.getLogger(LofterWebLogAvroTransformJsonJob.class);

    private static void lofterWebLogAvroTransformJsonJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.getConfig().enableObjectReuse();
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        Properties properties = new Properties();
        properties.setProperty("partition.discovery.interval.ms", "120000"); // default 5min -> 2min
        properties.setProperty("compression.type", "lz4");

        env.fromSource(
                        new KafkaCommonSource(
                                        "lofter.web.online", "LofterWebLogAvroTransformJsonJob")
                                .createWebMdaLogAvroSource(),
                        WatermarkStrategy.noWatermarks(),
                        "LofterWebLogAvroSource")
                .setParallelism(4)
                .uid("LofterWebLogAvroKafkaSource")
                .name("LofterWebLogAvroKafkaSource")
                .map(
                        new MapFunction<WebMdaLogAvro, String>() {
                            @Override
                            public String map(WebMdaLogAvro webMdaLogAvro) throws Exception {
                                return webMdaLogAvro.toString();
                            }
                        })
                .setParallelism(4)
                .disableChaining()
                .uid("LofterWebLogAvroMapFunction")
                .name("LofterWebLogAvroMapFunction")
                .sinkTo(new KafkaCommonSink("lofter.web.online.json", properties).createLogSink())
                .setParallelism(4)
                .uid("LofterWebLogAvroKafkaSink")
                .name("LofterWebLogAvroKafkaSink");

        env.execute("LofterWebLogAvroTransformJsonJob");
    }

    public static void main(String[] args) throws Exception {
        lofterWebLogAvroTransformJsonJob(ParameterTool.fromArgs(args));
    }
}
