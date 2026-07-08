package com.netease.yuanqi.ycy.ods;

import com.netease.yuanqi.common.pojo.avro.ods.ClientMdaLogAvro;
import com.netease.yuanqi.common.sink.kafka.KafkaCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import java.util.Properties;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YcyNaClientMdaLogAvroTransformJsonJob {
    private static final Logger LOG =
            LoggerFactory.getLogger(YcyNaClientMdaLogAvroTransformJsonJob.class);

    private static void ycyNaClientMdaLogAvroTransformJsonJob(ParameterTool params)
            throws Exception {
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
                                        "ycy.na.mda.online",
                                        "YcyNaClientMdaLogAvroTransformJsonJob")
                                .createClientMdaLogAvroSource(),
                        WatermarkStrategy.noWatermarks(),
                        "YcyNaClientMdaLogAvroSource")
                .setParallelism(4)
                .uid("YcyNaClientMdaLogAvroKafkaSource")
                .name("YcyNaClientMdaLogAvroKafkaSource")
                .map(
                        new MapFunction<ClientMdaLogAvro, String>() {
                            @Override
                            public String map(ClientMdaLogAvro clientMdaLogAvro) throws Exception {
                                return clientMdaLogAvro.toString();
                            }
                        })
                .setParallelism(4)
                .uid("YcyNaClientMdaLogAvroMapFunction")
                .name("YcyNaClientMdaLogAvroMapFunction")
                .disableChaining()
                .sinkTo(new KafkaCommonSink("ycy.na.mda.online.json", properties).createLogSink())
                .setParallelism(4)
                .uid("YcyNaClientMdaLogAvroKafkaSink")
                .name("YcyNaClientMdaLogAvroKafkaSink");

        env.execute("Ycy Na Client Mda Log Avro Transform Json Job");
    }

    public static void main(String[] args) throws Exception {
        ycyNaClientMdaLogAvroTransformJsonJob(ParameterTool.fromArgs(args));
    }
}
