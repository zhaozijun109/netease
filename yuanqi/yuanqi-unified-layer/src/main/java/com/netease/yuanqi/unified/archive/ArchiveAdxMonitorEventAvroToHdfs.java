package com.netease.yuanqi.unified.archive;

import com.netease.wm.hubble.avro.AdxMonitorEvent;
import com.netease.yuanqi.common.sink.file.FileSystemCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.DateTimeBucketAssigner;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

public class ArchiveAdxMonitorEventAvroToHdfs {
    private static void archiveAdxMonitorEventAvroToHdfs(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(600000); // 10min

        env.fromSource(
                        new KafkaCommonSource("adx.server.avro", "ArchiveAdxMonitorEventAvroToHdfs")
                                .createCommonSpecificAvroSource(AdxMonitorEvent.class),
                        WatermarkStrategy.noWatermarks(),
                        "AdxMonitorEventAvroSource")
                .setParallelism(6)
                .uid("AdxMonitorEventAvroKafkaSource")
                .name("AdxMonitorEventAvroKafkaSource")
                .sinkTo(
                        new FileSystemCommonSink("hdfs://gy-cluster8/user/da_lofter/warehouse/adx")
                                .createCommonSpecificAvroParquetSink(
                                        AdxMonitorEvent.class,
                                        CompressionCodecName.GZIP,
                                        new DateTimeBucketAssigner<>("'dt='yyyy-MM-dd")))
                .setParallelism(6)
                .uid("AdxMonitorEventAvroFileSink")
                .name("AdxMonitorEventAvroFileSink");

        env.execute("ArchiveAdxMonitorEventAvroToHdfs");
    }

    public static void main(String[] args) throws Exception {
        archiveAdxMonitorEventAvroToHdfs(ParameterTool.fromArgs(args));
    }
}
