package com.netease.easyudf.udf.alg;

import com.hankcs.hanlp.HanLP;
import com.netease.easyml.common.util.Constant;
import com.netease.easyml.common.util.JacksonUtil;
import com.netease.easyml.common.util.StringUtil;
import org.apache.hadoop.hive.ql.exec.UDF;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linjiuning on 2020/9/2.
 */
public class TextNormalizeUDF extends UDF {

    private static final Config CONFIG = new Config();

    public static class Config {
        boolean lowercase = true;
        boolean fullToHalf = true;
        boolean simplified = true;
        boolean removeEmoji = false;
        boolean removePunctuation = false;
        boolean removeHtml = false;

        public Config setLowercase(boolean lowercase) {
            this.lowercase = lowercase;
            return this;
        }

        public Config setFullToHalf(boolean fullToHalf) {
            this.fullToHalf = fullToHalf;
            return this;
        }

        public Config setSimplified(boolean simplified) {
            this.simplified = simplified;
            return this;
        }

        public Config setRemoveEmoji(boolean removeEmoji) {
            this.removeEmoji = removeEmoji;
            return this;
        }

        public Config setRemovePunctuation(boolean removePunctuation) {
            this.removePunctuation = removePunctuation;
            return this;
        }

        public Config setRemoveHtml(boolean removeHtml) {
            this.removeHtml = removeHtml;
            return this;
        }
    }

    public String evaluate(String text) {
        if (text == null) {
            return null;
        }
        return normalize(text, CONFIG);
    }

    public String evaluate(String text, String config) {
        if (text == null) {
            return null;
        }
        Config conf = JacksonUtil.jsonToBean(config, Config.class);
        return normalize(text, conf);
    }

    public List<String> evaluate(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return null;
        }
        List<String> res = new ArrayList<>();

        for (String text : texts) {
            text = normalize(text, CONFIG);
            res.add(text);
        }
        return res;
    }

    public List<String> evaluate(List<String> texts, String config) {
        if (texts == null || texts.isEmpty()) {
            return null;
        }
        List<String> res = new ArrayList<>();

        Config conf = JacksonUtil.jsonToBean(config, Config.class);
        for (String text : texts) {
            text = normalize(text, conf);
            res.add(text);
        }
        return res;
    }

    public String normalize(String text, Config config) {
        text = StringUtil.trim(text);
        if (config.lowercase) {
            text = text.toLowerCase();
        }
        if (!text.isEmpty() && config.fullToHalf) {
            text = StringUtil.fullToHalf(text);
        }
        if (!text.isEmpty() && config.simplified) {
            text = HanLP.convertToSimplifiedChinese(text);
        }
        if (!text.isEmpty() && config.removeEmoji) {
            text = StringUtil.removeAllEmojis(text);
        }
        if (!text.isEmpty() && config.removeHtml) {
            text = text.replaceAll(Constant.URL_REGEX, " ");
        }
        if (!text.isEmpty() && config.removePunctuation) {
            text = StringUtil.removePunctuation(text);
        }
        if (!text.isEmpty()) {
            text = StringUtil.trim(text.replaceAll("\\s+", " "));
        }
        return text;
    }
}
