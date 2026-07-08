package com.netease.easyudf.udf.alg;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.netease.easyml.common.util.JacksonUtil;
import com.netease.easyml.common.util.SegmentUtil;
import com.netease.easyml.common.util.StringUtil;
import org.apache.hadoop.hive.ql.exec.UDF;

import java.util.*;

/**
 * Created by linjiuning on 2020/9/2.
 */
public class HanLPTokenizer extends UDF {
    private static final Map<String, Segment> SEGMENTS = new HashMap<>();
    private static final Set<String> VOCABS = new HashSet<>();

    private static String[] _STOP_POS = new String[]{
            "w", "wb", "wd", "wf", "wh", "wj", "wky", "wkz", "wm", "wn",
            "wp", "ws", "wt", "ww", "wyy", "wyz", "xx", "y", "yg", "vyou",
            "vshi", "vg", "uzhi", "uzhe", "uz", "uyy", "uv", "usuo", "uls", "ulian",
            "ule", "uguo", "ug", "udh", "udeng", "ude1", "ude2", "ude3", "q", "qg",
            "qt", "qv", "pbei", "pba", "p", "o", "m", "mg", "Mg", "mq",
            "end", "e", "dl", "dg", "begin",
            "c", "cc", "r" // add by showcai
    };

    public static Set<String> STOP_POS = new HashSet<>(Arrays.asList(_STOP_POS));

    public static class Config {
        boolean withPos = false;
        String customDictionary = "";
        String delimiter = " ";
        boolean overwrite = false;
        boolean removeStopWord = false;
        String algorithm = "viterbi";

        public Config setRemoveStopWord(boolean removeStopWord) {
            this.removeStopWord = removeStopWord;
            return this;
        }

        public Config setDelimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public Config setWithPos(boolean withPos) {
            this.withPos = withPos;
            return this;
        }

        public Config setCustomDictionary(String customDictionary) {
            this.customDictionary = customDictionary;
            return this;
        }

        public Config setOverwrite(boolean overwrite) {
            this.overwrite = overwrite;
            return this;
        }

        public Config setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
            return this;
        }
    }

    public List<String> evaluate(String text) {
        return evaluate(text, false);
    }

    public List<String> evaluate(String text, boolean withPos) {
        if (text == null) {
            return null;
        }
        Config config = new Config();
        config.setWithPos(withPos);
        return tokenize(text, config);
    }

    public List<String> evaluate(String text, String config) {
        if (text == null) {
            return null;
        }
        Config conf = JacksonUtil.jsonToBean(config, Config.class);
        return tokenize(text, conf);
    }

    public List<String> tokenize(String text, Config config) {
        Segment segment = newSegment(config);
        List<Term> terms = segment.seg(text);
        List<String> tokens = new ArrayList<>();
        for (Term term : terms) {
            if (config.removeStopWord && STOP_POS.contains(term.nature.toString())) {
                continue;
            }
            if (config.withPos) {
                tokens.add(term.toString());
            } else {
                tokens.add(term.word);
            }
        }
        return tokens;
    }

    private static Segment newSegment(Config config) {
        String algorithm = config.algorithm;
        if (!SEGMENTS.containsKey(algorithm)) {
            synchronized (HanLPTokenizer.class) {
                if (!SEGMENTS.containsKey(algorithm)) {
                    Segment segment = HanLP.newSegment(config.algorithm);
                    segment.enableCustomDictionary(true);
                    SEGMENTS.put(algorithm, segment);
                }
            }
        }

        if (!StringUtil.isEmpty(config.customDictionary)) {
            if (!VOCABS.contains(config.customDictionary)) {
                synchronized (HanLPTokenizer.class) {
                    if (!VOCABS.contains(config.customDictionary)) {
                        SegmentUtil.addVocab(config.customDictionary, config.delimiter, config.overwrite);
                        VOCABS.add(config.customDictionary);
                    }
                }
            }
        }

        return SEGMENTS.get(algorithm);
    }
}
