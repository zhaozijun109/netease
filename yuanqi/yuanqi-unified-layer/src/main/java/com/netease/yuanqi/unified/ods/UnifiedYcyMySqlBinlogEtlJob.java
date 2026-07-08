package com.netease.yuanqi.unified.ods;

import com.netease.yuanqi.common.sink.kafka.KafkaYcyBinlogSink;
import com.netease.yuanqi.common.source.mysql.MySqlYcySource;
import com.netease.yuanqi.common.utils.Preconditions;
import com.netease.yuanqi.unified.operator.ods.binlog.CdcBinlogRichFlatMapFunction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * Unified YCY MySQL Binlog ETL Job
 *
 * <p>This is a unified, configurable job that replaces the following deprecated jobs:
 *
 * <ul>
 *   <li>{@link YcyMySqlBinlogEtlJob} - Non-sharded tables
 *   <li>{@link YcyMySqlShardBinlogEtlJob} - Sharded tables with primary key rewriting
 *   <li>{@link YcyMySqlOtherBinlogEtlJob} - Multiple data sources
 * </ul>
 *
 * <p><b>Key features:</b>
 *
 * <ul>
 *   <li>Configurable data sources (supports multiple sources with independent sharding config)
 *   <li>Savepoint compatibility via --uidPrefix and --flatMapUid parameters
 *   <li>Per-source sharding support via --isSharded, --isSharded2, --isSharded3, etc.
 *   <li>Legacy mode for migrating from YcyMySqlOtherBinlogEtlJob
 *   <li>Configurable parallelism and checkpoint settings
 * </ul>
 *
 * <p><b>Required parameters:</b>
 *
 * <ul>
 *   <li>--tableList: Table names (comma-separated)
 *   <li>--hostName: MySQL hostname
 *   <li>--databaseName: Database name (supports regex like avg_[0-9]+)
 *   <li>--serverId: Server ID range (e.g., 6401-6500)
 *   <li>--uidPrefix: UID prefix for Savepoint compatibility
 * </ul>
 *
 * <p><b>Optional parameters:</b>
 *
 * <ul>
 *   <li>--isSharded: Whether primary data source uses sharded tables (true/false, default: false)
 *   <li>--parallelism: Operator parallelism (default: 4)
 *   <li>--checkpointInterval: Checkpoint interval in ms (default: 120000)
 *   <li>--maxParallelism: Max parallelism (default: 256)
 *   <li>--legacyMode: Legacy mode for YcyMySqlOtherBinlogEtlJob migration (true/false, default:
 *       false)
 *   <li>--flatMapUid: Manual FlatMap UID override for compatibility (optional)
 *   <li>--hostName2, --databaseName2, --serverId2, --tableList2, --isSharded2: Second data source
 *   <li>--hostName3, --databaseName3, --serverId3, --tableList3, --isSharded3: Third data source
 *   <li>...up to 10 data sources
 * </ul>
 *
 * <p><b>Migration Examples:</b>
 *
 * <pre>
 * # From YcyMySqlBinlogEtlJob:
 * flink run -s &lt;savepoint&gt; \
 *   -c com.netease.yuanqi.unified.ods.UnifiedYcyMySqlBinlogEtlJob \
 *   --tableList user,order \
 *   --hostName a13-a13master-30093-slave.hz1.dumbo.nie.netease.com \
 *   --databaseName avg \
 *   --serverId 6401-6500 \
 *   --uidPrefix YcyMySqlCdc \
 *   --flatMapUid YcyCdcBinlogRichFlatMapFunction \
 *   --isSharded false
 *
 * # From YcyMySqlShardBinlogEtlJob:
 * flink run -s &lt;savepoint&gt; \
 *   -c com.netease.yuanqi.unified.ods.UnifiedYcyMySqlBinlogEtlJob \
 *   --tableList user,order \
 *   --hostName a13-a13shard-120206-s1.hz3.dumbo.nie.netease.com \
 *   --databaseName "avg_[0-9]+" \
 *   --serverId 6401-6500 \
 *   --uidPrefix YcyMySqlShard \
 *   --flatMapUid YcyCdcBinlogRichFlatMapFunction \
 *   --isSharded true
 *
 * # From YcyMySqlOtherBinlogEtlJob (with two sources):
 * flink run -s &lt;savepoint&gt; \
 *   -c com.netease.yuanqi.unified.ods.UnifiedYcyMySqlBinlogEtlJob \
 *   --tableList game_device_play_record \
 *   --hostName a13-a13shard-120206-s1.hz3.dumbo.nie.netease.com \
 *   --databaseName avg \
 *   --serverId 6501-6600 \
 *   --uidPrefix YcyMySqlOther \
 *   --isSharded false \
 *   --hostName2 a13-a13master-30093-slave.hz1.dumbo.nie.netease.com \
 *   --databaseName2 avg_statistics \
 *   --serverId2 6601-6700 \
 *   --tableList2 game_statistics_first_click \
 *   --isSharded2 false \
 *   --legacyMode true
 * </pre>
 *
 * @see YcyMySqlBinlogEtlJob
 * @see YcyMySqlShardBinlogEtlJob
 * @see YcyMySqlOtherBinlogEtlJob
 */
