package com.netease.easyudf.udf.util;

import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.junit.Before;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;

public class HashStringToIntUDFTest {
    private HashStringToIntUDF udf;

    @Before
    public void before() {
        udf = new HashStringToIntUDF();
    }

    @Test
    public void evaluate() throws HiveException, NoSuchAlgorithmException {
        System.out.println(udf.evaluate("821265056@4ca8f806_1cd054522", 8));
    }
}