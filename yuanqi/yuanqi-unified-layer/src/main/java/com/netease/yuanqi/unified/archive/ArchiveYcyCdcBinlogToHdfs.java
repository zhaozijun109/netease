package com.netease.yuanqi.unified.archive;

import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.pojo.config.KafkaConfig;
import com.netease.yuanqi.common.sink.file.FileSystemCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaBaseSource;
import com.netease.yuanqi.unified.operator.archive.ArchiveBinlogToHdfsRichMapFunction;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

public class ArchiveYcyCdcBinlogToHdfs {
    public static void archiveBinlogRowToHdfs(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(600000); // 10min

        String path = params.get("path");

        env.fromSource(
                        new KafkaBaseSource(
                                        KafkaConfig.builder()
                                                .setBootstrapServers(
                                                        ClusterConfigOptions
                                                                .getKafkaBootStrapServers(
                                                                        ClusterConfigOptions
                                                                                .KafkaBootstrapServersEnum
                                                                                .YCY_GUIANSERVER))
                                                .setProperties(
                                                        ClusterConfigOptions
                                                                .getKafkaSecurityProperty(
                                                                        ClusterConfigOptions
                                                                                .KafkaBootstrapServersEnum
                                                                                .YCY_GUIANSERVER))
                                                .setTopics("a13_avg_data_binlog_gy_dump")
                                                .setGroupId("ArchiveYcyCdcBinlogToHdfs")
                                                .build())
                                .createLogSource(),
                        WatermarkStrategy.noWatermarks(),
                        "Source")
                .setParallelism(4)
                .uid("YcyCdcBinlogKafkaSource")
                .name("YcyCdcBinlogKafkaSource")
                .map(new ArchiveBinlogToHdfsRichMapFunction())
                .setParallelism(4)
                .uid("YcyCdcBinlogRichMapFunction")
                .name("YcyCdcBinlogRichMapFunction")
                .sinkTo(
                        new FileSystemCommonSink(path)
                                .createArchiveFormatRowBulkSink(CompressionCodecName.GZIP))
                .setParallelism(4)
                .uid("YcyCdcBinlogFileSink")
                .name("YcyCdcBinlogFileSink");

        env.execute("ArchiveYcyCdcBinlogToHdfs");
    }

    public static void main(String[] args) throws Exception {
        archiveBinlogRowToHdfs(ParameterTool.fromArgs(args));
    }
}
