package com.netease.easyml.local.mllib.processor.process;

import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;
import com.netease.easyml.common.util.CollectionUtil;
import com.netease.easyml.common.util.StringUtil;
import com.netease.easyml.local.mllib.bean.Document;
import com.netease.easyml.local.mllib.bean.Sentence;
import com.netease.easyml.local.mllib.bean.SentenceType;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by eddielin on 2019/2/25.
 */
@AllArgsConstructor
@Slf4j
public class AutoPunctDocumentProcess implements DocumentProcess {
    public static final String NO_END_PUNCT = ",，：:、";
    public static final String END_PUNCT = ".。?？!！~;；…—";
    public static final String PUNCT = NO_END_PUNCT + END_PUNCT;
//    public static final Set<Character> PUNCT_SET = new HashSet<>();

    public static final Pattern PUCNT_PATTERN = Pattern.compile("[" + PUNCT + "]" + "[\"”“]?$");

    public static final String FG_SENTENCE_SEP = ",，。？?！!；;~-—…";
    public static final String FG_SENTENCE_SEP_KEEP_PATTERN = "(?<=[" + Pattern.quote(FG_SENTENCE_SEP) + "])";

    public static boolean endsWithPunct(String text) {
        return PUCNT_PATTERN.matcher(text).find();
    }

//    static {
//        for (char ch : PUNCT.toCharArray())
//            PUNCT_SET.add(ch);
//    }

    private AutoPunct autoPunct;
    private double threshold;

    // 处理跨段落
    private boolean allowCross;  // 是否处理跨段落
    private double crossThreshold; // 跨段落合并阈值
    private double crossBoostEnd; // 结尾标点加权
    private int crossParas; // 段落id差大于该值, 则boost end
    private Set<String> endPunct; // 结尾标点, 用于boost

    private Map<String, String> mapping; // 标点映射

    // 准备pair对, 长度截取
    private int windowUnits; // unit窗口大小
    private int maxChars; // 字符窗口大小
    private int minChars; // 最小长度

    private String choice(List<String> units, boolean reverse) {
        List<String> strings = new ArrayList<>();
        int len = 0;
        for (int i = 0; i < units.size(); i++) {
            if (reverse)
                strings.add(0, units.get(i));
            else
                strings.add(units.get(i));
            len += units.get(i).length();
            boolean isLegalLen = minChars <= 0 || len >= minChars;
            if (windowUnits > 0 && i + 1 >= windowUnits && isLegalLen)
                break;
        }
        String text = StringUtil.join(strings, "");
        if (maxChars > 0 && text.length() > maxChars) {
            if (reverse)
                text = text.substring(text.length() - maxChars);
            else
                text = text.substring(0, maxChars);
        }
        return text;
    }

    private List<List<String>> truncate(List<List<String>> pairs) {
        Map<String, List<String>> units = new HashMap<>();
        for (List<String> pair : pairs) {
            for (String text : pair) {
                if (units.containsKey(text))
                    continue;
                List<String> strings = splitSentence(text);
                units.put(text, strings);
            }
        }
        List<List<String>> proc = new ArrayList<>();
        for (List<String> pair : pairs) {
            List<String> units1 = units.get(pair.get(0));
            List<String> units2 = units.get(pair.get(1));
            Collections.reverse(units1);
            String text1 = choice(units1, true);
            String text2 = choice(units2, false);
            proc.add(Arrays.asList(text1, text2));
        }
        return proc;
    }

