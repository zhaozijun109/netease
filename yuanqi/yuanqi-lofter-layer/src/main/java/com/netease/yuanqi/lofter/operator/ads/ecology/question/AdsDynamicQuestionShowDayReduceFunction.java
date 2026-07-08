package com.netease.yuanqi.lofter.operator.ads.ecology.question;

import com.netease.yuanqi.lofter.pojo.ads.ecology.question.DynamicQuestionShowStatistics;
import org.apache.flink.api.common.functions.ReduceFunction;

public class AdsDynamicQuestionShowDayReduceFunction
        implements ReduceFunction<DynamicQuestionShowStatistics> {
    @Override
    public DynamicQuestionShowStatistics reduce(
            DynamicQuestionShowStatistics t1, DynamicQuestionShowStatistics t2) throws Exception {
        DynamicQuestionShowStatistics result = new DynamicQuestionShowStatistics();
        result.setQuestionId(t1.getQuestionId());
        result.setQuestionType(t1.getQuestionType());
        result.setBlogId(t1.getBlogId());
        result.setHourCount(t1.getHourCount() + t2.getHourCount());
        result.setTagLottieType(Math.max(t1.getTagLottieType(), t2.getTagLottieType()));
        result.setWindowStartTime(Math.max(t1.getWindowStartTime(), t2.getWindowStartTime()));
        result.setWindowEndTime(Math.max(t1.getWindowEndTime(), t2.getWindowEndTime()));
        result.setTag(t1.getTag());
        return result;
    }
}
