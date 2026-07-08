package com.netease.easyudf.udf.milvus;

import com.netease.easyudf.udf.util.MilvusUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class GetCollectionStatisticsUDFTest {
    private GetCollectionStatisticsUDF udf;

    @Before
    public void before() {
        udf = new GetCollectionStatisticsUDF();
    }

    @Test
    public void evaluate() throws Exception {
        MilvusUtil.setEnv("dev");
        System.out.println(udf.evaluate("ltv_lifecycle_u2i"));
    }
}