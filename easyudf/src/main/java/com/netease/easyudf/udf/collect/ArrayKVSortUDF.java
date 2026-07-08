package com.netease.easyudf.udf.collect;

import org.apache.hadoop.hive.ql.exec.UDF;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by linjiuning on 2021/3/31.
 */
public class ArrayKVSortUDF extends UDF {

    public List<String> evaluate(List<String> kvs, boolean reverse) {
        if (kvs == null) {
            return null;
        }
        List<Pair<String, Double>> res = new ArrayList<>();
        for (String kv : kvs) {
            try {
                if (kv != null) {
                    int i = kv.lastIndexOf(":");
                    if (i >= 0) {
                        String key = kv.substring(0, i);
                        Double value = Double.parseDouble(kv.substring(i + 1));
                        res.add(new Pair<>(key, value));
                    }
                }
            } catch (Exception ignored) {
            }
        }
        if (res.isEmpty()) {
            return null;
        }
        if (reverse) {
            return res.stream().sorted((it1, it2) -> it2.getValue1().compareTo(it1.getValue1()))
                    .map(it -> it.getValue0() + ":" + it.getValue1()).collect(Collectors.toList());

        }
        return res.stream().sorted(Comparator.comparing(Pair::getValue1))
                .map(it -> it.getValue0() + ":" + it.getValue1()).collect(Collectors.toList());

    }

    public List<String> evaluate(List<String> array) {
        return evaluate(array, false);
    }
}