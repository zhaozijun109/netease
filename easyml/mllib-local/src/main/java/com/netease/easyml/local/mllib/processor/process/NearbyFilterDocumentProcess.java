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
 * 基于假设：段落附近窗口的段落都被过滤了，则该段落也应该被过滤
 */
@Slf4j
public class NearbyFilterDocumentProcess implements DocumentProcess {
    private int nearbyWindow = 3;
    private double nearbyThreshold = 0.5;

    public NearbyFilterDocumentProcess() {
    }

    public NearbyFilterDocumentProcess(int nearbyWindow, double nearbyThreshold) {
        this.nearbyWindow = nearbyWindow;
        this.nearbyThreshold = nearbyThreshold;
    }

    private void nearby(List<Sentence> textSentences, int[] flags) {
        for (Sentence textSentence : textSentences) {
            if (textSentence.getOrigin().length() > 20)
                continue;
            int valid = 0;
            int cnt = 0;
            int sId = textSentence.getStartId().getParaId();
            int eId = textSentence.getEndId().getParaId();

            int i = sId - 1;
            while (i >= 0 && i >= sId - nearbyWindow) {
                valid += flags[i];
                cnt += flags[i] > 0 ? flags[i] : 1;
                i--;
                if (cnt >= nearbyWindow)
                    break;
            }
            i = eId + 1;
            while (i < flags.length && i <= eId + nearbyWindow) {
                valid += flags[i];
                cnt += flags[i] > 0 ? flags[i] : 1;
                i++;
                if (cnt >= 2 * nearbyWindow)
                    break;
            }
            if (valid * 1.0 / cnt < nearbyThreshold) {
                for (i = sId; i <= eId; i++)
                    flags[i] = 0;
            }
        }
    }

    @Override
    public void process(Document document) {
        List<Sentence> textSentences = document.getTextSentences();
        if (textSentences.isEmpty())
            return;
        Sentence last = textSentences.get(textSentences.size() - 1);
        int paraId = last.getEndId().getParaId();
        int[] flag = new int[paraId + 1];
        for (Sentence textSentence : textSentences) {
            int sId = textSentence.getStartId().getParaId();
            int eId = textSentence.getEndId().getParaId();
            for (int i = sId; i <= eId; i++)
                flag[i]++;
        }
        nearby(textSentences, flag);

        List<Sentence> nSents = new ArrayList<>();
        for (Sentence sentence : document.getSentences()) {
            if (!Objects.equals(sentence.getType(), SentenceType.TEXT)) {
                nSents.add(sentence);
                continue;
            }
            int sId = sentence.getStartId().getParaId();
            int eId = sentence.getEndId().getParaId();
            boolean legal = true;
            for (int i = sId; i <= eId; i++) {
                if (flag[i] == 0) {
                    legal = false;
                    break;
                }
            }
            if (legal) {
                nSents.add(sentence);
            } else {
                log.debug("Nearby filter: " + sentence);
            }
        }
        document.setSentences(nSents);
    }
}
