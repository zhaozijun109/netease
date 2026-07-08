package com.netease.yuanqi.unified.archive;

import com.netease.yuanqi.common.sink.file.FileSystemCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaRecSource;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

public class ArchiveFeedRecArticleToHdfs {
    private static void archiveFeedRecArticleToHdfs(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(600000); // 10min

        env.fromSource(
                        new KafkaRecSource(
                                        "rec_ds_feed_rec_article_feature_dump_music",
                                        "rec_article_feature_dump")
                                .createRecMusicFeatureArchiveFormatRowSource(
                                        "feed_rec_article_parse"),
                        WatermarkStrategy.noWatermarks(),
                        "RecArticleFeatureSource")
                .setParallelism(8)
                .uid("FeedRecArticleFeatureKafkaSource")
                .name("FeedRecArticleFeatureKafkaSource")
                .disableChaining()
                .sinkTo(
                        new FileSystemCommonSink("hdfs://gy-cluster8/user/rec/music_feature_dump/")
                                .createArchiveFormatRowBulkSink(CompressionCodecName.GZIP))
                .setParallelism(8)
                .uid("FeedRecArticleFeatureFileSink")
                .name("FeedRecArticleFeatureFileSink");

        env.execute("ArchiveFeedRecArticleToHdfs");
    }

    public static void main(String[] args) throws Exception {
        archiveFeedRecArticleToHdfs(ParameterTool.fromArgs(args));
    }
}
