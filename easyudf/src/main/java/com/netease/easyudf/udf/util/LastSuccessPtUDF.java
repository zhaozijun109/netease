package com.netease.easyudf.udf.util;

import com.netease.easyml.common.util.IOUtil;
import org.apache.hadoop.hive.ql.exec.UDF;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LastSuccessPtUDF extends UDF {
    private static final String SUCCESS = "_SUCCESS";
    private static final Pattern PT = Pattern.compile("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]");

    public String evaluate(String path) {
        List<String> directory = IOUtil.listAllDirectory(path);
        List<String> ptDs = new ArrayList<>();

        for (String d : directory) {
            String baseName = IOUtil.baseName(d);
            Matcher m = PT.matcher(baseName);
            if (m.matches() && IOUtil.exists(IOUtil.join(d, SUCCESS))) {
                ptDs.add(baseName);
            }
        }
        ptDs.sort(String::compareTo);
        if (ptDs.isEmpty())
            return "";
        return ptDs.get(ptDs.size() - 1);
    }
}