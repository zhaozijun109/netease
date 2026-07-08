package com.netease.easyudf.udf.milvus;

import org.junit.Before;
import org.junit.Test;

public class DropCollectionUDFTest {
    private DropCollectionUDF udf;

    @Before
    public void before() {
        udf = new DropCollectionUDF();
    }

    @Test
    public void evaluate() throws Exception {
        System.out.println(udf.evaluate("test_spark"));
    }
}