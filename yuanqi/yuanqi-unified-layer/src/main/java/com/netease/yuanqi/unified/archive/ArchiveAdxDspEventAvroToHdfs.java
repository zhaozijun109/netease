package com.netease.yuanqi.unified.archive;

import com.netease.wm.hubble.avro.AdxDspEvent;
import com.netease.yuanqi.common.sink.file.FileSystemCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.DateTimeBucketAssigner;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

public class ArchiveAdxDspEventAvroToHdfs {
    private static void archiveAdxDspEventAvroToHdfs(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(600000); // 10min

        env.fromSource(
                        new KafkaCommonSource("adserver.dsp.avro", "archive-adserver-dsp-avro")
                                .createCommonSpecificAvroSource(AdxDspEvent.class),
                        WatermarkStrategy.noWatermarks(),
                        "ArchiveAdxDspEventKafkaSource")
                .setParallelism(6)
                .uid("ArchiveAdxDspEventKafkaSource")
                .name("ArchiveAdxDspEventKafkaSource")
                .sinkTo(
                        new FileSystemCommonSink("hdfs://gy-cluster8/user/da_lofter/warehouse/dsp")
                                .createCommonSpecificAvroParquetSink(
                                        AdxDspEvent.class,
                                        CompressionCodecName.GZIP,
                                        new DateTimeBucketAssigner<>("'dt='yyyy-MM-dd")))
                .setParallelism(6)
                .uid("ArchiveAdxDspEventAvroFileSink")
                .name("ArchiveAdxDspEventAvroFileSink");

        env.execute("ArchiveAdxDspEventAvroToHdfs");
    }

    public static void main(String[] args) throws Exception {
        archiveAdxDspEventAvroToHdfs(ParameterTool.fromArgs(args));
    }
}
