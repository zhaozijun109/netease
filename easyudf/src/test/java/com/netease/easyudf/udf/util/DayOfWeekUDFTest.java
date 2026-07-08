package com.netease.easyudf.udf.util;

import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DayOfWeekUDFTest {
    private DayOfWeekUDF udf;

    @Before
    public void before() {
        udf = new DayOfWeekUDF();
    }

    @Test
    public void evaluate() throws HiveException {
        String monday = udf.evaluate("2023-10-02", "monday", 0);
        assertEquals(monday, "2023-10-02");
    }
}