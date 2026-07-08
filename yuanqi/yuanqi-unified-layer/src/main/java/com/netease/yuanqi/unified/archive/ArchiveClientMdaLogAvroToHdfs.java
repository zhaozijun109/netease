package com.netease.yuanqi.unified.archive;

import com.netease.yuanqi.common.sink.file.FileSystemCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

public class ArchiveClientMdaLogAvroToHdfs {
    private static void archiveClientMdaLogAvroToHdfs(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(600000); // 10min

        String topics = "lofter.mda.online, ycy.mda.online, ycy.na.mda.online";

        env.fromSource(
                        new KafkaCommonSource(topics, "ArchiveClientMdaLogAvroToHdfs")
                                .createClientMdaLogAvroSource(),
                        WatermarkStrategy.noWatermarks(),
                        "ClientMdaLogAvroSource")
                .setParallelism(32)
                .uid("ClientMdaLogAvroKafkaSource")
                .name("ClientMdaLogAvroKafkaSource")
                .disableChaining()
                .sinkTo(
                        new FileSystemCommonSink("hdfs://gy-cluster8/user/da_lofter/kafka/avro")
                                .createMultiTopicClientMdaLogAvroParquetSink(
                                        CompressionCodecName.ZSTD))
                .setParallelism(32)
                .uid("ClientMdaLogAvroFileSink")
                .name("ClientMdaLogAvroFileSink");

        env.execute("ArchiveClientMdaLogAvroToHdfs");
    }

    public static void main(String[] args) throws Exception {
        archiveClientMdaLogAvroToHdfs(ParameterTool.fromArgs(args));
    }
}