public class UnifiedYcyMySqlBinlogEtlJob {
    public static void main(String[] args) throws Exception {
        unifiedYcyMySqlBinlogEtlJob(ParameterTool.fromArgs(args));
    }

    // @SuppressWarnings("unchecked")
    private static void unifiedYcyMySqlBinlogEtlJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(600000); // 10min

        int parallelism = params.getInt("parallelism", 4);
        String tableList = params.get("tableList");
        String hostName = params.get("hostName");
        String databaseName = params.get("databaseName");
        String serverId = params.get("serverId");
        String uidPrefix = params.get("uidPrefix");

        Preconditions.checkArgument(
                tableList != null && !tableList.isEmpty(), "tableList is required");
        Preconditions.checkArgument(
                hostName != null && !hostName.isEmpty(), "hostName is required");
        Preconditions.checkArgument(
                databaseName != null && !databaseName.isEmpty(), "databaseName is required");
        Preconditions.checkArgument(
                serverId != null && !serverId.isEmpty(), "serverId is required");
        Preconditions.checkArgument(
                uidPrefix != null && !uidPrefix.isEmpty(),
                "uidPrefix is required for Savepoint compatibility");

        List<String> tables = Arrays.asList(tableList.split(","));

        boolean isSharded = params.getBoolean("isSharded", false);
        boolean legacyMode = params.getBoolean("legacyMode", false);
        String flatMapUidOverride = params.get("flatMapUid");

        Properties mySqlProps = new Properties();
        mySqlProps.setProperty("decimal.handling.mode", "double");

        DataStream<String> source1 =
                env.fromSource(
                                new MySqlYcySource(hostName, databaseName, mySqlProps)
                                        .createMysqlSource(serverId, tables),
                                WatermarkStrategy.noWatermarks(),
                                uidPrefix + "BinlogSource")
                        .setParallelism(parallelism)
                        .uid(uidPrefix + "BinlogSource")
                        .name(uidPrefix + "BinlogSource");

        List<DataStream<String>> additionalSources = new ArrayList<>();

