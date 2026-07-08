package com.netease.easyml.local.mllib.summary;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Created by eddielin on 2019/2/18.
 */
@AllArgsConstructor
public abstract class AbstractSimilarityBasedSummary implements Summary {
    private Similarity similarity;
    private Selector selector;

    protected abstract Map<Integer, Double> rank(double[][] similarity);

    public Map<Integer, Double> rank(List<List<String>> tokens) {
        double[][] scores = similarity.score(tokens);
        return rank(scores);
    }

    @Override
    public List<Integer> transform(List<List<String>> tokens) {
        double[][] scores = similarity.score(tokens);
        Map<Integer, Double> ranks = rank(scores);
        return selector.select(tokens, scores, ranks);
    }
}
