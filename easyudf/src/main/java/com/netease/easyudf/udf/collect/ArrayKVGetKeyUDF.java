package com.netease.easyudf.udf.collect;

import org.apache.hadoop.hive.ql.exec.UDF;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linjiuning on 2021/3/31.
 */
public class ArrayKVGetKeyUDF extends UDF {

    public List<String> evaluate(List<String> kvs, String sep) {
        if (kvs == null) {
            return null;
        }
        List<String> res = new ArrayList<>();
        for (String kv : kvs) {
            try {
                if (kv != null) {
                    int i = kv.lastIndexOf(sep);
                    if (i >= 0) {
                        String key = kv.substring(0, i);
                        res.add(key);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return res.isEmpty() ? null : res;
    }

    public List<String> evaluate(List<String> kvs) {
        return evaluate(kvs, ":");
    }
}