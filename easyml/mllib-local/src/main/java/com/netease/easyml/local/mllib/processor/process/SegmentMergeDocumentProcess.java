package com.netease.easyml.local.mllib.processor.process;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.netease.easyml.local.mllib.bean.Document;
import com.netease.easyml.local.mllib.bean.Sentence;
import com.netease.easyml.local.mllib.bean.SentenceType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.netease.easyml.local.mllib.processor.process.AutoPunctDocumentProcess.PUCNT_PATTERN;

/**
 * Created by eddielin on 2019/4/2.
 */
@Slf4j
@AllArgsConstructor
public class SegmentMergeDocumentProcess implements DocumentProcess {
    private static final Pattern IGNORE_END = Pattern.compile("[0-9a-zA-Z]+$");
    private static final Pattern IGNORE_START = Pattern.compile("^[0-9a-zA-Z]+");
    private Segment segment;
    private boolean cross = false;

    public SegmentMergeDocumentProcess(Segment segment) {
        this.segment = segment;
    }

    @Override
    public void process(Document document) {
        List<Sentence> sentences = document.getSentences();

        List<Sentence> tmp = new ArrayList<>();
        List<Sentence> nSentences = new ArrayList<>();
        for (Sentence cur : sentences) {
            if (nSentences.isEmpty()) {
                nSentences.add(cur);
                continue;
            }
            if (!Objects.equals(cur.getType(), SentenceType.TEXT)) {
                tmp.add(cur);
                continue;
            }

            if (!cross && !tmp.isEmpty()) {
                nSentences.addAll(tmp);
                nSentences.add(cur);
                tmp = new ArrayList<>();
                continue;
            }

            Sentence pre = nSentences.get(nSentences.size() - 1);
            String preSentence = pre.getOrigin();
            if (PUCNT_PATTERN.matcher(preSentence).find()) {
                nSentences.addAll(tmp);
                nSentences.add(cur);
                tmp = new ArrayList<>();
                continue;
            }

            String curSentence = cur.getOrigin();

            if (IGNORE_END.matcher(preSentence).find() && IGNORE_START.matcher(curSentence).find()) {
                nSentences.add(cur);
                continue;
            }

            String concatSentence = preSentence + curSentence;
            List<Term> terms = segment != null ? segment.seg(concatSentence) : HanLP.segment(concatSentence);
            StringBuilder accSentence = new StringBuilder();
            int idx = 0;
            while (accSentence.length() < preSentence.length()) {
                accSentence.append(terms.get(idx).word);
                idx += 1;
            }
            if (accSentence.length() != preSentence.length()) {
                log.debug(String.format("Merge: %s ++ %s", preSentence, curSentence));
                nSentences.remove(nSentences.size() - 1);
                pre = Sentence.concat(pre, cur);
                nSentences.add(pre);
            } else {
                nSentences.addAll(tmp);
                nSentences.add(cur);
            }
            tmp = new ArrayList<>();
        }
        document.setSentences(nSentences);
    }
}
