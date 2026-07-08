package com.netease.yuanqi.unified.ods;

import com.netease.wm.hubble.avro.AdxDspEvent;
import com.netease.yuanqi.common.sink.kafka.KafkaCommonSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import com.netease.yuanqi.unified.operator.ods.ad.DspLogRichFlatMapFunction;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class AdDspEventEtl {
    private static void dspEventEtlJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);

        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        env.fromSource(
                        new KafkaCommonSource("adserver.dsp.online", "ad-dsp-etl")
                                .createLogSource(),
                        WatermarkStrategy.noWatermarks(),
                        "dsp-source")
                .setParallelism(6)
                .uid("dsp-input")
                .name("adx-dsp-online-kafka")
                .flatMap(new DspLogRichFlatMapFunction())
                .setParallelism(6)
                .uid("DspLogRichFlatMapFunction")
                .name("DspLogRichFlatMapFunction")
                .sinkTo(
                        new KafkaCommonSink("adserver.dsp.avro")
                                .createCommonSpecificAvroSink(AdxDspEvent.class))
                .setParallelism(6)
                .uid("AdDspEventKafkaSink")
                .name("AdDspEventEtlKafkaSink");

        env.execute("AdDspEventEtl");
    }

    public static void main(String[] args) throws Exception {
        dspEventEtlJob(ParameterTool.fromArgs(args));
    }
}
