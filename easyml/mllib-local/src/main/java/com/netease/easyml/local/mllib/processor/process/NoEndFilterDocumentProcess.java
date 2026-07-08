package com.netease.easyml.local.mllib.processor.process;

import com.netease.easyml.local.mllib.bean.Document;
import com.netease.easyml.local.mllib.bean.Sentence;
import com.netease.easyml.local.mllib.bean.SentenceType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by eddielin on 2019/4/16.
 * 过滤没有标点结尾的段落
 */
@Slf4j
public class NoEndFilterDocumentProcess implements DocumentProcess {
    private double paraRatio = 0.4;
    private double lenRatio = 0.8;

    public NoEndFilterDocumentProcess() {
    }

    public NoEndFilterDocumentProcess(double paraRatio, double lenRatio) {
        this.paraRatio = paraRatio;
        this.lenRatio = lenRatio;
    }

    @Override
    public void process(Document document) {
        List<Sentence> textSentences = document.getTextSentences();
        if (textSentences.isEmpty())
            return;
        int good = 0;
        int goodLen = 0;
        int len = 0;
        for (Sentence sentence : textSentences) {
            String origin = sentence.getOrigin();
            if (AutoPunctDocumentProcess.endsWithPunct(origin)) {
                good++;
                goodLen += origin.length();
            }
            len += origin.length();
        }
        double lRatio = goodLen * 1. / len;
        double ratio = good * 1. / textSentences.size();
        if (ratio > paraRatio && lRatio > lenRatio) {
            List<Sentence> nSentences = new ArrayList<>();
            for (Sentence sentence : document.getSentences()) {
                String origin = sentence.getOrigin();
                if (Objects.equals(sentence.getType(), SentenceType.TEXT)
                        && !AutoPunctDocumentProcess.endsWithPunct(origin)) {
                    log.debug("End punct filter: " + origin);
                    continue;
                }
                nSentences.add(sentence);
            }
            document.setSentences(nSentences);
        }
    }
}
