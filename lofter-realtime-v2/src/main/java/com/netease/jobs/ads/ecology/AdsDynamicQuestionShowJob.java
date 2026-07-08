package com.netease.jobs.ads.ecology;

import com.netease.operator.ads.ecology.question.AdsDynamicQuestionShowDayReduceFunction;
import com.netease.operator.ads.ecology.question.AdsDynamicQuestionShowDayRichFlatMapFunction;
import com.netease.operator.ads.ecology.question.AdsDynamicQuestionShowHourProcessWindowFunction;
import com.netease.operator.ads.ecology.question.AdsDynamicQuestionShowHourReduceFunction;
import com.netease.operator.ads.ecology.question.AdsDynamicQuestionShowHourRichFlatMapFunction;
import com.netease.operator.ads.ecology.question.AdsDynamicQuestionShowRichFlatMapFunction;
import com.netease.operator.ads.ecology.question.AdsDynamicQuestionShowStaticsReduceFunction;
import com.netease.pojo.ecology.question.AskQuestion;
import com.netease.pojo.ecology.question.DynamicQuestionShowStatistics;
import com.netease.sink.KafkaProducerSink;
import com.netease.source.KafkaConsumerSource;
import com.netease.util.DateTimeFormatterUtils;
import com.netease.util.LoadConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.util.Properties;

public class AdsDynamicQuestionShowJob {
    private static final Logger LOG = LoggerFactory.getLogger(AdsDynamicQuestionShowJob.class);

    private static void adsDynamicQuestionShowJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        Properties kafkaConsumerProps = new Properties();
        kafkaConsumerProps.setProperty("partition.discovery.interval.ms", "60000");

        DataStream<AskQuestion> askQuestionStream =
                env.fromSource(
                                new KafkaConsumerSource(
                                                params.getRequired("kafka.bootstrap.servers"),
                                                params.getRequired(
                                                        "kafka.lofter.dwd_binlog_ndc_topic"),
                                                "AdsDynamicQuestionShowJob",
                                                kafkaConsumerProps)
                                        .createLatestKafkaSource(),
                                WatermarkStrategy.noWatermarks(),
                                "DwdBinlogNdcSource")
                        .setParallelism(6)
                        .name("AdsDynamicQuestionShowSource")
                        .uid("AdsDynamicQuestionShowSource")
                        .flatMap(new AdsDynamicQuestionShowRichFlatMapFunction())
                        .setParallelism(12)
                        .name("AdsDynamicQuestionShowFlatMap")
                        .uid("AdsDynamicQuestionShowFlatMap");

        DataStream<DynamicQuestionShowStatistics> questionHourStatisticsStream =
                askQuestionStream
                        .keyBy(
                                new KeySelector<AskQuestion, String>() {
                                    @Override
                                    public String getKey(AskQuestion askQuestion) throws Exception {
                                        return askQuestion.getTags() + "_" + askQuestion.getId();
                                    }
                                })
                        .window(TumblingProcessingTimeWindows.of(Time.hours(1)))
                        .reduce(
                                new AdsDynamicQuestionShowHourReduceFunction(),
                                new AdsDynamicQuestionShowHourProcessWindowFunction())
                        .setParallelism(12)
                        .name("AdsDynamicQuestionShowHourReduceFunction")
                        .uid("AdsDynamicQuestionShowHourReduceFunction");

        DataStream<String> hourResultStream =
                questionHourStatisticsStream
                        .keyBy(
                                new KeySelector<DynamicQuestionShowStatistics, String>() {
                                    @Override
                                    public String getKey(
                                            DynamicQuestionShowStatistics
                                                    dynamicQuestionShowStatistics)
                                            throws Exception {
                                        return dynamicQuestionShowStatistics.getWindowEndTime()
                                                + "_"
                                                + dynamicQuestionShowStatistics.getQuestionId();
                                    }
                                })
                        .window(TumblingProcessingTimeWindows.of(Time.minutes(1)))
                        .reduce(new AdsDynamicQuestionShowStaticsReduceFunction())
                        .setParallelism(6)
                        .name("AdsDynamicQuestionShowStaticsHourReduceFunction")
                        .uid("AdsDynamicQuestionShowStaticsHourReduceFunction")
                        .flatMap(new AdsDynamicQuestionShowHourRichFlatMapFunction())
                        .setParallelism(6)
                        .name("AdsDynamicQuestionShowHourRichFlatMap")
                        .uid("AdsDynamicQuestionShowHourRichFlatMap");

