package com.netease.easyml.local.mllib.processor.process;

import com.netease.easyml.common.util.StringUtil;
import com.netease.easyml.local.mllib.bean.Document;
import com.netease.easyml.local.mllib.bean.Sentence;
import com.netease.easyml.local.mllib.bean.SentenceType;
import com.netease.easyml.local.mllib.summary.SummaryPostProcess;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by eddielin on 2019/4/24.
 * 基于编号对句子进行合并
 */
@Slf4j
@AllArgsConstructor
public class LabelMergeDocumentProcess implements DocumentProcess {
    private Pattern label;
    private boolean cross;
    private int maxLen;
    private int minNum;

    public String getContainLabel(Sentence cur) {
        String origin = cur.getOrigin();
        List<String> units = SentenceSplitDocumentProcess.splitSentence(origin, true, true);
        for (String unit : units) {
            Matcher matcher = label.matcher(unit);
            if (matcher.find())
                return matcher.group();
        }
        return "";
    }

    public String getLabel(Sentence cur) {
        String origin = cur.getOrigin();
        Matcher matcher = label.matcher(origin);
        if (matcher.find())
            return matcher.group();
        return "";
    }

    @Override
    public void process(Document document) {
        List<Sentence> sentences = document.getSentences();

        List<Sentence> newSents = new ArrayList<>();
        int i = 0;
        while (i < sentences.size()) {
            Sentence cur = sentences.get(i);
            if (newSents.isEmpty() || !Objects.equals(cur.getType(), SentenceType.TEXT)) {
                newSents.add(cur);
                i++;
                continue;
            }

            List<String> labels = new ArrayList<>();
            int len = 0;
            String containLabel = getContainLabel(cur);
            if (containLabel.isEmpty()) {
                newSents.add(cur);
                i++;
                continue;
            }
            len += cur.getOrigin().length();
            labels.add(containLabel);
            int j = i + 1;
            while (j < sentences.size()) {
                cur = sentences.get(j);
                if (!Objects.equals(cur.getType(), SentenceType.TEXT)) {
                    j++;
                    continue;
                }
                if (!(cross || Objects.equals(sentences.get(j - 1).getType(), SentenceType.TEXT)))
                    break;
                String label = getLabel(cur);
                if (label.isEmpty())
                    break;
                labels.add(label);
                len += cur.getOrigin().length();
                j++;
            }
            //TODO: check label format is the same

            int minNum = Math.max(this.minNum, 2);
            if (labels.size() >= minNum && (maxLen <= 0 || len < maxLen)) {
                List<Sentence> tmp = new ArrayList<>();
                for (int k = i; k < j; k++) {
                    if (Objects.equals(sentences.get(k).getType(), SentenceType.TEXT))
                        tmp.add(sentences.get(k));
                }
                log.debug("Merge: " + StringUtil.join(tmp, "++"));
                Sentence concat = Sentence.concat(tmp.get(0), tmp.subList(1, tmp.size()).toArray(new Sentence[0]));
                newSents.add(concat);
                i = j;
            } else {
                newSents.add(sentences.get(i));
                i++;
            }
        }
        document.setSentences(newSents);
    }

    @Accessors(chain = true)
    @Setter
    public static class Builder {
        private Pattern label = SummaryPostProcess.LABEL_PATTERN;
        private boolean cross = true;
        private int maxLen = 150;
        private int minNum = 2;

        public LabelMergeDocumentProcess build() {
            return new LabelMergeDocumentProcess(label, cross, maxLen, minNum);
        }
    }
}
