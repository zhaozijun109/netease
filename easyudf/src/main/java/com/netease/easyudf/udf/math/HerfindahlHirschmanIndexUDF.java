package com.netease.easyudf.udf.math;


import org.apache.hadoop.hive.ql.exec.UDF;

import java.util.List;

public class HerfindahlHirschmanIndexUDF extends UDF {
    public Double evaluate(List<Double> arrays) {
        return evaluate(arrays, 1.0D);
    }

    public Double evaluate(List<Double> arrays, Double counter) {
        if (arrays == null || arrays.isEmpty() || counter == 0.0D)
            return null;
        double ret = 0.0D;
        for (Double array : arrays) {
            array = array / counter;
            ret += array * array;
        }
        return ret;
    }
}