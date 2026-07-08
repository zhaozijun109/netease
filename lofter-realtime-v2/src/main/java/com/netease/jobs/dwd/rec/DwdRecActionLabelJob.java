package com.netease.jobs.dwd.rec;

import com.netease.operator.dwd.rec.DwdRecActionLabelRichFlatMapFunction;
import com.netease.sink.KafkaProducerSink;
import com.netease.source.KafkaConsumerSource;
import com.netease.util.LoadConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Properties;

public class DwdRecActionLabelJob {
    private static final Logger LOG = LoggerFactory.getLogger(DwdRecActionLabelJob.class);

    private static void dwdRecActionLabelJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        Properties kafkaConsumerProps = new Properties();
        kafkaConsumerProps.setProperty("partition.discovery.interval.ms", "60000");

        Properties kafkaProducerProps = new Properties();

        env.fromSource(
                        new KafkaConsumerSource(
                                        params.getRequired("kafka.bootstrap.servers.rec"),
                                        params.getRequired("kafka.rec.rec_action_parse_topic"),
                                        "lofter.dwd_rec_action_label_job_test",
                                        kafkaConsumerProps)
                                .createLatestKafkaSource(),
                        WatermarkStrategy.noWatermarks(),
                        "DwdRecActionSource")
                .setParallelism(6)
                .name("DwdRecActionSource")
                .uid("DwdRecActionSource")
                .flatMap(new DwdRecActionLabelRichFlatMapFunction())
                .setParallelism(6)
                .name("DwdRecActionLabelRichFlatMapFunction")
                .uid("DwdRecActionLabelRichFlatMapFunction")
                .sinkTo(
                        new KafkaProducerSink(
                                        params.getRequired("kafka.bootstrap.servers"),
                                        params.getRequired(
                                                "kafka.lofter.dwd_rec_action_label_topic"),
                                        kafkaProducerProps)
                                .createKafkaProducerSink())
                .setParallelism(6)
                .name("DwdRecActionLabelSink")
                .uid("DwdRecActionLabelSink");

        env.execute("DwdRecActionLabelJob");
    }

    public static void main(String[] args) throws Exception {
        ParameterTool params = new LoadConfigurationUtils().loadConfiguration(args);
        dwdRecActionLabelJob(params);
    }
}
