package com.netease.operator.ads.ecology.question;

import com.netease.pojo.ecology.question.AskQuestion;
import com.netease.pojo.ecology.question.DynamicQuestionShowStatistics;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class AdsDynamicQuestionShowHourProcessWindowFunction
        extends ProcessWindowFunction<
                AskQuestion, DynamicQuestionShowStatistics, String, TimeWindow> {

    @Override
    public void process(
            String s,
            ProcessWindowFunction<AskQuestion, DynamicQuestionShowStatistics, String, TimeWindow>
                            .Context
                    context,
            Iterable<AskQuestion> iterable,
            Collector<DynamicQuestionShowStatistics> collector)
            throws Exception {
        AskQuestion askQuestion = iterable.iterator().next();
        DynamicQuestionShowStatistics statistics = new DynamicQuestionShowStatistics();
        statistics.setQuestionId(askQuestion.getId());
        statistics.setQuestionType(askQuestion.getCosplay() == 3 ? 1 : 0);
        statistics.setBlogId(askQuestion.getUserId());
        statistics.setHourCount(
                askQuestion.getCosplay() == 3
                        ? askQuestion.getDeltaScoreCount()
                        : askQuestion.getDeltaDiscussCount());

        int tagLottieType = 0;
        // score question
        if (askQuestion.getCosplay() == 3) {
            if (askQuestion.getDeltaScoreCount() > 50) {
                tagLottieType = 3;
            } else if (askQuestion.getDeltaScoreCount() > 10
                    && askQuestion.getCreateTime()
                            >= System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000) {
                tagLottieType = 2;
            }
        } else { // question
            if (askQuestion.getDeltaDiscussCount() > 20) {
                tagLottieType = 3;
            } else if (askQuestion.getDeltaDiscussCount() > 3
                    && askQuestion.getCreateTime()
                            >= System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000) {
                tagLottieType = 2;
            }
        }
        statistics.setTagLottieType(tagLottieType);
        statistics.setWindowStartTime(context.window().getStart());
        statistics.setWindowEndTime(context.window().getEnd());
        statistics.setTag(askQuestion.getTags());
        collector.collect(statistics);
    }
}
