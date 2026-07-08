package com.netease.easyml.local.mllib.processor.process;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.netease.easyml.common.util.StringUtil;
import com.netease.easyml.local.mllib.bean.Document;
import com.netease.easyml.local.mllib.bean.HTMLInfo;
import com.netease.easyml.local.mllib.bean.Sentence;
import com.netease.easyml.local.mllib.bean.SentenceType;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by eddielin on 2019/3/30.
 */
@Slf4j
public class SentenceSplitDocumentProcess implements DocumentProcess {
    public static final String SENTENCE_SEP = "。？?！!；;…\n\r";
    public static final String SENTENCE_SEP_PATTERN = "((([" + Pattern.quote(SENTENCE_SEP) + "][”\"》]?)|(?<![0-9])~(?![0-9]))+)";

    public static final String FG_SENTENCE_SEP = "、,，。？?！!；;:：~———|丨│｜…\n\r";
    public static final String FG_SENTENCE_SEP_PATTERN = "(([" + Pattern.quote(FG_SENTENCE_SEP) + "][”\"》]?)+)";

    public static final String SEP = "@SEP@";

    public static List<String> splitSentence(String text) {
        return splitSentence(text, false, false);
    }

    public static List<String> splitSentence(String text, boolean keep, boolean fineGrained) {
        String delimiters;
        if (fineGrained)
            delimiters = FG_SENTENCE_SEP_PATTERN;
        else
            delimiters = SENTENCE_SEP_PATTERN;
        if (keep) {
            text = text.replaceAll(delimiters, "$1" + SEP);
            delimiters = SEP;
        }
        return Arrays.asList(text.split(delimiters));
    }

    private Segment segment;
    private boolean fineGained = false;

    public SentenceSplitDocumentProcess(boolean fineGained) {
        this.fineGained = fineGained;
    }

    public SentenceSplitDocumentProcess() {
    }

    public SentenceSplitDocumentProcess(Segment segment) {
        this.segment = segment;
    }

    public SentenceSplitDocumentProcess(Segment segment, boolean fineGained) {
        this.segment = segment;
        this.fineGained = fineGained;
    }

    @Override
    public void process(Document document) {
        List<Sentence> nSentences = new ArrayList<>();
        List<Sentence> sentences = document.getSentences();
        int tPid = 0;
        for (int i = 0; i < sentences.size(); i++) {
            Sentence sentence = sentences.get(i);
            Map<HTMLInfo, Object> htmlInfos = sentence.getHtmlInfos();
            if (!Objects.equals(sentence.getType(), SentenceType.TEXT)) {
                nSentences.add(sentence);
                continue;
            }
            Sentence.Id startId = sentence.getStartId();
            String origin = sentence.getOrigin();
            List<Term> terms = sentence.getTerms();
            List<String> units = splitSentence(origin, true, fineGained);
            int k = 0;
            boolean flag = true;
            for (int j = 0; j < units.size(); j++) {
                Sentence s = new Sentence();
                String o = units.get(j);
                int len = 0;
                List<Term> nTerms = new ArrayList<>();
                while (k < terms.size() && len < o.length()) {
                    Term t = terms.get(k);
                    nTerms.add(t);
                    len += t.word.length();
                    k++;
                }
                if (len != o.length() || !flag) {
                    if (segment == null)
                        nTerms = HanLP.segment(o);
                    else
                        nTerms = segment.seg(o);
                    flag = false;
                }
                int offset = startId.getOffset() >= 0 ? startId.getOffset() : i;
                int pid = startId.getParaId() >= 0 ? startId.getParaId() : tPid;
                tPid++;
                s.setOrigin(o);
                s.setId(Sentence.Id.id(offset, pid, j));
                s.setTerms(nTerms);
                s.setHtmlInfos(htmlInfos);
                nSentences.add(s);
            }
            if (!flag) {
                log.warn("Origin: " + origin + "\tunits: " + StringUtil.join(units, " @@ "));
            }
        }
        document.setSentences(nSentences);
    }
}