        // Detect and process additional data sources (hostName2, hostName3, ...)
        for (int i = 2; i <= 10; i++) { // Support up to 10 data sources
            String hostNameN = params.get("hostName" + i);
            if (hostNameN == null || hostNameN.isEmpty()) {
                break; // No more data sources
            }

            String databaseNameN = params.get("databaseName" + i);
            String serverIdN = params.get("serverId" + i);
            String tableListN = params.get("tableList" + i);

            Preconditions.checkArgument(
                    databaseNameN != null && !databaseNameN.isEmpty(),
                    "databaseName" + i + " is required when hostName" + i + " is set");
            Preconditions.checkArgument(
                    serverIdN != null && !serverIdN.isEmpty(),
                    "serverId" + i + " is required when hostName" + i + " is set");
            Preconditions.checkArgument(
                    tableListN != null && !tableListN.isEmpty(),
                    "tableList" + i + " is required when hostName" + i + " is set");

            List<String> tablesN = Arrays.asList(tableListN.split(","));

            DataStream<String> sourceN =
                    env.fromSource(
                                    new MySqlYcySource(hostNameN, databaseNameN, mySqlProps)
                                            .createMysqlSource(serverIdN, tablesN),
                                    WatermarkStrategy.noWatermarks(),
                                    uidPrefix + "BinlogSource" + i)
                            .setParallelism(parallelism)
                            .uid(uidPrefix + "BinlogSource" + i)
                            .name(uidPrefix + "BinlogSource" + i);

            additionalSources.add(sourceN);
        }

        DataStream<String> mergedStream;

        if (legacyMode) {
            // Legacy Mode: Union first, then single FlatMap
            // This is for compatibility with YcyMySqlOtherBinlogEtlJob
            DataStream<String> mergedSource = source1;
            if (!additionalSources.isEmpty()) {
                mergedSource = source1.union(additionalSources.toArray(new DataStream[0]));
            }

            // Determine FlatMap UID for legacy compatibility
            String flatMapUid;
            if (flatMapUidOverride != null && !flatMapUidOverride.isEmpty()) {
                flatMapUid = flatMapUidOverride;
            } else if ("YcyMySqlOther".equals(uidPrefix)) {
                flatMapUid = "YcyCdcOtherBinlogRichFlatMapFunction";
            } else {
                flatMapUid = "YcyCdcBinlogRichFlatMapFunction";
            }

            mergedStream =
                    mergedSource
                            .flatMap(new CdcBinlogRichFlatMapFunction(isSharded))
                            .setParallelism(parallelism)
                            .uid(flatMapUid)
                            .name(flatMapUid);
        } else {
            // Modern Mode: Each source gets its own FlatMap, then union
            // Determine FlatMap UID
            String flatMapUid1;
            if (flatMapUidOverride != null && !flatMapUidOverride.isEmpty()) {
                flatMapUid1 = flatMapUidOverride;
            } else {
                flatMapUid1 = uidPrefix + "BinlogFlatMap";
            }

            DataStream<String> stream1 =
                    source1.flatMap(new CdcBinlogRichFlatMapFunction(isSharded))
                            .setParallelism(parallelism)
                            .uid(flatMapUid1)
                            .name(flatMapUid1);

            List<DataStream<String>> processedStreams = new ArrayList<>();
            processedStreams.add(stream1);

            // Process additional sources with their own isSharded config
            for (int i = 0; i < additionalSources.size(); i++) {
                int sourceIndex = i + 2; // Source index starts from 2
                boolean isShardedN = params.getBoolean("isSharded" + sourceIndex, false);
                String flatMapUidN = uidPrefix + "BinlogFlatMap" + sourceIndex;

                DataStream<String> streamN =
                        additionalSources
                                .get(i)
                                .flatMap(new CdcBinlogRichFlatMapFunction(isShardedN))
                                .setParallelism(parallelism)
                                .uid(flatMapUidN)
                                .name(flatMapUidN);

                processedStreams.add(streamN);
            }

            // Union all processed streams
            if (processedStreams.size() == 1) {
                mergedStream = processedStreams.get(0);
            } else {
                mergedStream =
                        processedStreams
                                .get(0)
                                .union(
                                        processedStreams
                                                .subList(1, processedStreams.size())
                                                .toArray(new DataStream[0]));
            }
        }

        mergedStream
                .sinkTo(new KafkaYcyBinlogSink().createLogSink())
                .setParallelism(parallelism)
                .uid(uidPrefix + "BinlogKafkaSink")
                .name(uidPrefix + "BinlogKafkaSink");

        env.execute("UnifiedYcyMySqlBinlogEtlJob-" + uidPrefix);
    }
}
