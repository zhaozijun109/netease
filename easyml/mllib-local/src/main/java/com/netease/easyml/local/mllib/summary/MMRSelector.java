package com.netease.easyml.local.mllib.summary;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by eddielin on 2019/2/18.
 */
@AllArgsConstructor
public class MMRSelector implements Selector {
    private static final double LAMBDA = 0.5;
    private static final int DIFF = 20;
    private double maxLen;
    private double lambda;
    private boolean charLevel;

    public MMRSelector(double maxLen) {
        this(maxLen, LAMBDA);
    }

    public MMRSelector(double maxLen, double lambda) {
        this(maxLen, lambda, true);
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
            double maxscore = Double.NEGATIVE_INFINITY;
            int pick = -1;
            for (int i : ranks.keySet()) {
                if (chosen[i]
                        || len + sentLen.get(i) >= maxLen)
                    continue;
                double tmpscore;
                if (summaryId.isEmpty()) {
                    tmpscore = 0.0;
                } else {
                    tmpscore = Double.NEGATIVE_INFINITY;
                    for (int j : summaryId) {
                        if (similarity[i][j] > tmpscore)
                            tmpscore = similarity[i][j];
                    }
                }
                double tmp = lambda * ranks.get(i) - (1 - lambda) * tmpscore;
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
