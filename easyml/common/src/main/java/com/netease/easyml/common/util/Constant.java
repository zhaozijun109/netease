package com.netease.easyml.common.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by linjiuning on 2020/6/22.
 */
public class Constant {
    public static final String URL_REGEX = "([hH][tT]{2}[pP]:/*|[hH][tT]{2}[pP][sS]:/*|[fF][tT][pP]:/*)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~\\/])+(\\?{0,1}(([A-Za-z0-9-~]+\\={0,1})([A-Za-z0-9-~]*)\\&{0,1})*)";
    public static final String USE_LESS_PATTERN = "[^a-zA-Z0-9\\u4E00-\\u9FFF\\u3400-\\u4DBF\\uF900-\\uFAFF，,。.？?！!：:；;~（(）)%《<》>\\[【\\]】、“”\"\\-]";

    private static final String PUNCT_PATH = "/data/punctuation";
    public static final String PUNCT = String.join("", IOUtil.readLines(Constant.class.getResourceAsStream(PUNCT_PATH)));
    public static final String PUNCT_PATTERN = "[" + Pattern.quote(Constant.PUNCT) + "]";

    private static final String STOPWORD_PATH = "/data/stopword";
    public static final Set<String> STOP_WORD = new HashSet<>(IOUtil.readLines(Constant.class.getResourceAsStream(STOPWORD_PATH)));

    public static final String MID_SENTENCE_SEP = "、,，；;:：———|丨│｜";
    public static final String MID_SENTENCE_SEP_PATTERN = "[" + Pattern.quote(MID_SENTENCE_SEP) + "]";
    public static final String MID_SENTENCE_SEP_KEEP_PATTERN = "(?<=[" + Pattern.quote(MID_SENTENCE_SEP) + "])";

    public static final String SENTENCE_SEP = "。？?！!；;~…\n";
    public static final String SENTENCE_SEP_PATTERN = "[" + Pattern.quote(SENTENCE_SEP) + "]";
    public static final String SENTENCE_SEP_KEEP_PATTERN = "(?<=[" + Pattern.quote(SENTENCE_SEP) + "])";

    // combine of MID_SENTENCE_SEP and SENTENCE_SEP
    public static final String FG_SENTENCE_SEP = "、,，。？?！!；;:：~———|丨│｜…\n";
    public static final String FG_SENTENCE_SEP_PATTERN = "[" + Pattern.quote(FG_SENTENCE_SEP) + "]";
    public static final String FG_SENTENCE_SEP_KEEP_PATTERN = "(?<=[" + Pattern.quote(FG_SENTENCE_SEP) + "])";

    public static final String SENTENCE_SEP_V2 = "。？?！!；;…\n\r";
    public static final String SENTENCE_SEP_PATTERN_V2 = "((([" + Pattern.quote(SENTENCE_SEP_V2) + "][”\"》]?)|(?<![0-9])~(?![0-9]))+)";

    public static final String FG_SENTENCE_SEP_V2 = "、,，。？?！!；;:：~———|丨│｜…\n\r";
    public static final String FG_SENTENCE_SEP_PATTERN_V2 = "(([" + Pattern.quote(FG_SENTENCE_SEP_V2) + "][”\"》]?)+)";

    public static final String SEP = "@SEP@";

    public static final String START_CLOSE = "[【(（｢「『《";
    public static final String END_CLOSE = "]】)）｣」』]》";

    public static final String IN_CLOSE_PATTERN = "[\\[【(（｢「『][^\\]】)）｣」』]*[\\]】)）｣」』]";
    public static final String CHINESE_NUMBER = "零一二三四五六七八九十";
    public static final String SPECIAL_NUMBER = "①②③④⑤⑥⑦⑧⑨⑩⑪⑫⑬⑭⑮⑯⑰⑱⑲⑳㉑㉒㉓㉔㉕㉖㉗㉘㉙㉚㉛㉜㉝㉞㉟㊱㊲㊳㊴㊵㊶㊷㊸㊹㊺㊻㊼㊽㊾㊿";
    public static final String LABEL_PATTERN = String.format("^(第?[\\d%s]{1,2}[.，,､、:：](?!\\d)|NO.\\d{1,2}|第?[%s]([.，,､、:：])?|第?([(（\\[【])[\\d%s]{1,2}([)）\\]】])([.，,､、:：])?)", CHINESE_NUMBER, SPECIAL_NUMBER, CHINESE_NUMBER);

    private static final String[] _STOP_POS = new String[]{
            "w", "wb", "wd", "wf", "wh", "wj", "wky", "wkz", "wm", "wn",
            "wp", "ws", "wt", "ww", "wyy", "wyz", "xx", "y", "yg", "vyou",
            "vshi", "vg", "uzhi", "uzhe", "uz", "uyy", "uv", "usuo", "uls", "ulian",
            "ule", "uguo", "ug", "udh", "udeng", "ude1", "ude2", "ude3", "q", "qg",
            "qt", "qv", "pbei", "pba", "p", "o", /*"m", "mg", "Mg", "mq",*/
            "end", "e", "dl", "dg", "begin"
    };

    public static final Set<String> STOP_POS = new HashSet<>(Arrays.asList(_STOP_POS));
}
