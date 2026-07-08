package com.netease.yuanqi.unified.archive;

import com.netease.yuanqi.common.sink.file.FileSystemCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

public class ArchiveWebMdaLogAvroToHdfs {
    private static void archiveWebMdaLogAvroToHdfs(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(600000); // 10min

        String topics = "lofter.web.online";

        env.fromSource(
                        new KafkaCommonSource(topics, "ArchiveWebMdaLogAvroToHdfs")
                                .createWebMdaLogAvroSource(),
                        WatermarkStrategy.noWatermarks(),
                        "WebMdaLogAvroSource")
                .setParallelism(8)
                .uid("WebMdaLogAvroKafkaSource")
                .name("WebMdaLogAvroKafkaSource")
                .sinkTo(
                        new FileSystemCommonSink("hdfs://gy-cluster8/user/da_lofter/kafka/avro")
                                .createMultiTopicWebMdaLogAvroParquetSink(
                                        CompressionCodecName.ZSTD))
                .setParallelism(4)
                .uid("WebMdaLogAvroFileSink")
                .name("WebMdaLogAvroFileSink");

        env.execute("ArchiveWebMdaLogAvroToHdfs");
    }

    public static void main(String[] args) throws Exception {
        archiveWebMdaLogAvroToHdfs(ParameterTool.fromArgs(args));
    }
}
