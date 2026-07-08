package com.netease.easyml.local.mllib.processor.process;

import com.hankcs.hanlp.seg.common.Term;
import com.netease.easyml.local.mllib.bean.Constant;
import com.netease.easyml.local.mllib.bean.Document;
import com.netease.easyml.local.mllib.bean.Sentence;
import com.netease.easyml.local.mllib.bean.SentenceType;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Created by eddielin on 2019/3/1.
 * 基于规则对句子进行合并
 */
@Slf4j
@AllArgsConstructor
public class RuleMergeDocumentProcess implements DocumentProcess {
    private Set<String> words;
    private String[] pos;
    private Set<String> punctuation;
    private boolean cross; // 是否可以跨段落
    private boolean force; // 是否合并不以标点结尾的句子
    private int maxLen;
    private int maxCross; // 最多跨越的非文本段

    public boolean isLegalStart(Term term) {
        String word = term.word;
        String nature = term.nature.toString();
        if (words.contains(word))
            return false;
        for (String p : pos) {
            if (nature.startsWith(p)) {
                return false;
            }
        }
        return true;
    }

    public boolean isLegalStart(Sentence cur) {
        List<Term> cleanTerms = cur.getCleanTerms();

        if (!cleanTerms.isEmpty()) {
            Term first = cleanTerms.get(0);
            return isLegalStart(first);
        }
        return true;
    }

    public boolean isLegalEnd(Sentence cur) {
        List<Term> cleanTerms = cur.getCleanTerms();
        if (!cleanTerms.isEmpty()) {
            String last = cleanTerms.get(cleanTerms.size() - 1).word;
            if (punctuation.contains(last) || (force && Constant.ZH.matcher(last).find()))
                return false;
        }
        return true;
    }

    public boolean isLegalEnd(String cur) {
        if (!cur.isEmpty()) {
            String last = cur.substring(cur.length() - 1);
            if (punctuation.contains(last) || (force && Constant.ZH.matcher(last).find()))
                return false;
        }
        return true;
    }

    public boolean isIndependent(Sentence before, Sentence after) {
        if (maxLen > 0 && after.getOrigin().length() > maxLen)
            return true;
        return isLegalStart(after) && isLegalEnd(before);
    }

    @Override
    public void process(Document document) {
        List<Sentence> sentences = document.getSentences();

        List<Sentence> tmp = new ArrayList<>();
        List<Sentence> newSents = new ArrayList<>();
        for (Sentence cur : sentences) {
            if (newSents.isEmpty()) {
                newSents.add(cur);
                continue;
            }

            if (!Objects.equals(cur.getType(), SentenceType.TEXT)) {
                tmp.add(cur);
                continue;
            }
            Sentence before = newSents.get(newSents.size() - 1);

            if (!Objects.equals(cur.getType(), SentenceType.TEXT)) {
                newSents.add(cur);
                continue;
            }

            if (!isIndependent(before, cur) &&
                    (cross || before.getEndId().getParaId() == cur.getStartId().getParaId())
                    && (maxCross <= 0 || tmp.size() <= maxCross)) {
                log.debug(String.format("Merge: %s ++ %s", before, cur));
                before = Sentence.concat(before, cur);
                newSents.set(newSents.size() - 1, before); // 去除中间夹杂的图片,小标题,图片标题
            } else {
                newSents.addAll(tmp);
                newSents.add(cur);
            }
            tmp = new ArrayList<>();
        }
        document.setSentences(newSents);
    }

    @Accessors(chain = true)
    @Setter
    public static class Builder {
        private Set<String> words = Constant.NON_INDEPENDENT_FIRST_WORDS;
        private String[] pos = Constant.NON_INDEPENDENT_FIRST_POS_PREFIX;
        private Set<String> punctuation = new HashSet<>(Arrays.asList(Constant.NON_STOP_FLAGS));
        private boolean cross = false;
        private boolean force = false;
        private int maxLen = 0;
        private int maxCross = 3; // 最多跨越的非文本段

        public RuleMergeDocumentProcess build() {
            return new RuleMergeDocumentProcess(words, pos, punctuation, cross, force, maxLen, maxCross);
        }
    }
}
