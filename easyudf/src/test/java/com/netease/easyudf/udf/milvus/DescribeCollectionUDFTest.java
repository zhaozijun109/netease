package com.netease.easyudf.udf.milvus;

import org.junit.Before;
import org.junit.Test;

public class DescribeCollectionUDFTest {
    private DescribeCollectionUDF udf;

    @Before
    public void before() {
        udf = new DescribeCollectionUDF();
    }

    @Test
    public void evaluate() throws Exception {
        System.out.println(udf.evaluate("sb_manual_supply"));
    }
}