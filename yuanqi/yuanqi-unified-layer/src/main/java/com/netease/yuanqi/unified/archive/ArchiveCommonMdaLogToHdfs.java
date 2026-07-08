package com.netease.yuanqi.unified.archive;

import com.netease.yuanqi.common.sink.file.FileSystemCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

public class ArchiveCommonMdaLogToHdfs {
    private static void archiveCommonMdaLogToHdfs(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(600000); // 10min

        String topics =
                "lofter.miniprogram.online, lofter.bookstore-miniprogram.online,"
                        + "snail.weixin.online, snail.mda.online, snail.web.online, snail.wap.online,"
                        + "lofter.push.reach, lofter_exception_log, lofter_simulation_log, lofter_ad_log,"
                        + "vc.mda.online, vc.wap.online,"
                        + "ruyuan.miniprogram.online,"
                        + "yuedu.mda.online, lofter.acw.front.log,"
                        + "adx.newlinkup.online," // adx.newlinkup.register
                        + "lofter.creator-stimulus-pm.online, lofter.creator.attribution_preview,"
                        + "lofter.creator.potential_preview, lofter.outerlinkup.online, lofter.photopost.online,"
                        + "lofter.push.anti.addiction, lofter.session.time, vc.game.web.online";

        env.fromSource(
                        new KafkaCommonSource(topics, "ArchiveCommonMdaLogToHdfs")
                                .createMdaArchiveFormatRowSource(),
                        WatermarkStrategy.noWatermarks(),
                        "CommonMdaLogSource")
                .setParallelism(8)
                .uid("CommonMdaLogKafkaSource")
                .name("CommonMdaLogKafkaSource")
                .disableChaining()
                .sinkTo(
                        new FileSystemCommonSink("hdfs://gy-cluster8/user/da_lofter/kafka")
                                .createArchiveFormatRowBulkSink(CompressionCodecName.GZIP))
                .setParallelism(8)
                .uid("CommonMdaLogFileSink")
                .name("CommonMdaLogFileSink");

        env.execute("ArchiveCommonMdaLogToHdfs");
    }

    public static void main(String[] args) throws Exception {
        archiveCommonMdaLogToHdfs(ParameterTool.fromArgs(args));
    }
}
