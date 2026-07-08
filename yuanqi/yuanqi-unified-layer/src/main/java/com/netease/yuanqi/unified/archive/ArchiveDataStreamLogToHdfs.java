package com.netease.yuanqi.unified.archive;

import com.netease.yuanqi.common.sink.file.FileSystemCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

@Deprecated
/** Handle data with datastream way. */
public class ArchiveDataStreamLogToHdfs {
    private static void archiveDataStreamLogToHdfs(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(600000); // 10min

        String topics =
                "lofter.tomcat.online, adx.server.online, adserver.dsp.online, adx.click.log, risk.yidun.personalaudit,"
                        + "forbid.reg.phone, risk.postdigest.audit, lofter.backend.log.phoneLogin, lofter.backend.log.bi, "
                        + "trace.request.meta, lofter.webapp.antispam.callback, lofter.brush.log.dispose, lofter.suggest.search.word.log, "
                        + "lofter.tag.artificial.import, lofter.slider.result.log, lofter.posthot.deduct, lofter.incantation.backend.log,"
                        + "lofter.slider.backend.log, lofter.acw.backend.log, lofter.shortlink.log, lofter.activity.backend.log, "
                        + "lofter.push.log, lofter.suspect.log, lofter.antisend.video, lofter.risk.access.api, lofter.anti.risk.similarity,"
                        + "lofter.antisend.online, lofter.anti.risk.log, lofter.antispam.callback, lofter.outerlinkup.online, "
                        + "lofter.postchange.online, adx.newlinkup.online, lofter.spm.online, lofter_qa_square, lofter_consumer_for_ds,"
                        + "rec.text_article_identification, data_center_ds_ab_log, rec.text_article_identification, "
                        + "data_center_ds_ab_log, risk.limit";

        env.fromSource(
                        new KafkaCommonSource(topics, "ArchiveDataStreamLogToHdfs")
                                .createDataStreamArchiveFormatRowSource(),
                        WatermarkStrategy.noWatermarks(),
                        "DataStreamLogSource")
                .setParallelism(8)
                .uid("DataStreamLogKafkaSource")
                .name("DataStreamLogKafkaSource")
                .disableChaining()
                .sinkTo(
                        new FileSystemCommonSink("hdfs://gy-cluster8/user/da_lofter/datastream")
                                .createArchiveFormatRowBulkSink(CompressionCodecName.GZIP))
                .setParallelism(8)
                .uid("DataStreamLogFileSink")
                .name("DataStreamLogFileSink");

        env.execute("ArchiveDataStreamLogToHdfs");
    }

    public static void main(String[] args) throws Exception {
        archiveDataStreamLogToHdfs(ParameterTool.fromArgs(args));
    }
}
