package com.netease.easyudf.udf.collect;

import org.apache.hadoop.hive.ql.exec.UDF;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linjiuning on 2021/3/31.
 */
public class ArrayKVGetValueUDF extends UDF {
    public List<Double> evaluate(List<String> kvs) {
        return evaluate(kvs, ":");
    }

    public List<Double> evaluate(List<String> kvs, String sep) {
        if (kvs == null) {
            return null;
        }
        List<Double> res = new ArrayList<>();
        for (String kv : kvs) {
            try {
                if (kv != null) {
                    int i = kv.lastIndexOf(sep);
                    if (i >= 0) {
                        Double value = Double.parseDouble(kv.substring(i + 1));
                        res.add(value);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return res.isEmpty() ? null : res;
    }

    public List<String> evaluate(List<String> kvs, String sep, boolean asDouble) {
        if (kvs == null) {
            return null;
        }
        List<String> res = new ArrayList<>();
        for (String kv : kvs) {
            try {
                if (kv != null) {
                    int i = kv.lastIndexOf(sep);
                    if (i >= 0) {
                        String value = kv.substring(i + 1);
                        res.add(value);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return res.isEmpty() ? null : res;
    }

    public List<String> evaluate(List<String> kvs, boolean asDouble) {
        return evaluate(kvs, ":", asDouble);
    }
}