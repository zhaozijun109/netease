package com.netease.yuanqi.unified.archive;

import com.netease.yuanqi.common.sink.file.FileSystemCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import com.netease.yuanqi.unified.operator.archive.ArchiveBackendLogToHdfsRichMapFunction;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

public class ArchiveBackendLogToHdfs {
    private static void archiveBackendLogToHdfs(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(600000); // 10min

        env.fromSource(
                        new KafkaCommonSource("lofter.backend.log.bi", "ArchiveBackendLogToHdfs")
                                .createLogSource(),
                        WatermarkStrategy.noWatermarks(),
                        "BackendLogSource")
                .setParallelism(4)
                .uid("BackendLogKafkaSource")
                .name("BackendLogKafkaSource")
                .map(new ArchiveBackendLogToHdfsRichMapFunction())
                .setParallelism(4)
                .uid("BackendLogRichMapFunction")
                .name("BackendLogRichMapFunction")
                .sinkTo(
                        new FileSystemCommonSink("hdfs://gy-cluster8/user/da_lofter/backendLog")
                                .createArchiveFormatRowBulkSink(CompressionCodecName.GZIP))
                .setParallelism(4)
                .uid("BackendLogFileSink")
                .name("BackendLogFileSink");

        env.execute("ArchiveBackendLogToHdfs");
    }

    public static void main(String[] args) throws Exception {
        archiveBackendLogToHdfs(ParameterTool.fromArgs(args));
    }
}
