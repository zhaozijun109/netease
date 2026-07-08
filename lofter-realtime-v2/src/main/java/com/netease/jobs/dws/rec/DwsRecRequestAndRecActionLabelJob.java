package com.netease.jobs.dws.rec;

import com.netease.operator.dws.rec.DwsRARActionLabelRichMapFunction;
import com.netease.operator.dws.rec.DwsRARProcessFunction;
import com.netease.operator.dws.rec.DwsRARReduceFunction;
import com.netease.operator.dws.rec.DwsRARRequestRichMapFunction;
import com.netease.operator.dws.rec.DwsRARRichMapFunction;
import com.netease.pojo.rec.RecActionLabel;
import com.netease.pojo.rec.RecRequest;
import com.netease.pojo.rec.RecRequestAndRecActionLabel;
import com.netease.sink.KafkaProducerSink;
import com.netease.source.KafkaConsumerSource;
import com.netease.util.LoadConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkGeneratorSupplier;
import org.apache.flink.api.common.eventtime.WatermarkOutput;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.util.Properties;

public class DwsRecRequestAndRecActionLabelJob {
    private static final Logger LOG =
            LoggerFactory.getLogger(DwsRecRequestAndRecActionLabelJob.class);

    private static void dwsRecRequestAndRecActionLabelJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        Properties kafkaConsumerProps = new Properties();
        kafkaConsumerProps.setProperty("partition.discovery.interval.ms", "60000");

        Properties kafkaProducerProps = new Properties();

        DataStream<RecRequest> recRequestDataStream =
                env.fromSource(
                                new KafkaConsumerSource(
                                                params.getRequired("kafka.bootstrap.servers"),
                                                params.getRequired(
                                                        "kafka.lofter.dwd_rec_request_topic"),
                                                "lofter.dws_rec_request_and_action_label_job_test",
                                                kafkaConsumerProps)
                                        .createLatestKafkaSource(),
                                WatermarkStrategy.noWatermarks(),
                                "RecRequestSource")
                        .setParallelism(6)
                        .name("DwsRARRequestSource")
                        .uid("DwsRARRequestSource")
                        .map(new DwsRARRequestRichMapFunction())
                        .setParallelism(6)
                        .name("DwsRARRequestRichMapFunction")
                        .uid("DwsRARRequestRichMapFunction")
                        .filter(
                                new FilterFunction<RecRequest>() {
                                    @Override
                                    public boolean filter(RecRequest recRequest) throws Exception {
                                        return recRequest.getUserId() != null
                                                && recRequest.getItemId() != null
                                                && recRequest.getRecId() != null;
                                    }
                                })
                        .setParallelism(6)
                        .name("DwsRARRequestFilterFunction")
                        .uid("DwsRARRequestFilterFunction")
                        .assignTimestampsAndWatermarks(
                                new WatermarkStrategy<RecRequest>() {
                                    @Override
                                    public WatermarkGenerator<RecRequest> createWatermarkGenerator(
                                            WatermarkGeneratorSupplier.Context context) {
                                        return new WatermarkGenerator<RecRequest>() {
                                            private long currentMaxTimestamp = Long.MIN_VALUE;

                                            @Override
                                            public void onEvent(
                                                    RecRequest recRequest,
                                                    long eventTimestamp,
                                                    WatermarkOutput watermarkOutput) {
                                                currentMaxTimestamp =
                                                        Math.max(
                                                                currentMaxTimestamp,
                                                                recRequest.getRecTime());
                                            }

                                            @Override
                                            public void onPeriodicEmit(WatermarkOutput output) {
                                                output.emitWatermark(
                                                        new Watermark(currentMaxTimestamp - 1));
                                            }
                                        };
                                    }
                                })
                        .setParallelism(6)
                        .name("DwsRARRequestAssignWatermark")
                        .uid("DwsRARRequestAssignWatermark");