        Properties backendKafkaProducerProps = new Properties();
        KafkaSink<String> backendCommonKafkaProducer =
                new KafkaProducerSink(
                                params.getRequired("kafka.bootstrap.servers.backend"),
                                params.getRequired("kafka.lofter.backend_common_topic"),
                                backendKafkaProducerProps)
                        .createKafkaProducerSink();

        Properties recKafkaProducerProps = new Properties();
        KafkaSink<String> recKafkaProducer =
                new KafkaProducerSink(
                                params.getRequired("kafka.bootstrap.servers.rec"),
                                params.getRequired("kafka.rec.rec_question_reason_topic"),
                                recKafkaProducerProps)
                        .createKafkaProducerSink();

        hourResultStream
                .sinkTo(backendCommonKafkaProducer)
                .setParallelism(6)
                .name("AdsDynamicQuestionShowBackendHourSink")
                .uid("AdsDynamicQuestionShowBackendHourSink");

        hourResultStream
                .sinkTo(recKafkaProducer)
                .setParallelism(6)
                .name("AdsDynamicQuestionShowRecHourSink")
                .uid("AdsDynamicQuestionShowRecHourSink");

        // day
        DataStream<String> dayResultStream =
                questionHourStatisticsStream
                        .keyBy(
                                new KeySelector<DynamicQuestionShowStatistics, String>() {
                                    @Override
                                    public String getKey(
                                            DynamicQuestionShowStatistics
                                                    dynamicQuestionShowStatistics)
                                            throws Exception {
                                        String day =
                                                new DateTimeFormatterUtils()
                                                        .dateFormat(
                                                                dynamicQuestionShowStatistics
                                                                        .getWindowEndTime());
                                        return day
                                                + "_"
                                                + dynamicQuestionShowStatistics.getTag()
                                                + "_"
                                                + dynamicQuestionShowStatistics.getQuestionId();
                                    }
                                })
                        .window(TumblingProcessingTimeWindows.of(Time.days(1)))
                        .reduce(new AdsDynamicQuestionShowDayReduceFunction())
                        .setParallelism(12)
                        .name("AdsDynamicQuestionShowDayReduceFunction")
                        .uid("AdsDynamicQuestionShowDayReduceFunction")
                        .keyBy(
                                new KeySelector<DynamicQuestionShowStatistics, String>() {
                                    @Override
                                    public String getKey(
                                            DynamicQuestionShowStatistics
                                                    dynamicQuestionShowStatistics)
                                            throws Exception {
                                        String day =
                                                new DateTimeFormatterUtils()
                                                        .dateFormat(
                                                                dynamicQuestionShowStatistics
                                                                        .getWindowEndTime());
                                        return day
                                                + "_"
                                                + dynamicQuestionShowStatistics.getQuestionId();
                                    }
                                })
                        .window(TumblingProcessingTimeWindows.of(Time.minutes(1)))
                        .reduce(new AdsDynamicQuestionShowStaticsReduceFunction())
                        .setParallelism(6)
                        .name("AdsDynamicQuestionShowStaticsDayReduceFunction")
                        .uid("AdsDynamicQuestionShowStaticsDayReduceFunction")
                        .flatMap(new AdsDynamicQuestionShowDayRichFlatMapFunction())
                        .setParallelism(6)
                        .name("AdsDynamicQuestionShowDayRichFlatMap")
                        .uid("AdsDynamicQuestionShowDayRichFlatMap");

        dayResultStream
                .sinkTo(backendCommonKafkaProducer)
                .setParallelism(6)
                .name("AdsDynamicQuestionShowBackendDaySink")
                .uid("AdsDynamicQuestionShowBackendDaySink");

        dayResultStream
                .sinkTo(recKafkaProducer)
                .setParallelism(6)
                .name("AdsDynamicQuestionShowRecDaySink")
                .uid("AdsDynamicQuestionShowRecDaySink");

        env.execute("AdsDynamicQuestionShowJob");
    }

    public static void main(String[] args) throws Exception {
        ParameterTool params = new LoadConfigurationUtils().loadConfiguration(args);
        adsDynamicQuestionShowJob(params);
    }
}
