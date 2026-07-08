package com.netease.easyudf.udf.milvus;

import org.junit.Before;
import org.junit.Test;

public class LoadCollectionUDFTest {
    private LoadCollectionUDF udf;

    @Before
    public void before() {
        udf = new LoadCollectionUDF();
    }

    @Test
    public void evaluate() throws Exception {
        System.out.println(udf.evaluate("sb_manual_supply", 3));
    }
}