package com.netease.yuanqi.lofter.ods;

import com.netease.yuanqi.common.pojo.avro.ods.WapMdaLogAvro;
import com.netease.yuanqi.common.sink.kafka.KafkaCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import java.util.Properties;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LofterWapLogAvroTransformJsonJob {
    private static final Logger LOG =
            LoggerFactory.getLogger(LofterWapLogAvroTransformJsonJob.class);

    private static void lofterWapLogAvroTransformJsonJob(ParameterTool params) throws Exception {
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
                                        "lofter.wap.online", "LofterWapLogAvroTransformJsonJob")
                                .createWapMdaLogAvroSource(),
                        WatermarkStrategy.noWatermarks(),
                        "LofterWapLogAvroSource")
                .setParallelism(8)
                .uid("LofterWapLogAvroKafkaSource")
                .name("LofterWapLogAvroKafkaSource")
                .map(
                        new MapFunction<WapMdaLogAvro, String>() {
                            @Override
                            public String map(WapMdaLogAvro wapMdaLogAvro) throws Exception {
                                return wapMdaLogAvro.toString();
                            }
                        })
                .setParallelism(8)
                .disableChaining()
                .uid("LofterWapLogAvroMapFunction")
                .name("LofterWapLogAvroMapFunction")
                .sinkTo(new KafkaCommonSink("lofter.wap.online.json", properties).createLogSink())
                .setParallelism(8)
                .uid("LofterWapLogAvroKafkaSink")
                .name("LofterWapLogAvroKafkaSink");

        env.execute("LofterWapLogAvroTransformJsonJob");
    }

    public static void main(String[] args) throws Exception {
        lofterWapLogAvroTransformJsonJob(ParameterTool.fromArgs(args));
    }
}
