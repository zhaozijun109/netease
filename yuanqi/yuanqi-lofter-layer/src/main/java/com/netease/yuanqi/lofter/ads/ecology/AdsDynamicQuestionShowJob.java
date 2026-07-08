package com.netease.yuanqi.lofter.ads.ecology;

import com.netease.yuanqi.common.config.ClusterConfigOptions;
import com.netease.yuanqi.common.pojo.config.KafkaConfig;
import com.netease.yuanqi.common.sink.kafka.KafkaBaseSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import com.netease.yuanqi.common.utils.DateTimeFormatterUtils;
import com.netease.yuanqi.lofter.operator.ads.ecology.question.AdsDynamicQuestionShowDayReduceFunction;
import com.netease.yuanqi.lofter.operator.ads.ecology.question.AdsDynamicQuestionShowDayRichFlatMapFunction;
import com.netease.yuanqi.lofter.operator.ads.ecology.question.AdsDynamicQuestionShowHourProcessWindowFunction;
import com.netease.yuanqi.lofter.operator.ads.ecology.question.AdsDynamicQuestionShowHourReduceFunction;
import com.netease.yuanqi.lofter.operator.ads.ecology.question.AdsDynamicQuestionShowHourRichFlatMapFunction;
import com.netease.yuanqi.lofter.operator.ads.ecology.question.AdsDynamicQuestionShowRichFlatMapFunction;
import com.netease.yuanqi.lofter.operator.ads.ecology.question.AdsDynamicQuestionShowStaticsReduceFunction;
import com.netease.yuanqi.lofter.pojo.ads.ecology.question.AskQuestion;
import com.netease.yuanqi.lofter.pojo.ads.ecology.question.DynamicQuestionShowStatistics;
import java.time.Duration;
import java.util.Properties;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdsDynamicQuestionShowJob {
    private static final Logger LOG = LoggerFactory.getLogger(AdsDynamicQuestionShowJob.class);

    private static void adsDynamicQuestionShowJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);

        Properties properties = new Properties();
        properties.setProperty("partition.discovery.interval.ms", "60000");

        DataStream<AskQuestion> askQuestionStream =
                env.fromSource(
                                new KafkaCommonSource(
                                                "lofter.binlog.online",
                                                "AdsDynamicQuestionShowJob",
                                                properties)
                                        .createLogSource(),
                                WatermarkStrategy.noWatermarks(),
                                "NdcBinlogSource")
                        .setParallelism(8)
                        .uid("NdcBinlogKafkaSource")
                        .name("NdcBinlogKafkaSource")
                        .flatMap(new AdsDynamicQuestionShowRichFlatMapFunction())
                        .setParallelism(16)
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
                        .window(TumblingProcessingTimeWindows.of(Duration.ofHours(1)))
                        .reduce(
                                new AdsDynamicQuestionShowHourReduceFunction(),
                                new AdsDynamicQuestionShowHourProcessWindowFunction())
                        .setParallelism(16)
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
                        .window(TumblingProcessingTimeWindows.of(Duration.ofMinutes(1)))
                        .reduce(new AdsDynamicQuestionShowStaticsReduceFunction())
                        .setParallelism(16)
                        .name("AdsDynamicQuestionShowStaticsHourReduceFunction")
                        .uid("AdsDynamicQuestionShowStaticsHourReduceFunction")
                        .flatMap(new AdsDynamicQuestionShowHourRichFlatMapFunction())
                        .setParallelism(16)
                        .name("AdsDynamicQuestionShowHourRichFlatMap")
                        .uid("AdsDynamicQuestionShowHourRichFlatMap");

        Properties backendKafkaProducerProps = new Properties();
        KafkaSink<String> backendCommonKafkaProducer =
                new KafkaBaseSink(
                                KafkaConfig.builder()
                                        .setBootstrapServers(
                                                ClusterConfigOptions.getKafkaBootStrapServers(
                                                        ClusterConfigOptions
                                                                .KafkaBootstrapServersEnum
                                                                .LOFTER_BACKEND))
                                        .setTopic("lofter.common.businessData")
                                        .setProperties(backendKafkaProducerProps)
                                        .build())
                        .createLogSink();

        Properties recKafkaProducerProps = new Properties();
        KafkaSink<String> recKafkaProducer =
                new KafkaBaseSink(
                                KafkaConfig.builder()
                                        .setBootstrapServers(
                                                ClusterConfigOptions.getKafkaBootStrapServers(
                                                        ClusterConfigOptions
                                                                .KafkaBootstrapServersEnum
                                                                .LOFTER_RECOMMEND))
                                        .setTopic("rec_bdms_question_reason")
                                        .setProperties(recKafkaProducerProps)
                                        .build())
                        .createLogSink();

        hourResultStream
                .sinkTo(backendCommonKafkaProducer)
                .setParallelism(4)
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
                                                DateTimeFormatterUtils.dateFormat(
                                                        dynamicQuestionShowStatistics
                                                                .getWindowEndTime());
                                        return day
                                                + "_"
                                                + dynamicQuestionShowStatistics.getTag()
                                                + "_"
                                                + dynamicQuestionShowStatistics.getQuestionId();
                                    }
                                })
                        .window(TumblingProcessingTimeWindows.of(Duration.ofDays(1)))
                        .reduce(new AdsDynamicQuestionShowDayReduceFunction())
                        .setParallelism(16)
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
                                                DateTimeFormatterUtils.dateFormat(
                                                        dynamicQuestionShowStatistics
                                                                .getWindowEndTime());
                                        return day
                                                + "_"
                                                + dynamicQuestionShowStatistics.getQuestionId();
                                    }
                                })
                        .window(TumblingProcessingTimeWindows.of(Duration.ofMinutes(1)))
                        .reduce(new AdsDynamicQuestionShowStaticsReduceFunction())
                        .setParallelism(16)
                        .name("AdsDynamicQuestionShowStaticsDayReduceFunction")
                        .uid("AdsDynamicQuestionShowStaticsDayReduceFunction")
                        .flatMap(new AdsDynamicQuestionShowDayRichFlatMapFunction())
                        .setParallelism(16)
                        .name("AdsDynamicQuestionShowDayRichFlatMap")
                        .uid("AdsDynamicQuestionShowDayRichFlatMap");

        dayResultStream
                .sinkTo(backendCommonKafkaProducer)
                .setParallelism(4)
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
        adsDynamicQuestionShowJob(ParameterTool.fromArgs(args));
    }
}
