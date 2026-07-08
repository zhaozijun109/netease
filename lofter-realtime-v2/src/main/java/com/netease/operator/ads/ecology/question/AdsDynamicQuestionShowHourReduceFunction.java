package com.netease.operator.ads.ecology.question;

import com.netease.pojo.ecology.question.AskQuestion;

import org.apache.flink.api.common.functions.ReduceFunction;

public class AdsDynamicQuestionShowHourReduceFunction implements ReduceFunction<AskQuestion> {
    @Override
    public AskQuestion reduce(AskQuestion a1, AskQuestion a2) throws Exception {
        if (a1.getDbUpdateTime() > a2.getDbUpdateTime()) {
            a1.setDeltaDiscussCount(a1.getDeltaDiscussCount() + a2.getDeltaDiscussCount());
            a1.setDeltaScoreCount(a1.getDeltaScoreCount() + a2.getDeltaScoreCount());
            return a1;
        } else {
            a2.setDeltaDiscussCount(a1.getDeltaDiscussCount() + a2.getDeltaDiscussCount());
            a2.setDeltaScoreCount(a1.getDeltaScoreCount() + a2.getDeltaScoreCount());
            return a2;
        }
    }
}