    public void process(Document document) {
        List<String> texts = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        List<Integer> group = new ArrayList<>();
        boolean empty = true;
        int gId = 0;
        List<Sentence> contentArray = document.getSentences();
        for (int i = 0; i < contentArray.size(); i++) {
            Sentence sentence = contentArray.get(i);
            SentenceType type = sentence.getType();
            if (!Objects.equals(type, SentenceType.TEXT)) {
                if (!empty) {
                    empty = true;
                    gId++;
                }
                continue;
            }
            String desc = sentence.getOrigin();
            desc = desc.trim();
            if (!desc.isEmpty()) {
                texts.add(desc);
                indices.add(i);
                group.add(gId);
                empty = false;
            }
        }
        List<List<String>> pairs = new ArrayList<>();
        List<Integer> indicesOfTexts = new ArrayList<>();

        for (int i = 0; i < texts.size() - 1; i++) {
            int j = i + 1;
            String text = texts.get(i);

//            if (PUNCT_SET.contains(text.charAt(text.length() - 1)))
            if (PUCNT_PATTERN.matcher(text).find())
                continue;
            if (allowCross || indices.get(j) - indices.get(i) == 1) {
                pairs.add(Arrays.asList(texts.get(i), texts.get(j)));
                indicesOfTexts.add(i);
            }
        }

        if (minChars > 0 || windowUnits > 0 || maxChars > 0)
            pairs = truncate(pairs);
        if (pairs.isEmpty())
            return;
        List<List<Map<String, Double>>> allProbs;
        try {
            allProbs = autoPunct.transformWithProb(pairs);
        } catch (Exception e) {
            return;
        }
        for (int i = 0; i < allProbs.size(); i++) {
            List<Map<String, Double>> probs = allProbs.get(i);
            if (probs.isEmpty())
                continue;
            int idx = indicesOfTexts.get(i);
            boolean cross = indices.get(idx + 1) - indices.get(idx) > crossParas;

            double noEndSumProb = 0.0;
            double endSumProb = 0.0;

            double noEndMaxProb = 0.0;
            double endMaxProb = 0.0;

            String noEndHost = "";
            String endHost = "";

            for (Map.Entry<String, Double> entry : probs.get(0).entrySet()) {
                String key = entry.getKey();
                double sc = entry.getValue();

                if (NO_END_PUNCT.contains(key)) {
                    noEndSumProb += sc;
                    if (sc > noEndMaxProb) {
                        noEndHost = key;
                        noEndMaxProb = sc;
                    }
                } else if (END_PUNCT.contains(key)) {
                    endSumProb += sc;
                    if (sc > endMaxProb) {
                        endHost = key;
                        endMaxProb = sc;
                    }
                }
            }

            if (cross)
                endSumProb += crossBoostEnd * (indices.get(idx + 1) - indices.get(idx) - crossParas);

            String host = endSumProb > noEndSumProb ? endHost : noEndHost;
            double maxProb = endSumProb > noEndSumProb ? endMaxProb : noEndMaxProb;

            if (mapping.containsKey(host)) {
                String tmp = mapping.get(host);
                log.debug(String.format("Mapping %s -> %s", host, tmp));
                host = tmp;
            }

            log.debug(String.format("Pair: %s %s, punct: %s %.4f, boost: %s", pairs.get(i).get(0), pairs.get(i).get(1),
                    host, maxProb, cross && endPunct.contains(host)));
            double t = cross ? crossThreshold : threshold;
            if (maxProb < t) {
                log.debug("Less than threshold, concat pairs.");
                host = "";
            }
            String text = texts.get(idx);
            text += host;
            texts.set(idx, text);
        }

        // write back to origin doc
        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            int j = indices.get(i);
            Sentence s = contentArray.get(j);
            String rText = s.getOrigin().trim();
            if (!text.equals(rText)) {
                s.setOrigin(text);
                if (!CollectionUtil.isEmpty(s.getTerms())) {
                    List<Term> terms = s.getTerms();
                    terms.add(new Term(text.substring(rText.length()), Nature.w));
                }
            }
        }
    }

    public static List<String> splitSentence(String text) {
        return StringUtil.split(text, FG_SENTENCE_SEP_KEEP_PATTERN);
    }

    @Accessors(chain = true)
    @Setter
    public static class Builder {
        private AutoPunct autoPunct;
        private double threshold = 0.01;
        private boolean allowCross = false;
        private double crossThreshold = 0.005;
        private double crossBoostEnd = 0.1;
        private int crossParas = 1;
        private String endPunct = END_PUNCT;
        private Map<String, String> mapping;
        private int windowUnits = 2;
        private int maxChars = 20;
        private int minChars = 4;

        public AutoPunctDocumentProcess build() {
            autoPunct.setThreshold(0);
            if (mapping == null) {
                mapping = new HashMap<>();
                mapping.put(":", "，");
                mapping.put("：", "，");
            }
            Set<String> endPunct_ = new HashSet<>(Arrays.asList(endPunct.split("")));
            return new AutoPunctDocumentProcess(autoPunct, threshold, allowCross, crossThreshold, crossBoostEnd, crossParas,
                    endPunct_, mapping, windowUnits, maxChars, minChars);
        }
    }
}
