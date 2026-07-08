package com.netease.jobs.ads.rec;

import com.netease.operator.ads.rec.AdsRARToHiveRichMapFunction;
import com.netease.pojo.rec.RecRequestAndRecActionLabel;
import com.netease.sink.FileSystemSink;
import com.netease.source.KafkaConsumerSource;
import com.netease.util.LoadConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Properties;

public class AdsRARToHiveJob {
    private static final Logger LOG = LoggerFactory.getLogger(AdsRARToHiveJob.class);

    private static void adsRARToHiveJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        Properties kafkaConsumerProps = new Properties();
        kafkaConsumerProps.setProperty("partition.discovery.interval.ms", "60000");

        env.fromSource(
                        new KafkaConsumerSource(
                                        params.getRequired("kafka.bootstrap.servers"),
                                        params.getRequired(
                                                "kafka.lofter.dws_rec_request_and_action_label_topic"),
                                        "lofter.ads_rar_to_hive_job_test",
                                        kafkaConsumerProps)
                                .createLatestKafkaSource(),
                        WatermarkStrategy.noWatermarks(),
                        "RecActionLabelRequestSource")
                .setParallelism(1)
                .name("AdsRARToHiveSource")
                .uid("AdsRARToHiveSource")
                .map(new AdsRARToHiveRichMapFunction())
                .setParallelism(1)
                .name("AdsRARToHiveRichMapFunction")
                .uid("AdsRARToHiveRichMapFunction")
                .sinkTo(
                        new FileSystemSink<>(
                                        RecRequestAndRecActionLabel.class,
                                        "hdfs://hz-cluster10/user/da_lofter/hive_db/lofter_tmp.db/ads_rar_to_hive_job_test",
                                        "'dt='yyyy-MM-dd")
                                .createSlothFileBucketSink())
                .setParallelism(1)
                .name("AdsRARToHiveSink")
                .uid("AdsRARToHiveSink");

        env.execute("AdsRARToHiveJob");
    }

    public static void main(String[] args) throws Exception {
        ParameterTool params = new LoadConfigurationUtils().loadConfiguration(args);
        adsRARToHiveJob(params);
    }
}
