package com.netease.yuanqi.lofter.ads.pve;

import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.pojo.config.KafkaConfig;
import com.netease.yuanqi.common.sink.jdbc.ClickhouseSink;
import com.netease.yuanqi.common.sink.kafka.KafkaBaseSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import com.netease.yuanqi.lofter.operator.ads.pve.AdsPveUserChatsStatisticsProcessWindowFunction;
import com.netease.yuanqi.lofter.operator.ads.pve.AdsPveUserChatsStatisticsReduceFunction;
import com.netease.yuanqi.lofter.operator.ads.pve.AdsPveUserChatsStatisticsRichFlatMapFunction;
import com.netease.yuanqi.lofter.operator.ads.pve.AdsPveUserRoleChatsStatisticsProcessWindowFunction;
import com.netease.yuanqi.lofter.operator.ads.pve.AdsPveUserRoleChatsStatisticsRichFlatMapFunction;
import com.netease.yuanqi.lofter.pojo.ads.pve.PveDialogueHourStatistics;
import com.netease.yuanqi.lofter.pojo.ads.pve.PveRoleDialogueHourStatistics;
import com.netease.yuanqi.lofter.pojo.ads.pve.PveUserRoleDialogueLogs;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Properties;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdsPveUserChatsStatisticsJob {
    private static final Logger LOG = LoggerFactory.getLogger(AdsPveUserChatsStatisticsJob.class);

    private static void adsPveUserChatsStatisticsJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        Properties properties = new Properties();
        properties.setProperty("partition.discovery.interval.ms", "60000");

        DataStream<PveUserRoleDialogueLogs> pveUserDialogueDataStream =
                env.fromSource(
                                new KafkaCommonSource(
                                                "lofter.binlog.online",
                                                "AdsPveUserChatsStatisticsJob",
                                                properties)
                                        .createLogSource(),
                                WatermarkStrategy.noWatermarks(),
                                "NdcBinlogSource")
                        .setParallelism(8)
                        .uid("NdcBinlogKafkaSource")
                        .name("NdcBinlogKafkaSource")
                        .flatMap(new AdsPveUserChatsStatisticsRichFlatMapFunction())
                        .setParallelism(16)
                        .name("AdsPveUserChatsStatisticsRichFlatMapFunction")
                        .uid("AdsPveUserChatsStatisticsRichFlatMapFunction");

        DataStream<PveRoleDialogueHourStatistics> pveRoleDialogueHourStatisticsDataStream =
                pveUserDialogueDataStream
                        .keyBy(
                                new KeySelector<PveUserRoleDialogueLogs, String>() {
                                    @Override
                                    public String getKey(
                                            PveUserRoleDialogueLogs pveUserRoleDialogueLogs)
                                            throws Exception {
                                        return pveUserRoleDialogueLogs.getDt()
                                                + "_"
                                                + pveUserRoleDialogueLogs.getHour()
                                                + "_"
                                                + pveUserRoleDialogueLogs.getRoleId();
                                    }
                                })
                        .window(TumblingProcessingTimeWindows.of(Duration.ofHours(1)))
                        // .trigger(ContinuousEventTimeTrigger.of(Time.minutes(5)))
                        .process(new AdsPveUserRoleChatsStatisticsProcessWindowFunction())
                        .setParallelism(16)
                        .name("AdsPveUserRoleChatsStatisticsProcessWindowFunction")
                        .uid("AdsPveUserRoleChatsStatisticsProcessWindowFunction");

        pveRoleDialogueHourStatisticsDataStream
                .flatMap(new AdsPveUserRoleChatsStatisticsRichFlatMapFunction())
                .setParallelism(16)
                .name("AdsPveUserRoleChatsStatisticsRichFlatMapFunction")
                .uid("AdsPveUserRoleChatsStatisticsRichFlatMapFunction")
                .sinkTo(
                        new KafkaBaseSink(
                                        KafkaConfig.builder()
                                                .setBootstrapServers(
                                                        ClusterConfigOptions
                                                                .getKafkaBootStrapServers(
                                                                        ClusterConfigOptions
                                                                                .KafkaBootstrapServersEnum
                                                                                .LOFTER_BACKEND))
                                                .setTopic("LOFTER.PVEMAN.rolerank")
                                                .setProperties(properties)
                                                .build())
                                .createLogSink())
                .setParallelism(6)
                .name("AdsPveUserRoleChatsStatisticsSinkFunction")
                .uid("AdsPveUserRoleChatsStatisticsSinkFunction");

        pveRoleDialogueHourStatisticsDataStream
                .keyBy(
                        new KeySelector<PveRoleDialogueHourStatistics, String>() {
                            @Override
                            public String getKey(
                                    PveRoleDialogueHourStatistics pveRoleDialogueHourStatistics)
                                    throws Exception {
                                return pveRoleDialogueHourStatistics.getDt()
                                        + "_"
                                        + pveRoleDialogueHourStatistics.getHour();
                            }
                        })
                .window(TumblingProcessingTimeWindows.of(Duration.ofMinutes(1)))
                .reduce(
                        new AdsPveUserChatsStatisticsReduceFunction(),
                        new AdsPveUserChatsStatisticsProcessWindowFunction())
                .setParallelism(16)
                .name("AdsPveUserChatsStatisticsReduceFunction")
                .uid("AdsPveUserChatsStatisticsReduceFunction")
                .addSink(
                        new ClickhouseSink<PveDialogueHourStatistics>()
                                .createSingleHostClickhouseSink(
                                        "insert into ads_pve_user_chats_statistics(dt, hour, dialoguePv, dialogueUv) values(?,?,?,?)",
                                        new JdbcStatementBuilder<PveDialogueHourStatistics>() {
                                            @Override
                                            public void accept(
                                                    PreparedStatement preparedStatement,
                                                    PveDialogueHourStatistics
                                                            pveUserDialogueHourStatistics)
                                                    throws SQLException {
                                                preparedStatement.setString(
                                                        1, pveUserDialogueHourStatistics.getDt());
                                                preparedStatement.setInt(
                                                        2, pveUserDialogueHourStatistics.getHour());
                                                preparedStatement.setLong(
                                                        3,
                                                        pveUserDialogueHourStatistics
                                                                .getDialoguePv());
                                                preparedStatement.setLong(
                                                        4,
                                                        pveUserDialogueHourStatistics
                                                                .getDialogueUv());
                                            }
                                        },
                                        JdbcExecutionOptions.builder()
                                                .withBatchSize(100)
                                                .withBatchIntervalMs(5000)
                                                .build()))
                .setParallelism(1)
                .name("AdsPveUserChatsStatisticsSink")
                .uid("AdsPveUserChatsStatisticsSink");

        env.execute("AdsPveUserChatsStatisticsJob");
    }

    public static void main(String[] args) throws Exception {
        adsPveUserChatsStatisticsJob(ParameterTool.fromArgs(args));
    }
}
