package com.netease.easyml.common.util;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.CoreDictionary;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.huaban.analysis.jieba.WordDictionary;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by linjiuning on 2020/6/22.
 */
@Slf4j
public class SegmentUtil {
    private static final int FREQ = 10000000;

    public static void addVocab(String vocabPath) {
        addVocab(vocabPath, false, FREQ);
    }

    public static void addVocab(String vocabPath, String delimiter) {
        addVocab(vocabPath, delimiter, false, FREQ);
    }

    public static void addVocab(String vocabPath, boolean overwrite) {
        addVocab(vocabPath, overwrite, FREQ);
    }

    public static void addVocab(String vocabPath, String delimiter, boolean overwrite) {
        addVocab(vocabPath, delimiter, overwrite, FREQ);
    }

    public static void addVocab(String vocabPath, boolean overwrite, int freq) {
        addVocab(vocabPath, " ", overwrite, freq);
    }

    // word freqWithNature
    public static void addVocab(String vocabPath, String delimiter, boolean overwrite, int freq) {
        List<String> files = new ArrayList<>();
        if (IOUtil.isDirectory(vocabPath)) {
            files = IOUtil.listAllFile(vocabPath);
        } else {
            files.add(vocabPath);
        }
        for (String file : files) {
            for (String line : IOUtil.readLines(file)) {
                String s;
                String natureWithFreq;
                int i = line.indexOf(delimiter);
                if (i < 1) {
                    s = line;
                    natureWithFreq = String.format("nz %d", freq);
                } else {
                    s = line.substring(0, i);
                    natureWithFreq = line.substring(i + 1);
                    String[] split = natureWithFreq.split(delimiter);
                    if (split.length == 1) {
                        natureWithFreq = String.format("nz %s", split[0]);
                    } else if (natureWithFreq.isEmpty()) {
                        natureWithFreq = String.format("nz %d", freq);
                    }
                }
                synchronized (SegmentUtil.class) {
                    if (overwrite || (!CoreDictionary.contains(s)) && !CustomDictionary.contains(s))
                        CustomDictionary.insert(s, natureWithFreq);
                }
            }
        }
    }

    public static void addVocab(Set<String> vocabs) {
        addVocab(vocabs, false, FREQ);
    }

    public static void addVocab(Set<String> vocabs, boolean overwrite) {
        addVocab(vocabs, overwrite, FREQ);
    }

    public synchronized static void addVocab(Set<String> vocabs, boolean overwrite, int freq) {
        for (String s : vocabs) {
            if (s.isEmpty())
                continue;
            if (overwrite || (!CoreDictionary.contains(s)) && !CustomDictionary.contains(s))
                CustomDictionary.insert(s, String.format("nz %d", freq));
        }
    }

    public static List<String> segment(String text) {
        return segment(null, text);
    }

    public static List<String> segment(Segment segment, String text) {
        if (text.length() < 2)
            return Collections.singletonList(text);
        List<Term> terms;
        if (segment == null)
            terms = HanLP.segment(text);
        else
            terms = segment.seg(text);
        return terms.stream().map(it -> it.word).collect(Collectors.toList());
    }

    // 前缀 + 后缀
    public static List<String> dictSegment(Set<String> vocabs, String text) {
        if (text.length() < 2 || vocabs.contains(text))
            return Collections.singletonList(text);
        List<String> prefixTokens = prefixSegment(vocabs, text);
        List<String> suffixTokens = suffixSegment(vocabs, text);
        if (suffixTokens.size() < prefixTokens.size())
            return suffixTokens;
        return prefixTokens;
    }

    // 前缀
    public static List<String> prefixSegment(Set<String> vocabs, String text) {
        List<String> result = new ArrayList<>();
        int begin = 0;
        int end = text.length();

        while (begin < end) {
            for (; end > begin; end--) {
                String is = text.substring(begin, end);
                if (is.length() > 1) {
                    if (vocabs.contains(is)) {
                        result.add(is);
                        begin = end;
                        end = text.length();
                        break;
                    }
                } else {
                    result.add(is);
                    begin = end;
                    end = text.length();
                    break;
                }
            }
        }
        return result;
    }

    // 后缀
    public static List<String> suffixSegment(Set<String> vocabs, String text) {
        List<String> result = new ArrayList<>();
        int begin = 0;
        int end = text.length();
        while (begin < end) {
            for (; begin < end; begin++) {
                String is = text.substring(begin, end);
                if (is.length() > 1) {
                    if (vocabs.contains(is)) {
                        result.add(is);
                        begin = 0;
                        end -= is.length();
                        break;
                    }
                } else {
                    result.add(is);
                    begin = 0;
                    end -= is.length();
                    break;
                }
            }
        }
        Collections.reverse(result);
        return result;
    }

    // 最大频次分词
    public static List<String> maxProbSegment(Map<String, Double> weights, String text) {
        if (text.isEmpty() || weights.containsKey(text))
            return Collections.singletonList(text);
        double[] score = new double[text.length() + 1];
        int[] parent = new int[text.length() + 1];
        for (int i = text.length() - 1; i >= 0; i--) {
            for (int j = i + 1; j <= text.length(); j++) {
                String cand = text.substring(i, j);
                double sc = score[j] + weights.getOrDefault(cand, 0.0);
                if (sc >= score[i]) {
                    parent[i] = j;
                    score[i] = sc;
                }
            }
        }
        List<String> result = new ArrayList<>();
        int i = 0;
        while (i < text.length()) {
            result.add(text.substring(i, parent[i]));
            i = parent[i];
        }
        return result;
    }

    // 最小切分分词
    public static List<String> minSegment(Set<String> vocabs, String text) {
        if (text.isEmpty() || vocabs.contains(text))
            return Collections.singletonList(text);
        int[] score = new int[text.length() + 1];
        for (int i = 0; i < score.length; i++) {
            score[i] = Integer.MAX_VALUE;
        }
        int[] parent = new int[text.length() + 1];
        for (int i = text.length() - 1; i >= 0; i--) {
            for (int j = i + 1; j <= text.length(); j++) {
                String cand = text.substring(i, j);
                if (vocabs.contains(cand) || cand.length() < 2) {
                    int sc = score[j] + 1;
                    if (sc <= score[i]) {
                        parent[i] = j;
                        score[i] = sc;
                    }
                }
            }
        }
        List<String> result = new ArrayList<>();
        int i = 0;
        while (i < text.length()) {
            result.add(text.substring(i, parent[i]));
            i = parent[i];
        }
        return result;
    }

    public static void loadCustomDictionary(String path, String delimiter) {
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
