package com.netease.yuanqi.unified.archive;

import static com.netease.yuanqi.unified.operator.archive.ArchiveYcyKafkaLogProcessFunction.FALLBACK_TAG;

import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.config.ClusterConfigOptions.KafkaBootstrapServersEnum;
import com.netease.yuanqi.common.pojo.archive.ArchiveFormatRow;
import com.netease.yuanqi.common.pojo.archive.ycy.kafka.KafkaArchiveRecord;
import com.netease.yuanqi.common.pojo.config.KafkaConfig;
import com.netease.yuanqi.common.sink.file.FileSystemCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaBaseSource;
import com.netease.yuanqi.common.utils.filesystem.bucket.ArchiveFormatRowBucketAssigner;
import com.netease.yuanqi.unified.operator.archive.ArchiveYcyKafkaLogProcessFunction;
import com.netease.yuanqi.unified.operator.archive.ArchiveYcyKafkaLogProcessFunction.TopicSinkConfig;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchiveYcyKafkaLogToHdfs {
    private static final Logger LOG = LoggerFactory.getLogger(ArchiveYcyKafkaLogToHdfs.class);

    private static void archiveYcyKafkaLogToHdfs(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(600000); // 10min

        String topics = params.getRequired("topics");
        String basePath = params.get("basePath", "hdfs://gy-cluster8/user/avg/kafka/");
        KafkaBootstrapServersEnum kafkaCluster =
                KafkaBootstrapServersEnum.valueOf(params.get("kafkaCluster", "YCY_GUIANSERVER"));

        List<String> topicList =
                Arrays.stream(topics.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());

        // Check for unregistered topics
        Map<String, TopicSinkConfig<?>> registry = ArchiveYcyKafkaLogProcessFunction.getRegistry();
        List<String> unregisteredTopics =
                topicList.stream()
                        .filter(t -> !registry.containsKey(t))
                        .collect(Collectors.toList());
        if (!unregisteredTopics.isEmpty()) {
            LOG.warn(
                    "The following topics have no typed schema and will be archived as raw JSON: {}",
                    unregisteredTopics);
        }

        // Build Kafka source with metadata extraction
        KafkaSource<Tuple3<String, Long, String>> kafkaSource =
                new KafkaBaseSource(
                                KafkaConfig.builder()
                                        .setBootstrapServers(
                                                ClusterConfigOptions.getKafkaBootStrapServers(
                                                        kafkaCluster))
                                        .setProperties(
                                                ClusterConfigOptions.getKafkaSecurityProperty(
                                                        kafkaCluster))
                                        .setTopics(topics)
                                        .setGroupId("ArchiveYcyKafkaLogToHdfs")
                                        .build())
                        .createLogSourceWithMetadata();

        // Source -> ProcessFunction
        SingleOutputStreamOperator<Object> mainStream =
                env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "YcyKafkaLogSource")
                        .setParallelism(4)
                        .uid("YcyKafkaLogKafkaSource")
                        .name("YcyKafkaLogKafkaSource")
                        .process(new ArchiveYcyKafkaLogProcessFunction())
                        .setParallelism(4)
                        .uid("YcyKafkaLogProcessFunction")
                        .name("YcyKafkaLogProcessFunction");

        // SideOutput -> typed FileSink for registered topics
        for (String topic : topicList) {
            TopicSinkConfig<?> config = registry.get(topic);
            if (config != null) {
                addTypedSink(mainStream, config, basePath, topic);
            }
        }

        // Fallback FileSink for unregistered topics
        if (!unregisteredTopics.isEmpty()) {
            FileSink<ArchiveFormatRow> fallbackSink =
                    new FileSystemCommonSink(basePath)
                            .createArchiveFormatRowBulkSink(
                                    CompressionCodecName.GZIP,
                                    new ArchiveFormatRowBucketAssigner("'dt='yyyy-MM-dd"));
            mainStream
                    .getSideOutput(FALLBACK_TAG)
                    .sinkTo(fallbackSink)
                    .setParallelism(4)
                    .uid("FallbackFileSink")
                    .name("FallbackFileSink");
        }

        env.execute("ArchiveYcyKafkaLogToHdfs");
    }

    @SuppressWarnings("unchecked")
    private static <T extends KafkaArchiveRecord> void addTypedSink(
            SingleOutputStreamOperator<Object> mainStream,
            TopicSinkConfig<?> config,
            String basePath,
            String topicName) {
        TopicSinkConfig<T> typedConfig = (TopicSinkConfig<T>) config;
        String path = basePath.endsWith("/") ? basePath + topicName : basePath + "/" + topicName;
        FileSink<T> fileSink =
                new FileSystemCommonSink(path)
                        .createYcyKafkaLogParquetSink(
                                typedConfig.getClazz(), CompressionCodecName.GZIP);
        mainStream
                .getSideOutput(typedConfig.getTag())
                .sinkTo(fileSink)
                .setParallelism(4)
                .uid(topicName + "FileSink")
                .name(topicName + "FileSink");
    }

    public static void main(String[] args) throws Exception {
        archiveYcyKafkaLogToHdfs(ParameterTool.fromArgs(args));
    }
}
