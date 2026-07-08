package com.netease.easyml.local.mllib.processor.cleaner;


import com.netease.easyml.common.util.Constant;
import com.netease.easyml.local.mllib.processor.cleaner.impl.PipeLineCleaner;
import com.netease.easyml.local.mllib.processor.cleaner.impl.RegexCleaner;

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by eddielin on 2018/6/4.
 */
public class CleanerFactory {

    private static RegexCleaner PUNCT_CLEANER;

    static {
        String punct1 = Pattern.quote(Constant.PUNCT.replaceAll("[%％]", ""));
        String punct2 = Pattern.quote(Constant.PUNCT.replaceAll("[%％.]", ""));
        String pattern1 = String.format("(?<!\\d)[%s](?!\\d)", punct1);
        String pattern2 = String.format("[%s]{2,}", punct1);
        String pattern3 = String.format("(?<=\\d)[%s]$", punct1);
        String pattern4 = String.format("(?<=\\d)[%s](?!\\d)", punct2);
        String pattern5 = String.format("(?<!\\d)[%s](?=\\d)", punct2);
//        String pattern = String.format("(?<!\\d)[%s](?!\\d)||(?<=\\d)[%s]$|(?<=\\d)[%s](?!\\d)|(?<!\\d)[%s](?=\\d)", punct1, punct1, punct1, punct2, punct2);
        PUNCT_CLEANER = new RegexCleaner(pattern1, pattern2, pattern3, pattern4, pattern5);
    }

    public static RegexCleaner getPunctCleaner() {
        return PUNCT_CLEANER;
    }

    private static RegexCleaner STOPWORD_CLEANER;

    static {
        String pattern = "^(" + Constant.STOP_WORD.stream().map(Pattern::quote).collect(Collectors.joining("|")) + ")$";
        STOPWORD_CLEANER = new RegexCleaner(pattern);
    }

    public static RegexCleaner getStopWordCleaner() {
        return STOPWORD_CLEANER;
    }

    private static final RegexCleaner INCLOSE_CLEANER = new RegexCleaner(Constant.IN_CLOSE_PATTERN);

    /**
     * (ab)cd -> cd
     */
    public static RegexCleaner getIncloseCleaner() {
        return INCLOSE_CLEANER;
    }

    private static final RegexCleaner LABEL_CLEANER = new RegexCleaner(Constant.LABEL_PATTERN);

    /**
     * 1. abcd -> abcd
     */
    public static RegexCleaner getLabelCleaner() {
        return LABEL_CLEANER;
    }

    private static final RegexCleaner USE_LESS_CLEANER = new RegexCleaner(Constant.USE_LESS_PATTERN);

    public static RegexCleaner getUseLessCleaner() {
        return USE_LESS_CLEANER;
    }

    private static final RegexCleaner SPACE_CLEANER = new RegexCleaner("[\\s\\t]+");

    public static RegexCleaner getSpaceCleaner() {
        return SPACE_CLEANER;
    }

    /**
     * 去除标点符号, 特殊字符, 编号, 括号内容
     */
    private static PipeLineCleaner WEAK_NORM_CLEANER;
    private static PipeLineCleaner WEAK_NORM_NOPUNC_CLEANER;

    static {
        WEAK_NORM_CLEANER = new PipeLineCleaner(INCLOSE_CLEANER, PUNCT_CLEANER, USE_LESS_CLEANER, LABEL_CLEANER, SPACE_CLEANER);
        WEAK_NORM_NOPUNC_CLEANER = new PipeLineCleaner(INCLOSE_CLEANER, USE_LESS_CLEANER, LABEL_CLEANER, SPACE_CLEANER);
    }

    public static ICleaner getWeakCleaner() {
        return getWeakCleaner(true);
    }

    public static ICleaner getWeakCleaner(boolean punct) {
        return punct ? WEAK_NORM_CLEANER : WEAK_NORM_NOPUNC_CLEANER;
    }

    public static ICleaner getPatternCleaner(String... patterns) {
        return new RegexCleaner(patterns);
    }

    public static ICleaner getPatternCleaner(Collection<String> patterns) {
        return new RegexCleaner(patterns);
    }
}
