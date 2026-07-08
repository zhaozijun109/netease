package com.netease.yuanqi.unified.archive.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lofter.rs.basic.bean.dto.upload.ActionDto;
import com.netease.yuanqi.common.sink.file.FileSystemCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import com.netease.yuanqi.common.utils.filesystem.bucket.RecActionLogBucketAssigner;
import java.util.Map;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import rs.basic.upload.parse.dto.ActionHiveDto;
import rs.basic.upload.parse.enums.DataType;
import rs.basic.upload.parse.handler.ActionMessageHandler;

public class ArchiveTestRecActionLogToHdfs {
    private static void archiveTestRecActionLogToHdfs(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(600000); // 10min

        DataStream<Tuple2<Integer, ActionHiveDto>> actionHiveDtoDataStream =
                env.fromSource(
                                new KafkaCommonSource(
                                                "rec_upload_action_parse",
                                                "ArchiveTestRecActionLogToHdfs")
                                        .createLogSource(),
                                WatermarkStrategy.noWatermarks(),
                                "RecActionLogSource")
                        .setParallelism(16)
                        .uid("RecActionLogKafkaSource")
                        .name("RecActionLogKafkaSource")
                        .flatMap(
                                new RichFlatMapFunction<String, Tuple2<Integer, ActionHiveDto>>() {
                                    private ObjectMapper objectMapper;

                                    @Override
                                    public void open(Configuration parameters) throws Exception {
                                        objectMapper = new ObjectMapper();
                                    }

                                    @Override
                                    public void flatMap(
                                            String s,
                                            Collector<Tuple2<Integer, ActionHiveDto>> collector)
                                            throws Exception {
                                        try {
                                            ActionDto actionDto =
                                                    objectMapper.readValue(s, ActionDto.class);
                                            Map<DataType, ActionHiveDto> actionHiveDtoMap =
                                                    ActionMessageHandler.parseHiveActionDto(
                                                            actionDto);
                                            if (actionHiveDtoMap != null
                                                    && !actionHiveDtoMap.isEmpty()) {
                                                if (actionHiveDtoMap.get(DataType.Blog) != null) {
                                                    collector.collect(
                                                            new Tuple2<>(
                                                                    1,
                                                                    actionHiveDtoMap.get(
                                                                            DataType.Blog)));
                                                }

                                                if (actionHiveDtoMap.get(DataType.NoBlog) != null) {
                                                    collector.collect(
                                                            new Tuple2<>(
                                                                    2,
                                                                    actionHiveDtoMap.get(
                                                                            DataType.NoBlog)));
                                                }
                                            }
                                        } catch (JsonProcessingException e) {
                                            System.out.println(e.getMessage());
                                        }
                                    }
                                })
                        .setParallelism(16)
                        .uid("RecActionLogRichFlatMapFunction")
                        .name("RecActionLogRichFlatMapFunction");

        actionHiveDtoDataStream
                .filter(tuple2 -> tuple2.f0 == 1)
                .setParallelism(16)
                .uid("RecBlogActionLogFilterFunction")
                .name("RecBlogActionLogFilterFunction")
                .map(tuple2 -> tuple2.f1)
                .setParallelism(16)
                .uid("RecBlogActionLogMapFunction")
                .name("RecBlogActionLogMapFunction")
                .sinkTo(
                        new FileSystemCommonSink(
                                        "hdfs://gy-cluster8/user/rec/zhaozijun03/ods_lofter_blog_action_di/")
                                .createCommonReflectAvroParquetSink(
                                        ActionHiveDto.class,
                                        CompressionCodecName.GZIP,
                                        new RecActionLogBucketAssigner("'day='yyyy-MM-dd")))
                .setParallelism(4)
                .uid("RecBlogActionLogFileSink")
                .name("RecBlogActionLogFileSink");

        actionHiveDtoDataStream
                .filter(tuple2 -> tuple2.f0 == 2)
                .setParallelism(16)
                .uid("RecNoBlogActionLogFilterFunction")
                .name("RecNoBlogActionLogFilterFunction")
                .map(tuple2 -> tuple2.f1)
                .setParallelism(16)
                .uid("RecNoBlogActionLogMapFunction")
                .name("RecNoBlogActionLogMapFunction")
                .sinkTo(
                        new FileSystemCommonSink(
                                        "hdfs://gy-cluster8/user/rec/zhaozijun03/ods_lofter_noblog_action_di/")
                                .createCommonReflectAvroParquetSink(
                                        ActionHiveDto.class,
                                        CompressionCodecName.GZIP,
                                        new RecActionLogBucketAssigner("'day='yyyy-MM-dd")))
                .setParallelism(8)
                .uid("RecNoBlogActionLogFileSink")
                .name("RecNoBlogActionLogFileSink");

        env.execute("ArchiveRecActionLogToHdfs");
    }

    public static void main(String[] args) throws Exception {
        archiveTestRecActionLogToHdfs(ParameterTool.fromArgs(args));
    }
}
