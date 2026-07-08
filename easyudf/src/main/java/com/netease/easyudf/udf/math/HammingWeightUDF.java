package com.netease.easyudf.udf.math;

import org.apache.hadoop.hive.ql.exec.UDF;

public class HammingWeightUDF extends UDF {

    public int evaluate(int n) {
        int ret = 0;
        while (n != 0) {
            n &= n - 1;
            ret++;
        }
        return ret;
    }

    public long evaluate(long n) {
        int ret = 0;
        while (n != 0) {
            n &= n - 1;
            ret++;
        }
        return ret;
    }
}