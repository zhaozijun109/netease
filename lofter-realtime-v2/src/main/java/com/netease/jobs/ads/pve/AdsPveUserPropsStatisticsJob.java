package com.netease.jobs.ads.pve;

import com.netease.operator.ads.pve.AdsPveUserPropsStatisticsProcessWindowFunction;
import com.netease.operator.ads.pve.AdsPveUserPropsStatisticsReduceFunction;
import com.netease.operator.ads.pve.AdsPveUserPropsStatisticsRichFlatMapFunction;
import com.netease.pojo.pve.PveRolePropsCostResult;
import com.netease.sink.KafkaProducerSink;
import com.netease.source.KafkaConsumerSource;
import com.netease.util.LoadConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.util.Properties;

/** Props cost stamina statistics. */
public class AdsPveUserPropsStatisticsJob {
    private static final Logger LOG = LoggerFactory.getLogger(AdsPveUserPropsStatisticsJob.class);

    private static void adsPveUserPropsStatisticsJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        Properties kafkaConsumerProps = new Properties();
        kafkaConsumerProps.setProperty("partition.discovery.interval.ms", "60000");

        Properties kafkaProducerProps = new Properties();

        env.fromSource(
                        new KafkaConsumerSource(
                                        params.getRequired("kafka.bootstrap.servers"),
                                        params.getRequired("kafka.lofter.dwd_binlog_ndc_topic"),
                                        "AdsPveUserPropsStatisticsJob",
                                        kafkaConsumerProps)
                                .createLatestKafkaSource(),
                        WatermarkStrategy.noWatermarks(),
                        "DwdBinlogNdcSource")
                .setParallelism(6)
                .name("AdsPveUserPropsStatisticsSourceFunction")
                .uid("AdsPveUserPropsStatisticsSourceFunction")
                .flatMap(new AdsPveUserPropsStatisticsRichFlatMapFunction())
                .setParallelism(6)
                .name("AdsPveUserPropsStatisticsRichFlatMapFunction")
                .uid("AdsPveUserPropsStatisticsRichFlatMapFunction")
                .keyBy(
                        new KeySelector<PveRolePropsCostResult, String>() {
                            @Override
                            public String getKey(PveRolePropsCostResult pveRolePropsCostResult)
                                    throws Exception {
                                return pveRolePropsCostResult.getDt()
                                        + "_"
                                        + pveRolePropsCostResult.getHour()
                                        + "_"
                                        + pveRolePropsCostResult.getRoleId();
                            }
                        })
                .window(TumblingProcessingTimeWindows.of(Time.minutes(5)))
                .reduce(
                        new AdsPveUserPropsStatisticsReduceFunction(),
                        new AdsPveUserPropsStatisticsProcessWindowFunction())
                .setParallelism(6)
                .name("AdsPveUserPropsStatisticsReduceFunction")
                .uid("AdsPveUserPropsStatisticsReduceFunction")
                .sinkTo(
                        new KafkaProducerSink(
                                        params.getRequired("kafka.bootstrap.servers.backend"),
                                        params.getRequired("kafka.lofter.pve_role_rank_topic"),
                                        kafkaProducerProps)
                                .createKafkaProducerSink())
                .setParallelism(1)
                .name("AdsPveUserPropsStatisticsSinkFunction")
                .uid("AdsPveUserPropsStatisticsSinkFunction");

        env.execute("AdsPveUserPropsStatisticsJob");
    }

    public static void main(String[] args) throws Exception {
        ParameterTool params = new LoadConfigurationUtils().loadConfiguration(args);
        adsPveUserPropsStatisticsJob(params);
    }
}
