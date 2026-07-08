package com.netease.easyudf.udf.alg;

import com.netease.easyml.common.util.JacksonUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.hadoop.hive.ql.exec.UDF;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SessionSegmentUDF extends UDF {
    private static final Config CONFIG = new Config();

    @Data
    @Accessors(chain = true)
    public static class Config {
        String dateFormat = "yyyy-MM-dd HH:mm:ss";
        // 秒
        long interval = 1800;
        // time#item_id
        String timeSep = "#";
        // time#item_id,time#item_id
        String itemSep = ",";
    }

    public List<String> evaluate(List<String> items) throws ParseException {
        return segment(items, CONFIG);
    }

    public List<String> evaluate(List<String> items, String config) throws ParseException {
        Config conf = JacksonUtil.jsonToBean(config, Config.class);
        return segment(items, conf);
    }

    public List<String> segment(List<String> items, Config config) throws ParseException {
        if (items == null)
            return null;
        Collections.sort(items);
        List<String> res = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        long lastTime = 0;

        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            int split = item.indexOf(config.timeSep);

            String time = item.substring(0, split);
            String itemId = item.substring(split + 1);

            SimpleDateFormat format = new SimpleDateFormat(config.dateFormat);
            long timeUnix = format.parse(time).getTime() / 1000L;
            if (i == 0) {
                lastTime = timeUnix;
                sb.append(itemId);
                continue;
            }

            if (timeUnix - lastTime > config.interval && sb.length() > 0) {
                res.add(sb.toString());
                sb = new StringBuilder();
            }

            if (sb.length() > 0) {
                sb.append(config.itemSep);
            }
            sb.append(itemId);
            lastTime = timeUnix;
        }
        if (sb.length() > 0) {
            res.add(sb.toString());
        }
        return res;
    }
}
