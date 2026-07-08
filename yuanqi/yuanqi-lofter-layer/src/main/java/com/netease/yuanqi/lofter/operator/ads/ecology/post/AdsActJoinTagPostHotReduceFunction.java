package com.netease.yuanqi.lofter.operator.ads.ecology.post;

import com.netease.yuanqi.lofter.pojo.ads.ecology.post.ActJoinTagPostHot;
import org.apache.flink.api.common.functions.ReduceFunction;

public class AdsActJoinTagPostHotReduceFunction implements ReduceFunction<ActJoinTagPostHot> {
    @Override
    public ActJoinTagPostHot reduce(ActJoinTagPostHot a1, ActJoinTagPostHot a2) throws Exception {
        Long hotCount = a1.getHot() + a2.getHot();
        a1.setHot(hotCount);
        a2.setHot(hotCount);
        return a1.getPostModifyTime() > a2.getPostModifyTime() ? a1 : a2;
    }
}
