package com.netease.easyudf.udf.alg;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import com.huaban.analysis.jieba.WordDictionary;
import com.netease.easyml.common.util.IOUtil;
import com.netease.easyml.common.util.JacksonUtil;
import com.netease.easyml.common.util.StringUtil;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by linjiuning on 2020/10/12.
 */
public class JiebaTokenizer extends UDF {
    private static final Logger log = LoggerFactory.getLogger(JiebaTokenizer.class);
    private static final int FREQ = 10000000;
    private static final Set<String> VOCABS = new HashSet<>();

    public static class Config {
        String customDictionary = "";
        String delimiter = " ";
        JiebaSegmenter.SegMode mode = JiebaSegmenter.SegMode.SEARCH;

        public String getCustomDictionary() {
            return customDictionary;
        }

        public Config setCustomDictionary(String customDictionary) {
            this.customDictionary = customDictionary;
            return this;
        }

        public String getDelimiter() {
            return delimiter;
        }

        public Config setDelimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public JiebaSegmenter.SegMode getMode() {
            return mode;
        }

        public Config setMode(String mode) {
            this.mode = JiebaSegmenter.SegMode.valueOf(mode.toUpperCase());
            return this;
        }
    }

    public List<String> evaluate(String text) {
        return tokenize(text, new Config());
    }

    public List<String> evaluate(String text, String config) {
        if (text == null) {
            return null;
        }
        Config conf = JacksonUtil.jsonToBean(config, Config.class);
        return tokenize(text, conf);
    }

    public List<String> tokenize(String text, Config config) {
        JiebaSegmenter segment = newSegment(config);
        List<SegToken> terms = segment.process(text, config.mode);
        List<String> tokens = new ArrayList<>();
        for (SegToken term : terms) {
            tokens.add(term.word);
        }
        return tokens;
    }

    private static JiebaSegmenter newSegment(Config config) {
        JiebaSegmenter segment = new JiebaSegmenter();

        if (!StringUtil.isEmpty(config.customDictionary)) {
            if (!VOCABS.contains(config.customDictionary)) {
                synchronized (JiebaTokenizer.class) {
                    if (!VOCABS.contains(config.customDictionary)) {
                        loadCustomDictionary(config.customDictionary, config.delimiter);
                        VOCABS.add(config.customDictionary);
                    }
                }
            }
        }
        return segment;
    }

    private static void loadCustomDictionary(String path, String delimiter) {
        WordDictionary dictionary = WordDictionary.getInstance();
        try {
            long startTime = System.currentTimeMillis();
            int count = 0;
            Field field = dictionary.getClass().getDeclaredField("total");
            field.setAccessible(true);
            Double total = (Double) field.get(dictionary);
            Method method = dictionary.getClass().getDeclaredMethod("addWord", String.class);
            method.setAccessible(true);
            for (String line : IOUtil.readLines(path)) {
                String word;
                String natureWithFreq;
                int i = line.indexOf(delimiter);
                if (i < 1) {
                    word = line;
                    natureWithFreq = String.format("nz %d", FREQ);
                } else {
                    word = line.substring(0, i);
                    natureWithFreq = line.substring(i + 1);
                    String[] split = natureWithFreq.split(delimiter);
                    if (split.length == 1) {
                        natureWithFreq = String.format("nz %s", split[0]);
                    } else if (natureWithFreq.isEmpty()) {
                        natureWithFreq = String.format("nz %d", FREQ);
                    }
                }

                try {
                    word = (String) method.invoke(dictionary, word);
                    double freq = Double.parseDouble(natureWithFreq.split(" ")[1]);
                    dictionary.freqs.put(word, Math.log(freq / total));
                    count++;
                } catch (NumberFormatException ignored) {
                }
            }
            log.info(String.format("user dict %s load finished, tot words:%d, time elapsed:%dms", path, count, System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            log.error(String.format("%s: load user dict failure!", path));
        }
    }
}
