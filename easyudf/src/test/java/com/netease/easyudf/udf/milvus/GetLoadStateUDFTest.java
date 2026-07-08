package com.netease.easyudf.udf.milvus;

import org.junit.Before;
import org.junit.Test;

public class GetLoadStateUDFTest {
    private GetLoadStateUDF udf;

    @Before
    public void before() {
        udf = new GetLoadStateUDF();
    }

    @Test
    public void evaluate() throws Exception {
        System.out.println(udf.evaluate("test_spark"));
    }
}