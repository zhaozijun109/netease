package com.netease.yuanqi.unified.archive;

import com.netease.yuanqi.common.sink.file.FileSystemCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import com.netease.yuanqi.unified.operator.archive.ArchiveBinlogToHdfsRichMapFunction;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

public class ArchiveNdcBinlogToHdfs {
    private static void archiveNdcBinlogToHdfs(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(600000); // 10min

        env.fromSource(
                        new KafkaCommonSource("lofter.binlog.online", "ArchiveNdcBinlogToHdfs")
                                .createLogSource(),
                        WatermarkStrategy.noWatermarks(),
                        "NdcBinlogSource")
                .setParallelism(8)
                .uid("NdcBinlogKafkaSource")
                .name("NdcBinlogKafkaSource")
                .map(new ArchiveBinlogToHdfsRichMapFunction())
                .setParallelism(8)
                .uid("NdcBinlogRichMapFunction")
                .name("NdcBinlogRichMapFunction")
                .sinkTo(
                        new FileSystemCommonSink("hdfs://gy-cluster8/user/da_lofter/binlog")
                                .createArchiveFormatRowBulkSink(CompressionCodecName.GZIP))
                .setParallelism(4)
                .uid("NdcBinlogFileSink")
                .name("NdcBinlogFileSink");

        env.execute("ArchiveNdcBinlogToHdfs");
    }

    public static void main(String[] args) throws Exception {
        archiveNdcBinlogToHdfs(ParameterTool.fromArgs(args));
    }
}
