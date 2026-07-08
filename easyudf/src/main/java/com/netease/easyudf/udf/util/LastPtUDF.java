package com.netease.easyudf.udf.util;

import com.netease.easyml.common.util.IOUtil;
import org.apache.hadoop.hive.ql.exec.UDF;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LastPtUDF extends UDF {
    public String evaluate(String path, String ptD) {
        return evaluate(path, ptD, "yyyy-MM-dd");
    }

    public String evaluate(String path, String ptD, String format) {
        List<String> directory = IOUtil.listAllDirectory(path);
        List<String> ptDs = new ArrayList<>();
        String pat = Stream.of(format.split("-")).map(it -> {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < it.length(); i++)
                sb.append(".");
            return sb.toString();
        }).collect(Collectors.joining("-"));
        Pattern pt = Pattern.compile(String.format("%s=(%s)$|%s=(%s)/", new Object[]{ptD, pat, ptD, pat}));
        for (String d : directory) {
            Matcher m = pt.matcher(d);
            if (m.find())
                for (int i = 1; i < m.groupCount(); i++) {
                    if (m.group(i) != null) {
                        ptDs.add(m.group(i));
                        break;
                    }
                }
        }
        ptDs.sort(String::compareTo);
        if (ptDs.isEmpty())
            return "";
        return ptDs.get(ptDs.size() - 1);
    }
}