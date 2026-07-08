package com.netease.operator.ads.ecology.question;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.pojo.KafkaPayloadResult;
import com.netease.pojo.ecology.question.DynamicQuestionShowStatistics;
import com.netease.pojo.ecology.question.DynamicQuestionShowStatisticsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

public class AdsDynamicQuestionShowHourRichFlatMapFunction
        extends RichFlatMapFunction<DynamicQuestionShowStatistics, String> {
    private static final Logger LOG =
            LoggerFactory.getLogger(AdsDynamicQuestionShowHourRichFlatMapFunction.class);
    private ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void flatMap(DynamicQuestionShowStatistics statistics, Collector<String> collector)
            throws Exception {
        DynamicQuestionShowStatisticsResult statisticsResult =
                new DynamicQuestionShowStatisticsResult();
        statisticsResult.setItemId(statistics.getQuestionId());
        statisticsResult.setItemType(1);
        statisticsResult.setBlogId(statistics.getBlogId());
        statisticsResult.setRecReason(statistics.getQuestionType() == 1 ? "刚刚有人评过" : "刚刚有新讨论");
        statisticsResult.setPriority(statistics.getQuestionType() == 1 ? 1 : 2);
        statisticsResult.setTagLottieType(statistics.getTagLottieType());
        statisticsResult.setWeight(statistics.getHourCount());

        KafkaPayloadResult kafkaPayloadResult = new KafkaPayloadResult();
        kafkaPayloadResult.setMessageType(6);
        kafkaPayloadResult.setPayload(statisticsResult);

        if ((statistics.getHourCount() > 10 && statistics.getQuestionType() == 1)
                || (statistics.getHourCount() > 5 && statistics.getQuestionType() == 0)) {
            collector.collect(objectMapper.writeValueAsString(kafkaPayloadResult));
        }
    }
}
