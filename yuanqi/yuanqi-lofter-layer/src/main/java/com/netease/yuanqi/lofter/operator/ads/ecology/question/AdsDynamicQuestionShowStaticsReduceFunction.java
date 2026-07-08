package com.netease.yuanqi.lofter.operator.ads.ecology.question;

import com.netease.yuanqi.lofter.pojo.ads.ecology.question.DynamicQuestionShowStatistics;
import org.apache.flink.api.common.functions.ReduceFunction;

public class AdsDynamicQuestionShowStaticsReduceFunction
        implements ReduceFunction<DynamicQuestionShowStatistics> {
    @Override
    public DynamicQuestionShowStatistics reduce(
            DynamicQuestionShowStatistics d1, DynamicQuestionShowStatistics d2) throws Exception {
        return d1.getHourCount() > d2.getHourCount() ? d1 : d2;
    }
}
