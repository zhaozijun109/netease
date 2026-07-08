package com.netease.jobs.dwd.binlog.ndc;

import com.netease.operator.dwd.binlog.ndc.DwdBinlogNdcRichFlatMapFunction;
import com.netease.operator.dwd.binlog.ndc.DwdBinlogNdcTransformRichFlatMapFunction;
import com.netease.sink.KafkaProducerSink;
import com.netease.source.KafkaConsumerSource;
import com.netease.util.LoadConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Properties;

/** The duplicate data of user order. */
public class DwdBinlogNdcTransformJob {
    private static final Logger LOG = LoggerFactory.getLogger(DwdBinlogNdcTransformJob.class);

    private static void dwdBinlogNdcTransformJob(ParameterTool params) throws Exception {
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
                                        params.getRequired("kafka.lofter.binlog_ndc_topic"),
                                        "DwdBinlogNdcTransformJob",
                                        kafkaConsumerProps)
                                .createBinlogNdcSource(),
                        WatermarkStrategy.noWatermarks(),
                        "KafkaBinlogNdcSource")
                .setParallelism(6)
                .name("BinlogNdcDataStreamSource")
                .uid("BinlogNdcDataStreamSource")
                .flatMap(new DwdBinlogNdcTransformRichFlatMapFunction())
                .setParallelism(6)
                .name("BinlogNdcDataStreamTransformRichFlatMap")
                .uid("BinlogNdcDataStreamTransformRichFlatMap")
                .flatMap(new DwdBinlogNdcRichFlatMapFunction())
                .setParallelism(6)
                .name("BinlogNdcDataStreamRichFlatMap")
                .uid("BinlogNdcDataStreamRichFlatMap")
                .sinkTo(
                        new KafkaProducerSink(
                                        params.getRequired("kafka.bootstrap.servers"),
                                        params.getRequired("kafka.lofter.dwd_binlog_ndc_topic"),
                                        kafkaProducerProps)
                                .createKafkaProducerSink())
                .setParallelism(6)
                .name("BinlogNdcDataStreamSink")
                .uid("BinlogNdcDataStreamSink");

        env.execute("DwdBinlogNdcTransformJob");
    }

    public static void main(String[] args) throws Exception {
        ParameterTool params = new LoadConfigurationUtils().loadConfiguration(args);
        dwdBinlogNdcTransformJob(params);
    }
}
