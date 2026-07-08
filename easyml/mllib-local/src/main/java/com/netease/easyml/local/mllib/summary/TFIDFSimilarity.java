package com.netease.easyml.local.mllib.summary;

import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Created by eddielin on 2019/2/19.
 */
@AllArgsConstructor
public class TFIDFSimilarity implements Similarity {
    private double diagValue;
    private double threshold;

    public TFIDFSimilarity() {
        this.diagValue = 0.0;
        this.threshold = 0.0;
    }

    @Override
    public double[][] score(List<List<String>> tokens) {
        TFIDFScorer scorer = TFIDFScorer.getScorer(tokens);
        int snum = tokens.size();
        double[][] similarity = new double[snum][snum];
        for (int i = 0; i < snum; i++) {
            similarity[i][i] = diagValue;
            for (int j = i + 1; j < snum; j++) {
                double sc = scorer.getScores(tokens.get(i), tokens.get(j));
                if (sc > threshold)
                    similarity[i][j] = similarity[j][i] = sc;
            }
        }
        return similarity;
    }
}
