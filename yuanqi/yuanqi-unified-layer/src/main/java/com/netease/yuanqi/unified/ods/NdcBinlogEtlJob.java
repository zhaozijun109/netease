package com.netease.yuanqi.unified.ods;

import com.netease.yuanqi.common.sink.kafka.KafkaCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import com.netease.yuanqi.unified.operator.ods.binlog.NdcBinlogRichFlatMapFunction;
import java.util.Properties;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class NdcBinlogEtlJob {
    private static void ndcBinlogEtlJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        Properties properties = new Properties();
        properties.setProperty("partition.discovery.interval.ms", "120000"); // default 5min -> 2min
        properties.setProperty("compression.type", "lz4");
        properties.setProperty("max.request.size", "10485760"); // sink端
        // properties.setProperty("max.partition.fetch.bytes", "10485760"); // 下游source端

        env.fromSource(
                        new KafkaCommonSource("lofter.binlog.ndc", "NdcBinlogEtlJob", properties)
                                .createNdcBinlogSource(),
                        WatermarkStrategy.noWatermarks(),
                        "NdcBinlogSource")
                .setParallelism(8)
                .uid("NdcBinlogKafkaSource")
                .name("NdcBinlogKafkaSource")
                .flatMap(new NdcBinlogRichFlatMapFunction())
                .setParallelism(8)
                .uid("NdcBinlogRichFlatMapFunction")
                .name("NdcBinlogRichFlatMapFunction")
                .sinkTo(new KafkaCommonSink("lofter.binlog.online", properties).createLogSink())
                .setParallelism(8)
                .uid("NdcBinlogKafkaSink")
                .name("NdcBinlogKafkaSink");

        env.execute("NdcBinlogEtlJob");
    }

    public static void main(String[] args) throws Exception {
        ndcBinlogEtlJob(ParameterTool.fromArgs(args));
    }
}
