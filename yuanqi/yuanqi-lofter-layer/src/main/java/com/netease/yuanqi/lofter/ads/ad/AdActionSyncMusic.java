package com.netease.yuanqi.lofter.ads.ad;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.wm.hubble.avro.AdxMonitorEvent;
import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.pojo.avro.ods.ClientMdaLogAvro;
import com.netease.yuanqi.common.pojo.config.KafkaConfig;
import com.netease.yuanqi.common.sink.kafka.KafkaBaseSink;
import com.netease.yuanqi.common.source.kafka.KafkaBaseSource;
import com.netease.yuanqi.lofter.pojo.ads.ad.AdActionRecord;
import com.netease.yuanqi.lofter.pojo.ads.ad.AdBidParameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
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

public class AdActionSyncMusic {
    private static final Logger LOG = LoggerFactory.getLogger(AdActionSyncMusic.class);

    public static final Set<String> AD_EVENTS =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList("ad-66", "ad-67")));

    public static void main(String[] args) throws Exception {
        syncAdActionLogToMusic(ParameterTool.fromArgs(args));
    }

    public static Double toDoubleValue(String key, CharSequence value) {
        try {
            Double result = Double.parseDouble(value.toString());
            return result;
        } catch (NumberFormatException e) {
            LOG.warn("cast field {} as double, got: {}", key, value);
            return null;
        }
    }

    private static void syncAdActionLogToMusic(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        Properties properties = new Properties();
        properties.setProperty("partition.discovery.interval.ms", "60000");

        KafkaSink<String> actionRecordSink =
                new KafkaBaseSink(
                                KafkaConfig.builder()
                                        .setBootstrapServers(
                                                ClusterConfigOptions.getKafkaBootStrapServers(
                                                        ClusterConfigOptions
                                                                .KafkaBootstrapServersEnum
                                                                .MUSIC_AD))
                                        .setTopic("ods_iad_lofter_action_record")
                                        .setProperties(properties)
                                        .build())
                        .createLogSink();

        DataStream<String> monitorEvents =
                env.fromSource(
                                new KafkaBaseSource(
                                                KafkaConfig.builder()
                                                        .setBootstrapServers(
                                                                ClusterConfigOptions
                                                                        .getKafkaBootStrapServers(
                                                                                ClusterConfigOptions
                                                                                        .KafkaBootstrapServersEnum
                                                                                        .LOFTER_DATA))
                                                        .setTopics("adx.server.avro")
                                                        .setGroupId("ad_action_sync_music")
                                                        .build())
                                        .createCommonSpecificAvroSource(AdxMonitorEvent.class),
                                WatermarkStrategy.noWatermarks(),
                                "ad-monitor-source")
                        .uid("input-ad-monitor")
                        .flatMap(
                                new RichFlatMapFunction<AdxMonitorEvent, String>() {
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
                                            AdxMonitorEvent e, Collector<String> collector)
                                            throws Exception {

                                        if (e.getRequestUuid() != null
                                                && e.getApp() != null
                                                && e.getRequestUuid().length() > 0
                                                && e.getClick() + e.getExpose() > 0
                                                && Objects.equals(
                                                        e.getApp().toString(), "LOFTER")) {
                                            AdActionRecord record = new AdActionRecord();
                                            record.setAction(e.getClick() > 0 ? "click" : "expose");
                                            record.setReqUid(e.getRequestUuid().toString());
                                            record.setTime(e.getTime());
                                            record.setOs(e.getDeviceOs().toString());
                                            record.setDspId(e.getAdSource().toString());
                                            record.setAppVersion(
                                                    e.getAppVersion() != null
                                                            ? e.getAppVersion().toString()
                                                            : null);
                                            collector.collect(
                                                    objectMapper.writeValueAsString(record));
                                        }
                                    }
                                });

        DataStream<String> clientEvents =
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
                                                        .setGroupId("ad_action_sync_music")
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
                                        Map<CharSequence, CharSequence> params = e.getParams();
                                        String eventId = e.getEventId().toString();

                                        if (AD_EVENTS.contains(eventId)) {
                                            AdActionRecord record = new AdActionRecord();

                                            for (Map.Entry<CharSequence, CharSequence> kv :
                                                    params.entrySet()) {
                                                if (Objects.equals(
                                                        kv.getKey().toString(), "req_uid")) {
                                                    record.setReqUid(
                                                            kv.getValue() != null
                                                                    ? kv.getValue().toString()
                                                                    : null);
                                                }

                                                if (Objects.equals(
                                                        kv.getKey().toString(), "ad_source")) {
                                                    record.setDspId(
                                                            kv.getValue() != null
                                                                    ? kv.getValue().toString()
                                                                    : null);
                                                }

                                                if (Objects.equals(
                                                        kv.getKey().toString(), "slotId")) {
                                                    record.setSlotId(
                                                            kv.getValue() != null
                                                                    ? kv.getValue().toString()
                                                                    : null);
                                                }

                                                if (Objects.equals(kv.getKey().toString(), "ext")
                                                        && kv.getValue() != null) {
                                                    String ext = kv.getValue().toString();
                                                    if (ext != null && ext.contains("bidPrice")) {
                                                        try {
                                                            AdBidParameter bidParameter =
                                                                    objectMapper.readValue(
                                                                            kv.getValue()
                                                                                    .toString(),
                                                                            AdBidParameter.class);
                                                            if (bidParameter != null) {
                                                                record.setBidPrice(
                                                                        toDoubleValue(
                                                                                "bidPrice",
                                                                                bidParameter
                                                                                        .getBidPrice()));
                                                                record.setBidFactor(
                                                                        toDoubleValue(
                                                                                "bidFactor",
                                                                                bidParameter
                                                                                        .getBidFactor()));
                                                            }
                                                        } catch (Exception ex) {
                                                            LOG.error("bid parameter: {}", ext);
                                                            LOG.error(
                                                                    "parse ad bid parameter error",
                                                                    ex);
                                                        }
                                                    }
                                                }
                                            }

                                            record.setTime(e.getOccurTime());
                                            String action =
                                                    Objects.equals(eventId, "ad-66")
                                                            ? "fill"
                                                            : "win";
                                            record.setAction(action);
                                            record.setOs(
                                                    e.getDeviceOs() != null
                                                            ? e.getDeviceOs().toString()
                                                            : null);
                                            record.setAppVersion(
                                                    e.getAppVersion() != null
                                                            ? e.getAppVersion().toString()
                                                            : null);

                                            if (record.getReqUid() != null
                                                    && record.getReqUid().length() > 0) {
                                                collector.collect(
                                                        objectMapper.writeValueAsString(record));
                                            }
                                        }
                                    }
                                });

        clientEvents.union(monitorEvents).sinkTo(actionRecordSink);

        env.execute("ad action sync music");
    }
}
