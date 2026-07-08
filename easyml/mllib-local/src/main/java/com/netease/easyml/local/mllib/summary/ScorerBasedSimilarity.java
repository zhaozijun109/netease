package com.netease.easyml.local.mllib.summary;

import com.netease.easyml.local.mllib.scorer.impl.IScorer;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Created by eddielin on 2019/2/18.
 */
@AllArgsConstructor
public class ScorerBasedSimilarity implements Similarity {
    private IScorer<List<String>> scorer;
    private double threshold;
    private double diagValue;

    public ScorerBasedSimilarity(IScorer<List<String>> scorer, double threshold) {
        this(scorer, threshold, 0.0);
    }

    public ScorerBasedSimilarity(IScorer<List<String>> scorer) {
        this(scorer, 0.0);
    }

    @Override
    public double[][] score(List<List<String>> tokens) {
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
