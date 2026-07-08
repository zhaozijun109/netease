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
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @deprecated Use {@link UnifiedYcyMySqlBinlogEtlJob} with --uidPrefix YcyMySqlCdc instead. This
 *     job has been replaced by UnifiedYcyMySqlBinlogEtlJob for better maintainability and
 *     flexibility. To migrate from this job with Savepoint compatibility:
 *     <pre>
 * flink run -s &lt;savepoint-path&gt; \
 *   -c com.netease.yuanqi.unified.ods.UnifiedYcyMySqlBinlogEtlJob \
 *   --tableList [your_tables] \
 *   --hostName a13-a13master-30093-slave.hz1.dumbo.nie.netease.com \
 *   --databaseName avg \
 *   --serverId 6401-6500 \
 *   --uidPrefix YcyMySqlCdc \
 *   --flatMapUid YcyCdcBinlogRichFlatMapFunction \
 *   --isSharded false \
 *   --parallelism 4
 * </pre>
 *
 * @see UnifiedYcyMySqlBinlogEtlJob
 */
@Deprecated
public class YcyMySqlBinlogEtlJob {
    public static void main(String[] args) throws Exception {
        ycyMySqlBinlogEtlJob(ParameterTool.fromArgs(args));
    }

    private static void ycyMySqlBinlogEtlJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        String tableList = params.get("tableList");
        Preconditions.checkArgument(tableList != null && !tableList.isEmpty(), "tableList is null");
        List<String> tables = Arrays.asList(tableList.split(","));

        Properties mySqlProps = new Properties();
        mySqlProps.setProperty("decimal.handling.mode", "double");

        env.fromSource(
                        new MySqlYcySource(
                                        "a13-a13master-30093-slave.hz1.dumbo.nie.netease.com",
                                        "avg",
                                        mySqlProps)
                                .createMysqlSource("6401-6500", tables),
                        WatermarkStrategy.noWatermarks(),
                        "YcyMySqlCdcBinlogSource")
                .setParallelism(4)
                .uid("YcyMySqlCdcBinlogSource")
                .name("YcyMySqlCdcBinlogSource")
                .flatMap(new CdcBinlogRichFlatMapFunction())
                .setParallelism(4)
                .uid("YcyCdcBinlogRichFlatMapFunction")
                .name("YcyCdcBinlogRichFlatMapFunction")
                .sinkTo(new KafkaYcyBinlogSink().createLogSink())
                .setParallelism(4)
                .uid("YcyMySqlCdcBinlogKafkaSink")
                .name("YcyMySqlCdcBinlogKafkaSink");

        env.execute("YcyMySqlBinlogEtlJob");
    }
}
