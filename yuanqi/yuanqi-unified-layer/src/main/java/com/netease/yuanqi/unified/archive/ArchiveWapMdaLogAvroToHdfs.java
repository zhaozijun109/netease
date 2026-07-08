package com.netease.yuanqi.unified.archive;

import com.netease.yuanqi.common.sink.file.FileSystemCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

public class ArchiveWapMdaLogAvroToHdfs {
    private static void archiveWapMdaLogAvroToHdfs(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(600000); // 10min

        String topics = "lofter.wap.online";

        env.fromSource(
                        new KafkaCommonSource(topics, "ArchiveWapMdaLogAvroToHdfs")
                                .createWapMdaLogAvroSource(),
                        WatermarkStrategy.noWatermarks(),
                        "WapMdaLogAvroSource")
                .setParallelism(8)
                .uid("WapMdaLogAvroKafkaSource")
                .name("WapMdaLogAvroKafkaSource")
                .sinkTo(
                        new FileSystemCommonSink("hdfs://gy-cluster8/user/da_lofter/kafka/avro")
                                .createMultiTopicWapMdaLogAvroParquetSink(
                                        CompressionCodecName.ZSTD))
                .setParallelism(4)
                .uid("WapMdaLogAvroFileSink")
                .name("WapMdaLogAvroFileSink");

        env.execute("ArchiveWapMdaLogAvroToHdfs");
    }

    public static void main(String[] args) throws Exception {
        archiveWapMdaLogAvroToHdfs(ParameterTool.fromArgs(args));
    }
}
