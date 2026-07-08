package com.netease.easyudf.udf.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.netease.easyml.common.util.ArrayUtil;
import org.apache.hadoop.hive.ql.exec.UDF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RenderForUDF extends UDF {
    private String render(String text, Object k, Object v) {
        return text.replaceAll("\\$\\{" + k + "}", v.toString());
    }

    private List<String> render(String text, Map vars) {
        List<String> results = new ArrayList<>();
        results.add(text);
        for (Object k : vars.keySet()) {
            Object v = vars.get(k);
            int size = results.size();
            for (int i = 0; i < size; i++) {
                if (ArrayUtil.isNDArray(v)) {
                    for (int j = 0; j < ArrayUtil.size(v); j++)
                        results.add(render(results.get(i), k, ArrayUtil.get(v, j)));
                } else {
                    results.add(render(results.get(i), k, v));
                }
            }
            results = results.subList(size, results.size());
        }
        return results;
    }

    public List<String> evaluate(String text, String vars) {
        if (text == null)
            return null;
        if (vars == null)
            return Collections.singletonList(text);
        JSONObject jSONObject = JSON.parseObject(vars, Feature.OrderedField);
        return render(text, jSONObject);
    }
}