        DataStream<RecActionLabel> recActionLabelDataStream =
                env.fromSource(
                                new KafkaConsumerSource(
                                                params.getRequired("kafka.bootstrap.servers"),
                                                params.getRequired(
                                                        "kafka.lofter.dwd_rec_action_label_topic"),
                                                "lofter.dws_rec_request_and_action_label_job_test",
                                                kafkaConsumerProps)
                                        .createLatestKafkaSource(),
                                WatermarkStrategy.noWatermarks(),
                                "RecActionLabelSource")
                        .setParallelism(6)
                        .name("DwsRARRecActionLabelSource")
                        .uid("DwsRARRecActionLabelSource")
                        .map(new DwsRARActionLabelRichMapFunction())
                        .setParallelism(6)
                        .name("DwsRARRecActionLabelRichMapFunction")
                        .uid("DwsRARRecActionLabelRichMapFunction")
                        .filter(
                                new FilterFunction<RecActionLabel>() {
                                    @Override
                                    public boolean filter(RecActionLabel recActionLabel)
                                            throws Exception {
                                        return recActionLabel.getUserId() != null
                                                && recActionLabel.getItemId() != null
                                                && recActionLabel.getRecId() != null;
                                    }
                                })
                        .setParallelism(6)
                        .name("DwsRARRecActionLabelFilterFunction")
                        .uid("DwsRARRecActionLabelFilterFunction")
                        .assignTimestampsAndWatermarks(
                                new WatermarkStrategy<RecActionLabel>() {
                                    @Override
                                    public WatermarkGenerator<RecActionLabel>
                                            createWatermarkGenerator(
                                                    WatermarkGeneratorSupplier.Context context) {
                                        return new WatermarkGenerator<RecActionLabel>() {
                                            private final long maxOutOfOrderness = 5000L; // 5s

                                            private long currentMaxTimestamp;

                                            @Override
                                            public void onEvent(
                                                    RecActionLabel recActionLabel,
                                                    long eventTimestamp,
                                                    WatermarkOutput watermarkOutput) {
                                                currentMaxTimestamp =
                                                        Math.max(
                                                                currentMaxTimestamp,
                                                                recActionLabel.getLastActionTime());
                                            }

                                            @Override
                                            public void onPeriodicEmit(WatermarkOutput output) {
                                                output.emitWatermark(
                                                        new Watermark(
                                                                currentMaxTimestamp
                                                                        - maxOutOfOrderness
                                                                        - 1));
                                            }
                                        };
                                    }
                                })
                        .setParallelism(6)
                        .name("DwsRARRecActionLabelAssignWatermark")
                        .uid("DwsRARRecActionLabelAssignWatermark");

        recRequestDataStream
                .keyBy(
                        new KeySelector<RecRequest, String>() {
                            @Override
                            public String getKey(RecRequest recRequest) throws Exception {
                                return recRequest.getUserId()
                                        + "_"
                                        + recRequest.getItemId()
                                        + "_"
                                        + recRequest.getRecId();
                            }
                        })
                .intervalJoin(
                        recActionLabelDataStream.keyBy(
                                new KeySelector<RecActionLabel, String>() {
                                    @Override
                                    public String getKey(RecActionLabel recActionLabel)
                                            throws Exception {
                                        return recActionLabel.getUserId()
                                                + "_"
                                                + recActionLabel.getItemId()
                                                + "_"
                                                + recActionLabel.getRecId();
                                    }
                                }))
                .inEventTime()
                .between(Time.minutes(-1), Time.minutes(10))
                .process(new DwsRARProcessFunction())
                .setParallelism(24)
                .name("DwsRARProcessFunction")
                .uid("DwsRARProcessFunction")
                .keyBy(
                        new KeySelector<RecRequestAndRecActionLabel, String>() {
                            @Override
                            public String getKey(
                                    RecRequestAndRecActionLabel recRequestAndActionLabel)
                                    throws Exception {
                                return recRequestAndActionLabel.getUserId()
                                        + "_"
                                        + recRequestAndActionLabel.getItemId()
                                        + "_"
                                        + recRequestAndActionLabel.getRecId();
                            }
                        })
                .window(TumblingEventTimeWindows.of(Time.minutes(10)))
                .reduce(new DwsRARReduceFunction())
                .setParallelism(24)
                .name("DwsRARReduceFunction")
                .uid("DwsRARReduceFunction")
                .map(new DwsRARRichMapFunction())
                .setParallelism(24)
                .name("DwsRARRichMapFunction")
                .uid("DwsRARRichMapFunction")
                .sinkTo(
                        new KafkaProducerSink(
                                        params.getRequired("kafka.bootstrap.servers"),
                                        params.getRequired(
                                                "kafka.lofter.dws_rec_request_and_action_label_topic"),
                                        kafkaProducerProps)
                                .createKafkaProducerSink())
                .setParallelism(6)
                .name("DwsRARSink")
                .uid("DwsRARSink");

        env.execute("DwsRecRequestAndRecActionLabelJob");
    }

    public static void main(String[] args) throws Exception {
        ParameterTool params = new LoadConfigurationUtils().loadConfiguration(args);
        dwsRecRequestAndRecActionLabelJob(params);
    }
}
