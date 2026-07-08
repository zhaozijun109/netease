package com.netease.easyudf.udf.io;


import com.netease.easyml.common.util.CollectionUtil;
import com.netease.easyml.common.util.IOUtil;
import com.netease.easyml.common.util.StringUtil;
import org.apache.hadoop.hive.ql.exec.UDF;

import java.util.ArrayList;
import java.util.List;

public class ListFileUDF extends UDF {

    public List<String> evaluate(String path) {
        return evaluate(path, 0L, 0L);
    }

    public List<String> evaluate(String path, long startModified, long endModified) {
        if (StringUtil.isEmpty(path))
            return null;
        List<String> files = IOUtil.listFile(path, it ->
                (!IOUtil.baseName(it).startsWith(".") && !IOUtil.baseName(it).startsWith("_")));
        if (startModified > 0L || endModified > 0L) {
            List<String> result = new ArrayList<>();
            for (String file : files) {
                long l = IOUtil.lastModified(file);
                if (startModified > 0L && l < startModified)
                    continue;
                if (endModified > 0L && l >= endModified)
                    continue;
                result.add(file);
            }
            files = result;
        }
        return files.isEmpty() ? null : files;
    }

    public List<String> evaluate(List<String> paths) {
        return evaluate(paths, 0L, 0L);
    }

    public List<String> evaluate(List<String> paths, long startModified, long endModified) {
        if (CollectionUtil.isEmpty(paths))
            return null;
        List<String> result = new ArrayList<>();
        for (String path : paths) {
            List<String> evaluate = evaluate(path, startModified, endModified);
            if (evaluate != null)
                result.addAll(evaluate);
        }
        return result.isEmpty() ? null : result;
    }
}
