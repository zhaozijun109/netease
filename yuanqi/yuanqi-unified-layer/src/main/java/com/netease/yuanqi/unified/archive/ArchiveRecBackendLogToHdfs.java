package com.netease.yuanqi.unified.archive;

import com.netease.yuanqi.common.sink.file.FileSystemCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaRecSource;
import com.netease.yuanqi.common.utils.filesystem.bucket.RecBackendLogBucketAssigner;
import java.util.Map;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.basic.upload.parse.dto.RecHiveDto;
import rs.basic.upload.parse.enums.DataType;
import rs.basic.upload.parse.handler.RecMessageHandler;

public class ArchiveRecBackendLogToHdfs {
    private static final Logger LOG = LoggerFactory.getLogger(ArchiveRecBackendLogToHdfs.class);

    private static void archiveRecBackendLogToHdfs(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(600000); // 10min

        DataStream<Tuple2<Integer, RecHiveDto>> recnHiveDtoDataStream =
                env.fromSource(
                                new KafkaRecSource("rec_ds_rec_log", "ArchiveRecBackendLogToHdfs")
                                        .createLogSource(),
                                WatermarkStrategy.noWatermarks(),
                                "RecBackendLogSource")
                        .setParallelism(6)
                        .uid("RecBackendLogKafkaSource")
                        .name("RecBackendLogKafkaSource")
                        .flatMap(
                                new FlatMapFunction<String, Tuple2<Integer, RecHiveDto>>() {
                                    @Override
                                    public void flatMap(
                                            String s,
                                            Collector<Tuple2<Integer, RecHiveDto>> collector)
                                            throws Exception {
                                        try {
                                            Map<DataType, RecHiveDto> recHiveDtoMap =
                                                    RecMessageHandler.parseRecDto(s);
                                            if (recHiveDtoMap != null && !recHiveDtoMap.isEmpty()) {
                                                if (recHiveDtoMap.get(DataType.Blog) != null) {
                                                    collector.collect(
                                                            new Tuple2<>(
                                                                    1,
                                                                    recHiveDtoMap.get(
                                                                            DataType.Blog)));
                                                }

                                                if (recHiveDtoMap.get(DataType.NoBlog) != null) {
                                                    collector.collect(
                                                            new Tuple2<>(
                                                                    2,
                                                                    recHiveDtoMap.get(
                                                                            DataType.NoBlog)));
                                                }
                                            }
                                        } catch (Exception e) {
                                            LOG.error("Error format message: {}", s, e);
                                        }
                                    }
                                })
                        .setParallelism(6)
                        .uid("RecBackendLogFlatMapFunction")
                        .name("RecBackendLogFlatMapFunction");

        recnHiveDtoDataStream
                .filter(tuple2 -> tuple2.f0 == 1)
                .setParallelism(6)
                .uid("RecBlogBackendLogFilterFunction")
                .name("RecBlogBackendLogFilterFunction")
                .map(tuple2 -> tuple2.f1)
                .setParallelism(6)
                .uid("RecBlogBackendLogMapFunction")
                .name("RecBlogBackendLogMapFunction")
                .sinkTo(
                        new FileSystemCommonSink(
                                        "hdfs://gy-cluster8/user/rec/lofter_hive/ods_lofter_blog_rec_di/")
                                .createCommonReflectAvroParquetSink(
                                        RecHiveDto.class,
                                        CompressionCodecName.GZIP,
                                        new RecBackendLogBucketAssigner("'day='yyyy-MM-dd")))
                .setParallelism(1)
                .uid("RecBlogBackendLogFileSink")
                .name("RecBlogBackendLogFileSink");

        recnHiveDtoDataStream
                .filter(tuple2 -> tuple2.f0 == 2)
                .setParallelism(6)
                .uid("RecNoBlogBackendLogFilterFunction")
                .name("RecNoBlogBackendLogFilterFunction")
                .map(tuple2 -> tuple2.f1)
                .setParallelism(6)
                .uid("RecNoBlogBackendLogMapFunction")
                .name("RecNoBlogBackendLogMapFunction")
                .sinkTo(
                        new FileSystemCommonSink(
                                        "hdfs://gy-cluster8/user/rec/lofter_hive/ods_lofter_noblog_rec_di/")
                                .createCommonReflectAvroParquetSink(
                                        RecHiveDto.class,
                                        CompressionCodecName.GZIP,
                                        new RecBackendLogBucketAssigner("'day='yyyy-MM-dd")))
                .setParallelism(6)
                .uid("RecNoBlogBackendLogFileSink")
                .name("RecNoBlogBackendLogFileSink");

        env.execute("ArchiveRecBackendLogToHdfs");
    }

    public static void main(String[] args) throws Exception {
        archiveRecBackendLogToHdfs(ParameterTool.fromArgs(args));
    }
}
