package com.netease.yuanqi.lofter.dim.es;

import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.pojo.avro.ods.ClientMdaLogAvro;
import com.netease.yuanqi.common.pojo.config.KafkaConfig;
import com.netease.yuanqi.common.sink.es.EsCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaBaseSource;
import com.netease.yuanqi.lofter.operator.dim.es.DimDeviceRichFlatMapFunction;
import com.netease.yuanqi.lofter.pojo.DimDevice;
import java.io.IOException;
import java.util.Properties;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.elasticsearch.sink.ElasticsearchEmitter;
import org.apache.flink.connector.elasticsearch.sink.RequestIndexer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentFactory;

public class DimDeviceJob {

    private static void dimDeviceJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        Properties properties = new Properties();
        properties.setProperty("partition.discovery.interval.ms", "60000");

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
                                                .setGroupId("DimDeviceJob")
                                                .setProperties(properties)
                                                .build())
                                .createCommonSpecificAvroSource(ClientMdaLogAvro.class),
                        WatermarkStrategy.noWatermarks(),
                        "DimDeviceJobSource")
                .uid("DimDeviceJobSource")
                .name("DimDeviceJobSource")
                .filter(e -> e.getDeviceUdid() != null && e.getOccurTime() != null)
                .uid("DimDeviceJobFilter")
                .name("DimDeviceJobFilter")
                .keyBy(e -> e.getDeviceUdid().toString())
                .flatMap(new DimDeviceRichFlatMapFunction())
                .uid("DimDeviceRichFlatMapFunction")
                .name("DimDeviceRichFlatMapFunction")
                .sinkTo(
                        new EsCommonSink()
                                .createCommonEsSink(
                                        new ElasticsearchEmitter<DimDevice>() {
                                            @Override
                                            public void emit(
                                                    DimDevice dimDevice,
                                                    SinkWriter.Context context,
                                                    RequestIndexer requestIndexer) {
                                                try {
                                                    UpdateRequest updateRequest =
                                                            new UpdateRequest()
                                                                    .index("lofter_dim_device_v2")
                                                                    .type("_doc")
                                                                    .id(dimDevice.getDeviceUdid())
                                                                    .doc(
                                                                            XContentFactory
                                                                                    .jsonBuilder()
                                                                                    .startObject()
                                                                                    .field(
                                                                                            "deviceUdid",
                                                                                            dimDevice
                                                                                                    .getDeviceUdid())
                                                                                    .field(
                                                                                            "latest_day_first_time",
                                                                                            dimDevice
                                                                                                    .getLatestDayFirstTime())
                                                                                    .endObject())
                                                                    .docAsUpsert(true)
                                                                    .detectNoop(false);

                                                    requestIndexer.add(updateRequest);
                                                } catch (IOException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        }))
                .uid("DimDeviceEsSink")
                .name("DimDeviceEsSink");

        env.execute("DimDeviceJob");
    }

    public static void main(String[] args) throws Exception {
        dimDeviceJob(ParameterTool.fromArgs(args));
    }
}
