package com.netease.easyudf.udf.math;

import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.junit.Before;
import org.junit.Test;

public class HammingWeightUDFTest {
    private HammingWeightUDF udf;

    @Before
    public void before() {
        udf = new HammingWeightUDF();
    }

    @Test
    public void evaluate() throws HiveException {
        int w = udf.evaluate(3);
        System.out.println(w);
    }
}