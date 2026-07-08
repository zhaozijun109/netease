package com.netease.easyudf.udf.milvus;

import org.junit.Before;
import org.junit.Test;

public class ReleaseCollectionUDFTest {
    private ReleaseCollectionUDF udf;

    @Before
    public void before() {
        udf = new ReleaseCollectionUDF();
    }

    @Test
    public void evaluate() throws Exception {
        System.out.println(udf.evaluate("test_spark"));
    }
}