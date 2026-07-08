package com.netease.easyml.local.mllib.summary;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by eddielin on 2019/2/18.
 */
@AllArgsConstructor
public class ThresholdSelector implements Selector {
    private static final double THRESHOLD = 0.7;
    private static final double BETA = 0.1;
    private static final int MIN_SENT_LEN = 5;
    private static final int DIFF = 20;
    private double maxLen;
    private double threshold;
    private double beta;
    private boolean charLevel;

    public ThresholdSelector(double maxLen, double threshold) {
        this(maxLen, threshold, BETA, true);
    }

    @Override
    public List<Integer> select(List<List<String>> tokens, double[][] similarity, Map<Integer, Double> ranks) {
        List<Integer> summaryId = new ArrayList<>();
        int len = 0;
        int snum = tokens.size();
        List<Integer> sentLen = Utils.getSentenceLength(tokens, charLevel);
        boolean[] chosen = new boolean[snum];
        for (int i = 0; i < snum; i++)
            chosen[i] = false;
        while (len < maxLen) {
            double maxscore = 0;
            int pick = -1;
            for (int i : ranks.keySet()) {
                if (chosen[i]
                        || len + sentLen.get(i) >= maxLen
                        || sentLen.get(i) < MIN_SENT_LEN)
                    continue;
                double tmpscore = ranks.get(i);

                for (int j : summaryId) {
                    if (similarity[i][j] > threshold)
                        tmpscore = 0;
                }
                double tmp = tmpscore / Math.pow(sentLen.get(i), beta);
                if (tmp > maxscore) {
                    maxscore = tmp;
                    pick = i;
                }
            }
            if (pick == -1)
                break;
            chosen[pick] = true;
            len += sentLen.get(pick);
            summaryId.add(pick);
            if (len >= maxLen - DIFF)
                break;
        }
        return summaryId;
    }
}
