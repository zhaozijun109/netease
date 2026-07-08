package com.netease.yuanqi.lofter.ads.ad;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.pojo.avro.ods.ClientMdaLogAvro;
import com.netease.yuanqi.common.pojo.config.KafkaConfig;
import com.netease.yuanqi.common.sink.kafka.KafkaBaseSink;
import com.netease.yuanqi.common.source.kafka.KafkaBaseSource;
import com.netease.yuanqi.lofter.pojo.ads.ad.AdC2sRecord;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdC2sSyncMusic {
    private static final Logger LOG = LoggerFactory.getLogger(AdC2sSyncMusic.class);

    public static final String AD_C2S_EVENT = "ad-317";

    public static void main(String[] args) throws Exception {
        syncAdC2sLogToMusic(ParameterTool.fromArgs(args));
    }

    private static String charSeqToString(CharSequence cs) {
        return cs != null ? cs.toString() : null;
    }

    private static void syncAdC2sLogToMusic(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        Properties properties = new Properties();
        properties.setProperty("partition.discovery.interval.ms", "60000");

        KafkaSink<String> c2sRecordSink =
                new KafkaBaseSink(
                                KafkaConfig.builder()
                                        .setBootstrapServers(
                                                ClusterConfigOptions.getKafkaBootStrapServers(
                                                        ClusterConfigOptions
                                                                .KafkaBootstrapServersEnum
                                                                .MUSIC_AD))
                                        .setTopic("ods_iad_lofter_c2s_record")
                                        .setProperties(properties)
                                        .build())
                        .createLogSink();

        DataStream<String> c2sEvents =
                env.fromSource(
                                new KafkaBaseSource(
                                                KafkaConfig.builder()
                                                        .setBootstrapServers(
                                                                ClusterConfigOptions
                                                                        .getKafkaBootStrapServers(
                                                                                ClusterConfigOptions
                                                                                        .KafkaBootstrapServersEnum
                                                                                        .LOFTER_DATA))
                                                        .setTopics("lofter.mda.online")
                                                        .setGroupId("ad_c2s_sync_music")
                                                        .build())
                                        .createCommonSpecificAvroSource(ClientMdaLogAvro.class),
                                WatermarkStrategy.noWatermarks(),
                                "mda-source")
                        .uid("input-mda")
                        .flatMap(
                                new RichFlatMapFunction<ClientMdaLogAvro, String>() {
                                    private ObjectMapper objectMapper;

                                    @Override
                                    public void open(OpenContext openContext) throws Exception {
                                        super.open(openContext);
                                        objectMapper = new ObjectMapper();
                                        objectMapper.setSerializationInclusion(
                                                JsonInclude.Include.NON_NULL);
                                    }

                                    @Override
                                    public void flatMap(
                                            ClientMdaLogAvro e, Collector<String> collector)
                                            throws Exception {
                                        if (e.getEventId() == null) {
                                            return;
                                        }
                                        String eventId = e.getEventId().toString();
                                        if (!Objects.equals(eventId, AD_C2S_EVENT)) {
                                            return;
                                        }

                                        AdC2sRecord record = new AdC2sRecord();
                                        record.setAction("c2s_log");

                                        Map<CharSequence, CharSequence> params = e.getParams();
                                        if (params != null) {
                                            for (Map.Entry<CharSequence, CharSequence> kv :
                                                    params.entrySet()) {
                                                if (kv.getKey() == null) {
                                                    continue;
                                                }
                                                String key = kv.getKey().toString();
                                                String value = charSeqToString(kv.getValue());

                                                switch (key) {
                                                    case "step_code":
                                                        record.setStepCode(value);
                                                        break;
                                                    case "result_code":
                                                        record.setResultCode(value);
                                                        break;
                                                    case "url":
                                                        record.setUrl(value);
                                                        break;
                                                    case "req_uid":
                                                        record.setReqUid(value);
                                                        break;
                                                    case "message_type":
                                                        record.setMessageType(value);
                                                        break;
                                                    case "error_description":
                                                        record.setErrorDescription(value);
                                                        break;
                                                    case "c2sNum":
                                                        record.setC2sNum(value);
                                                        break;
                                                    default:
                                                        break;
                                                }
                                            }
                                        }

                                        record.setIp(charSeqToString(e.getIp()));
                                        record.setUserid(
                                                e.getUserId() != null
                                                        ? String.valueOf(e.getUserId())
                                                        : null);
                                        record.setLogtime(
                                                e.getOccurTime() != null
                                                        ? String.valueOf(e.getOccurTime())
                                                        : null);
                                        record.setAppver(charSeqToString(e.getAppVersion()));
                                        record.setOs(charSeqToString(e.getDeviceOs()));
                                        record.setOsver(charSeqToString(e.getDeviceOsVersion()));

                                        if (record.getReqUid() != null
                                                && record.getReqUid().length() > 0) {
                                            collector.collect(
                                                    objectMapper.writeValueAsString(record));
                                        }
                                    }
                                });

        c2sEvents.sinkTo(c2sRecordSink);

        env.execute("ad c2s sync music");
    }
}
