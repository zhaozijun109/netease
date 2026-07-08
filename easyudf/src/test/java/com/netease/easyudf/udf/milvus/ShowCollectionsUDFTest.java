package com.netease.easyudf.udf.milvus;

import com.netease.easyudf.udf.util.MilvusUtil;
import org.junit.Before;
import org.junit.Test;

public class ShowCollectionsUDFTest {

    private ShowCollectionsUDF udf;

    @Before
    public void before() {
        udf = new ShowCollectionsUDF();
    }

    @Test
    public void evaluate() throws Exception {
        MilvusUtil.setEnv("dev");
        System.out.println(udf.evaluate());
    }
}