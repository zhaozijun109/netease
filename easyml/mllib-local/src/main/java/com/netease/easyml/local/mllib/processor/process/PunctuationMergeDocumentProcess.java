package com.netease.easyml.local.mllib.processor.process;

import com.hankcs.hanlp.seg.common.Term;
import com.netease.easyml.local.mllib.bean.Document;
import com.netease.easyml.local.mllib.bean.Sentence;
import com.netease.easyml.local.mllib.bean.SentenceType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Created by eddielin on 2019/4/24.
 * 基于标点对句子进行合并
 */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class PunctuationMergeDocumentProcess implements DocumentProcess {
    private Set<String> punctuation = new HashSet<>(Arrays.asList(",", "，", "、", ":", "：", "—", "——"));
    private int maxCross = 3; // 最多跨越的非文本段

    public boolean isLegalEnd(Sentence cur) {
        List<Term> cleanTerms = cur.getCleanTerms();
        if (!cleanTerms.isEmpty()) {
            String last = cleanTerms.get(cleanTerms.size() - 1).word;
            if (punctuation.contains(last))
                return false;
        }
        return true;
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

            if (!Objects.equals(before.getType(), SentenceType.TEXT)) {
                newSents.add(cur);
                continue;
            }

            if (!isLegalEnd(before)
                    && tmp.stream().noneMatch(it -> it.getType().equals(SentenceType.SUBTITLE))
                    && (maxCross <= 0 || tmp.size() <= maxCross)
                    && (cur.getStartId().getOffset() <= before.getEndId().getOffset() + 2)) {
                log.debug(String.format("Merge: %s ++ %s", before, cur));
                before = Sentence.concat(before, cur);
                newSents.set(newSents.size() - 1, before); // 去除中间夹杂的图片,小标题,图片标题
            } else {
                if (!isLegalEnd(before) && !newSents.isEmpty()) {
                    log.debug("Remove illegal end: " + before);
                    newSents.remove(newSents.size() - 1);
                }
                newSents.addAll(tmp);
                newSents.add(cur);
            }
            tmp = new ArrayList<>();
        }
        document.setSentences(newSents);
    }
}
