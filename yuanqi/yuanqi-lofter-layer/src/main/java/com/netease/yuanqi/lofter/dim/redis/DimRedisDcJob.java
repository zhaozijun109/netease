package com.netease.yuanqi.lofter.dim.redis;

import static org.apache.flink.streaming.connectors.redis.config.RedisValidator.REDIS_CLUSTER;
import static org.apache.flink.streaming.connectors.redis.config.RedisValidator.REDIS_COMMAND;
import static org.apache.flink.streaming.connectors.redis.config.RedisValidator.REDIS_MODE;

import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import com.netease.yuanqi.lofter.operator.dim.redis.DimRedisDcRichFlatMapFunction;
import java.util.Arrays;
import java.util.List;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.redis.command.RedisCommand;
import org.apache.flink.streaming.connectors.redis.config.FlinkClusterConfig;
import org.apache.flink.streaming.connectors.redis.mapper.RedisSinkMapper;
import org.apache.flink.streaming.connectors.redis.mapper.RowRedisSinkMapper;
import org.apache.flink.streaming.connectors.redis.table.RedisSinkFunction;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.catalog.ResolvedSchema;
import org.apache.flink.table.data.binary.BinaryRowData;
import org.apache.flink.table.types.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DimRedisDcJob {
    private static final Logger LOG = LoggerFactory.getLogger(DimRedisDcJob.class);

    private static void dimRedisDcJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);
        // env.disableOperatorChaining();

        Configuration configuration = new Configuration();
        configuration.setString(REDIS_MODE, REDIS_CLUSTER);
        configuration.setString(REDIS_COMMAND, RedisCommand.SET.name());
        // configuration.setInteger(TTL, 10000); // second

        RedisSinkMapper redisMapper = new RowRedisSinkMapper(RedisCommand.SET, configuration);
        FlinkClusterConfig redisClusterConfig =
                new FlinkClusterConfig.Builder()
                        .setNodesInfo(
                                ClusterConfigOptions.getRedisHosts(
                                        ClusterConfigOptions.RedisHostsEnum.COMMON))
                        .setPassword(
                                ClusterConfigOptions.getRedisAuthUserAndPass(
                                                ClusterConfigOptions.RedisHostsEnum.COMMON)
                                        .f1)
                        .build();

        List<String> columnNames = Arrays.asList("activityCode", "activityId");
        List<DataType> columnDataTypes = Arrays.asList(DataTypes.STRING(), DataTypes.BIGINT());
        ResolvedSchema resolvedSchema = ResolvedSchema.physical(columnNames, columnDataTypes);

        env.fromSource(
                        new KafkaCommonSource("lofter.binlog.online", "DimRedisDcJob")
                                .createLogSource(),
                        WatermarkStrategy.noWatermarks(),
                        "DimRedisDcSource")
                .setParallelism(1)
                .uid("DimRedisDcKafkaSource")
                .name("DimRedisDcKafkaSource")
                .flatMap(new DimRedisDcRichFlatMapFunction())
                .setParallelism(1)
                .uid("DimRedisDcRichFlatMapFunction")
                .name("DimRedisDcRichFlatMapFunction")
                .addSink(
                        new RedisSinkFunction<BinaryRowData>(
                                redisClusterConfig, redisMapper, resolvedSchema, configuration))
                .setParallelism(1)
                .uid("DimRedisDcSink")
                .name("DimRedisDcSink");

        env.execute("DimRedisDcJob");
    }

    public static void main(String[] args) throws Exception {
        dimRedisDcJob(ParameterTool.fromArgs(args));
    }
}
