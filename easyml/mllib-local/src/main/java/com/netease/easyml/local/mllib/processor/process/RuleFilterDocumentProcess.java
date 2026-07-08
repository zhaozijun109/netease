package com.netease.easyml.local.mllib.processor.process;

import com.hankcs.hanlp.seg.common.Term;
import com.netease.easyml.common.collection.Counter;
import com.netease.easyml.common.collection.Tuple;
import com.netease.easyml.local.mllib.bean.Constant;
import com.netease.easyml.local.mllib.bean.Document;
import com.netease.easyml.local.mllib.bean.Sentence;
import com.netease.easyml.local.mllib.bean.SentenceType;
import com.netease.easyml.local.mllib.processor.cleaner.impl.RegexCleaner;
import com.netease.easyml.local.mllib.processor.filter.impl.RegexFilter;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by eddielin on 2019/3/1.
 * 基于规则过滤句子
 */
@Slf4j
@AllArgsConstructor
public class RuleFilterDocumentProcess implements DocumentProcess {
    private static final Pattern NUMBER = Pattern.compile("\\d+");
    private static final String IN_CLOSE_PATTERN = "[\\[【(（｢「『《][^\\]】)）｣」』》]*[\\]】)）｣」』》]";
    private static final RegexCleaner INCLOSE_CLEANER = new RegexCleaner(IN_CLOSE_PATTERN);
    private static final Pattern SPECIAL_PT = Pattern.compile("[:：][\u4e00-\u9fa50-9a-zA-Z]+[:：]");

    private RegexFilter filter;
    private RegexFilter weakFilter;
    private double zhRatio;
    private boolean allowEmpty;
    private boolean lowQuality;

    public boolean legal(Sentence sentence) {
        String origin = sentence.getOrigin();
        String cSpace = origin.replaceAll("\\s+", "").trim();
        if (filter.filter(cSpace))
            return false;

        if (sentence.getTerms().size() < 10) {
            if (weakFilter.filter(cSpace))
                return false;
        }

        if (zhRatio > 0) {
            String replace = origin.replaceAll("\\s+", " ");

            String cnStr = replace.replaceAll("[^\u4e00-\u9fa50-9a-zA-Z ]", "");
            cnStr = cnStr.replaceAll("[^\u4e00-\u9fa5 ]{7,}", "");

            // 中文字符和短英文比例
            if (cnStr.length() * 1.0 / origin.length() < zhRatio) {
                return false;
            }
        }


        Set<String> bwords = new HashSet<>();
        Matcher m = weakFilter.getPattern().matcher(cSpace);
        while (m.find()) {
            String group = m.group();
            bwords.add(group);
        }
        if (bwords.size() > 1)
            return false;

        String[] split = origin.replaceAll("[0-9a-zA-Z]", "").split("[\\s+]");
        List<String> collect = Arrays.stream(split).filter(it -> it.length() > 4 && it.length() < 15).collect(Collectors.toList());
        if (collect.size() > 5 && collect.size() * 1.0 / split.length > 0.5) {
            log.debug("Low quality too many space: " + sentence);
            return false;
        }

        split = origin.split("[/+\\\\]");
        collect = Arrays.stream(split).filter(it -> {
            String trim = it.replaceAll("[0-9a-zA-Z]", "").trim();
            return it.length() > 4 || !trim.isEmpty();
        }).collect(Collectors.toList());
        if (collect.size() > 3) {
            log.debug("Low quality too many /+\\: " + sentence);
            return false;
        }

        // 低质量句子
        Counter<String> counter = new Counter<>();
        int range = 0;
        List<Term> terms = sentence.getTerms();
        for (int i = 0; i < terms.size(); i++) {
            int j = i;
            while (j < terms.size() && !terms.get(j).nature.startsWith("w")) {
                if (terms.get(j).word.length() > 1)
                    counter.add(terms.get(j).word);
                j++;
            }
            if (j - i > range) {
                range = j - i;
            }
            i = j;
        }

        if (range > 25) {
            log.debug("Low quality too long: " + sentence);
            return false;
        }

        if (terms.size() < 50) {
            List<Tuple<String, Integer>> tuples = counter.mostCommon();
            if (!tuples.isEmpty() && terms.size() > 25 && tuples.get(0).v2() > 5) {
                log.debug("Low quality duplicate words: " + sentence);
                return false;
            }
        }

        if (SPECIAL_PT.matcher(sentence.getOrigin()).find()) {
            return false;
        }

        String replace = INCLOSE_CLEANER.replace("", origin).trim();
        if (replace.length() < 0.3 * origin.length())
            return false;

        return true;
    }

    @Override
    public void process(Document document) {
        List<Sentence> sentences = document.getSentences();
        boolean[] flags = new boolean[sentences.size()];
        for (int i = 0; i < sentences.size(); i++) {
            Sentence sentence = sentences.get(i);
            if (!Objects.equals(sentence.getType(), SentenceType.TEXT)) {
                flags[i] = true;
                continue;
            }
            if (!allowEmpty && sentence.getClean().trim().isEmpty()) {
                flags[i] = false;
                log.debug("Illegal empty: " + sentence);
                continue;
            }

            if (legal(sentence))
                flags[i] = true;
            else {
                flags[i] = false;
                log.debug("Illegal: " + sentence);
            }
        }
        List<Sentence> fSents = new ArrayList<>();
        for (int i = 0; i < flags.length; i++) {
            if (flags[i])
                fSents.add(sentences.get(i));
        }
        document.setSentences(fSents);
    }

    @Accessors(chain = true)
    @Setter
    public static class Builder {
        private RegexFilter filter = new RegexFilter(
                Constant.BLACK_PATTERN.pattern()
        );
        private RegexFilter weakFilter = new RegexFilter(
                Arrays.stream(Constant.BLACK_WORDS).map(Pattern::quote).collect(Collectors.joining("|")),
                Constant.SALE_PATTERN.pattern()
        );
        private double zhRatio = 0.5;
        private boolean allowEmpty = false;
        private boolean lowQuality = false;

        public RuleFilterDocumentProcess build() {
            return new RuleFilterDocumentProcess(filter, weakFilter, zhRatio, allowEmpty, lowQuality);
        }
    }
}
