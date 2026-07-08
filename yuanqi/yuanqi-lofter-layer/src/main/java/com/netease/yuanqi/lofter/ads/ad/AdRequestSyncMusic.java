package com.netease.yuanqi.lofter.ads.ad;

import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.pojo.config.KafkaConfig;
import com.netease.yuanqi.common.sink.kafka.KafkaBaseSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import com.netease.yuanqi.lofter.operator.ads.ad.AdRequestFlatMapFunction;
import com.netease.yuanqi.lofter.operator.ads.ad.AdRequestToStringFunction;
import com.netease.yuanqi.lofter.pojo.ads.ad.AdRequestRecord;
import java.util.Properties;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class AdRequestSyncMusic {

    public static void main(String[] args) throws Exception {
        syncAdRequestToMusic(ParameterTool.fromArgs(args));
    }

    private static void syncAdRequestToMusic(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        Properties properties = new Properties();
        properties.setProperty("partition.discovery.interval.ms", "60000");

        KafkaSink<String> requestRecordSink =
                new KafkaBaseSink(
                                KafkaConfig.builder()
                                        .setBootstrapServers(
                                                ClusterConfigOptions.getKafkaBootStrapServers(
                                                        ClusterConfigOptions
                                                                .KafkaBootstrapServersEnum
                                                                .MUSIC_AD))
                                        .setTopic("ods_iad_lofter_request_record")
                                        .setProperties(properties)
                                        .build())
                        .createLogSink();

        DataStream<AdRequestRecord> requests =
                env.fromSource(
                                new KafkaCommonSource(
                                                "adserver.dsp.online",
                                                "ad_dsp_request_sync_music",
                                                properties)
                                        .createLogSource(),
                                WatermarkStrategy.noWatermarks(),
                                "dsp-source")
                        .uid("input-dsp")
                        .name("dsp-kafka-source")
                        .setParallelism(6)
                        .rebalance()
                        .flatMap(new AdRequestFlatMapFunction())
                        .name("process-ad-request")
                        .uid("dsp-request");

        requests.flatMap(new AdRequestToStringFunction()).sinkTo(requestRecordSink);

        env.execute("ad dsp sync music");
    }
}
