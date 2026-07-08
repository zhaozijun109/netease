package com.netease.operator.ads.pve;

import com.netease.pojo.pve.PveRolePropsCostResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.flink.api.common.functions.ReduceFunction;

public class AdsPveUserPropsStatisticsReduceFunction
        implements ReduceFunction<PveRolePropsCostResult> {
    private static final Logger LOG =
            LoggerFactory.getLogger(AdsPveUserPropsStatisticsReduceFunction.class);

    @Override
    public PveRolePropsCostResult reduce(PveRolePropsCostResult p1, PveRolePropsCostResult p2)
            throws Exception {
        p1.setCostStamina(p1.getCostStamina() + p2.getCostStamina());
        return p1;
    }
}
