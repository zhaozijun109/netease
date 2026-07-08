package com.netease.easyudf.udf.math;

import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.junit.Before;
import org.junit.Test;

public class BetaDistributionSampleUDFTest {
    private BetaDistributionSampleUDF udf;

    @Before
    public void before() {
        udf = new BetaDistributionSampleUDF();
    }

    @Test
    public void evaluate() throws HiveException {
        double score = udf.evaluate(10000, 90000);
        System.out.println(score);
    }
}