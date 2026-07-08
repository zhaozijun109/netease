package com.netease.jobs.dwd.rec;

import com.netease.operator.dwd.rec.DwdRecActionRichFlatMapFunction;
import com.netease.sink.KafkaProducerSink;
import com.netease.source.KafkaConsumerSource;
import com.netease.util.LoadConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Properties;

public class DwdRecActionJob {
    private static final Logger LOG = LoggerFactory.getLogger(DwdRecActionJob.class);

    private static void dwdRecActionJob(ParameterTool params) throws Exception {
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
                                        params.getRequired("kafka.rec.rec_action_topic"),
                                        "lofter.dwd_rec_action_job_test",
                                        kafkaConsumerProps)
                                .createLatestKafkaSource(),
                        WatermarkStrategy.noWatermarks(),
                        "RecUploadActionSource")
                .setParallelism(1)
                .name("DwdRecActionSource")
                .uid("DwdRecActionSource")
                .flatMap(new DwdRecActionRichFlatMapFunction())
                .setParallelism(1)
                .name("DwdRecActionRichFlatMapFunction")
                .uid("DwdRecActionRichFlatMapFunction")
                .sinkTo(
                        new KafkaProducerSink(
                                        params.getRequired("kafka.bootstrap.servers"),
                                        params.getRequired("kafka.lofter.dwd_rec_action_topic"),
                                        kafkaProducerProps)
                                .createKafkaProducerSink())
                .setParallelism(1)
                .name("DwdRecActionSink")
                .uid("DwdRecActionSink");

        env.execute("DwdRecActionJob");
    }

    public static void main(String[] args) throws Exception {
        ParameterTool params = new LoadConfigurationUtils().loadConfiguration(args);
        dwdRecActionJob(params);
    }
}
