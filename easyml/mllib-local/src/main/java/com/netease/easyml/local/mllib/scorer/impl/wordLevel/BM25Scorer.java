package com.netease.easyml.local.mllib.scorer.impl.wordLevel;

import com.netease.easyml.local.mllib.scorer.impl.IScorer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linjiuning on 2018/6/3.
 */
public class BM25Scorer implements IScorer<List<String>> {

    private final double PARAM_K1 = 1.5;
    private final double PARAM_B = 0.75;
    private final double EPSILON = 0.25;

    private Map<String, Double> idf;
    private double defaultIdf;
    private double averageLength;

    public BM25Scorer(Map<String, Double> idf, double defaultIdf, double averageLength) {
        this.idf = idf;
        this.defaultIdf = defaultIdf;
        this.averageLength = averageLength;
    }

    @Override
    public double getScores(List<String> v1, List<String> v2) {
        Map<String, Integer> counter = new HashMap<>();
        for (String word : v2) {
            counter.put(word, counter.getOrDefault(word, 0) + 1);
        }
        double score = 0.0;
        for (String word : v1) {
            if (!counter.containsKey(word))
                continue;
            double idf_ = idf.getOrDefault(word, defaultIdf);
            if (idf_ < 0)
                idf_ = averageLength * EPSILON;
            score += idf_ * counter.get(word) * (PARAM_K1 + 1) / (counter.get(word) + PARAM_K1 * (1 - PARAM_B + PARAM_B * v2.size() / averageLength));
        }
        return score;
    }

    public void setAverageLength(double newAvgLen) {
        this.averageLength = newAvgLen;
    }
}
