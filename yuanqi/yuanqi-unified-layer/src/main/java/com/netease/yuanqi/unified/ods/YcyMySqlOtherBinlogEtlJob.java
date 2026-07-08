package com.netease.yuanqi.unified.ods;

import com.netease.yuanqi.common.sink.kafka.KafkaYcyBinlogSink;
import com.netease.yuanqi.common.source.mysql.MySqlYcySource;
import com.netease.yuanqi.common.utils.Preconditions;
import com.netease.yuanqi.unified.operator.ods.binlog.CdcBinlogRichFlatMapFunction;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @deprecated Use {@link UnifiedYcyMySqlBinlogEtlJob} with --uidPrefix YcyMySqlOther instead. This
 *     job has been replaced by UnifiedYcyMySqlBinlogEtlJob for better maintainability and
 *     flexibility. To migrate from this job with Savepoint compatibility:
 *     <pre>
 * flink run -s &lt;savepoint-path&gt; \
 *   -c com.netease.yuanqi.unified.ods.UnifiedYcyMySqlBinlogEtlJob \
 *   --tableList [tables1] \
 *   --hostName a13-a13shard-120206-s1.hz3.dumbo.nie.netease.com \
 *   --databaseName avg \
 *   --serverId 6501-6600 \
 *   --uidPrefix YcyMySqlOther \
 *   --isSharded false \
 *   --hostName2 [optional_host2] \
 *   --databaseName2 [optional_db2] \
 *   --serverId2 6601-6700 \
 *   --tableList2 [tables2] \
 *   --isSharded2 false \
 *   --legacyMode true \
 *   --parallelism 4
 * </pre>
 *     <b>Note:</b> Supports multiple data sources via --hostName2, --hostName3, etc. Use
 *     --legacyMode true for Savepoint compatibility.
 * @see UnifiedYcyMySqlBinlogEtlJob
 */
@Deprecated
public class YcyMySqlOtherBinlogEtlJob {
    public static void main(String[] args) throws Exception {
        // --tableList game_device_play_record
        // --hostName2 a13-a13master-30093-slave.hz1.dumbo.nie.netease.com
        // --databaseName2 avg_statistics
        // --tableList2 game_statistics_first_click
        ycyMySqlOtherBinlogEtlJob(ParameterTool.fromArgs(args));
    }

    private static void ycyMySqlOtherBinlogEtlJob(ParameterTool params) throws Exception {
        // 非业务库非分库分表的其他实例和表同步
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        String tableList = params.get("tableList");
        Preconditions.checkArgument(tableList != null && !tableList.isEmpty(), "tableList is null");
        List<String> tables = Arrays.asList(tableList.split(","));

        // 第二实例（可选）参数：单独的 host / db / tableList
        String hostName2 = params.get("hostName2");
        String databaseName2 = params.get("databaseName2");
        String tableList2 = params.get("tableList2");

        Properties mySqlProps = new Properties();
        mySqlProps.setProperty("decimal.handling.mode", "double");

        DataStream<String> source1 =
                env.fromSource(
                                new MySqlYcySource(
                                                "a13-a13shard-120206-s1.hz3.dumbo.nie.netease.com",
                                                "avg",
                                                mySqlProps)
                                        .createMysqlSource("6501-6600", tables),
                                WatermarkStrategy.noWatermarks(),
                                "YcyMySqlOtherBinlogSource")
                        .setParallelism(4)
                        .uid("YcyMySqlOtherBinlogSource")
                        .name("YcyMySqlOtherBinlogSource");

        // 实例 2 Source（若配置 hostName2/databaseName2/tableList2 则启用）
        DataStream<String> mergedSource;
        if (hostName2 != null && !hostName2.isEmpty()) {
            Preconditions.checkArgument(
                    databaseName2 != null && !databaseName2.isEmpty(),
                    "databaseName2 is null when hostName2 is set");
            Preconditions.checkArgument(
                    tableList2 != null && !tableList2.isEmpty(),
                    "tableList2 is null when hostName2 is set");

            List<String> tables2 = Arrays.asList(tableList2.split(","));

            DataStream<String> source2 =
                    env.fromSource(
                                    new MySqlYcySource(hostName2, databaseName2, mySqlProps)
                                            .createMysqlSource("6601-6700", tables2),
                                    WatermarkStrategy.noWatermarks(),
                                    "YcyMySqlOtherBinlogSource2")
                            .setParallelism(4)
                            .uid("YcyMySqlOtherBinlogSource2")
                            .name("YcyMySqlOtherBinlogSource2");

            mergedSource = source1.union(source2);
        } else {
            mergedSource = source1;
        }

        mergedSource
                .flatMap(new CdcBinlogRichFlatMapFunction())
                .setParallelism(4)
                .uid("YcyCdcOtherBinlogRichFlatMapFunction")
                .name("YcyCdcOtherBinlogRichFlatMapFunction")
                .sinkTo(new KafkaYcyBinlogSink().createLogSink())
                .setParallelism(4)
                .uid("YcyMySqlOtherBinlogKafkaSink")
                .name("YcyMySqlOtherBinlogKafkaSink");

        env.execute("YcyMySqlOtherBinlogEtlJob");
    }
}
