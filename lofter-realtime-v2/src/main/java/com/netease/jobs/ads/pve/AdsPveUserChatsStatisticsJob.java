package com.netease.jobs.ads.pve;

import com.netease.operator.ads.pve.AdsPveUserChatsStatisticsProcessWindowFunction;
import com.netease.operator.ads.pve.AdsPveUserChatsStatisticsReduceFunction;
import com.netease.operator.ads.pve.AdsPveUserChatsStatisticsRichFlatMapFunction;
import com.netease.operator.ads.pve.AdsPveUserRoleChatsStatisticsProcessWindowFunction;
import com.netease.operator.ads.pve.AdsPveUserRoleChatsStatisticsRichFlatMapFunction;
import com.netease.pojo.pve.PveDialogueHourStatistics;
import com.netease.pojo.pve.PveRoleDialogueHourStatistics;
import com.netease.pojo.pve.PveUserRoleDialogueLogs;
import com.netease.sink.DatabaseSink;
import com.netease.sink.KafkaProducerSink;
import com.netease.source.KafkaConsumerSource;
import com.netease.util.LoadConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class AdsPveUserChatsStatisticsJob {
    private static final Logger LOG = LoggerFactory.getLogger(AdsPveUserChatsStatisticsJob.class);

    private static void adsPveUserChatsStatisticsJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        Properties kafkaConsumerProps = new Properties();
        kafkaConsumerProps.setProperty("partition.discovery.interval.ms", "60000");

        Properties kafkaProducerProps = new Properties();

        DataStream<PveUserRoleDialogueLogs> pveUserDialogueDataStream =
                env.fromSource(
                                new KafkaConsumerSource(
                                                params.getRequired("kafka.bootstrap.servers"),
                                                params.getRequired(
                                                        "kafka.lofter.dwd_binlog_ndc_topic"),
                                                "AdsPveUserChatsStatisticsJob",
                                                kafkaConsumerProps)
                                        .createLatestKafkaSource(),
                                WatermarkStrategy.noWatermarks(),
                                "DwdBinlogNdcSource")
                        .setParallelism(6)
                        .name("AdsPveUserChatsStatisticsSourceFunction")
                        .uid("AdsPveUserChatsStatisticsSourceFunction")
                        .flatMap(new AdsPveUserChatsStatisticsRichFlatMapFunction())
                        .setParallelism(6)
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
                        .window(TumblingProcessingTimeWindows.of(Time.hours(1)))
                        // .trigger(ContinuousEventTimeTrigger.of(Time.minutes(5)))
                        .process(new AdsPveUserRoleChatsStatisticsProcessWindowFunction())
                        .setParallelism(6)
                        .name("AdsPveUserRoleChatsStatisticsProcessWindowFunction")
                        .uid("AdsPveUserRoleChatsStatisticsProcessWindowFunction");

        pveRoleDialogueHourStatisticsDataStream
                .flatMap(new AdsPveUserRoleChatsStatisticsRichFlatMapFunction())
                .setParallelism(6)
                .name("AdsPveUserRoleChatsStatisticsRichFlatMapFunction")
                .uid("AdsPveUserRoleChatsStatisticsRichFlatMapFunction")
                .sinkTo(
                        new KafkaProducerSink(
                                        params.getRequired("kafka.bootstrap.servers.backend"),
                                        params.getRequired("kafka.lofter.pve_role_rank_topic"),
                                        kafkaProducerProps)
                                .createKafkaProducerSink())
                .setParallelism(1)
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
                .window(TumblingProcessingTimeWindows.of(Time.minutes(5)))
                .reduce(
                        new AdsPveUserChatsStatisticsReduceFunction(),
                        new AdsPveUserChatsStatisticsProcessWindowFunction())
                .setParallelism(6)
                .name("AdsPveUserChatsStatisticsReduceFunction")
                .uid("AdsPveUserChatsStatisticsReduceFunction")
                .addSink(
                        new DatabaseSink<PveDialogueHourStatistics>(
                                        params.getRequired("clickhouse.jdbc.driver"),
                                        params.getRequired("clickhouse.data.jdbc.url"),
                                        params.getRequired("clickhouse.data.jdbc.user"),
                                        params.getRequired("clickhouse.data.jdbc.password"))
                                .createClickhouseSink(
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
        ParameterTool params = new LoadConfigurationUtils().loadConfiguration(args);
        adsPveUserChatsStatisticsJob(params);
    }
}
