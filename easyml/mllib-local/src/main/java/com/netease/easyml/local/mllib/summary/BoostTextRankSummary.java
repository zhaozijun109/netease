package com.netease.easyml.local.mllib.summary;

import com.netease.easyml.local.mllib.PageRank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * Created by eddielin on 2019/2/19.
 */
public class BoostTextRankSummary extends PageRankBasedSummary {
    private Params boostParams;

    public BoostTextRankSummary(Similarity similarity, Selector selector, PageRank.Params params) {
        this(similarity, selector, params, new Params());
    }

    public BoostTextRankSummary(Similarity similarity, Selector selector, PageRank.Params params, Params boostParams) {
        super(similarity, selector, params);
        this.boostParams = boostParams;
    }

    @Override
    protected Map<Integer, Double> rank(double[][] similarity) {
        Map<Integer, Double> ranks = super.rank(similarity);
        int topK = boostParams.getTopK();
        double boostFactor = boostParams.getBoostFactor();

        // boost score for the first 3 sentences
        for (int i = 0; i < topK; ++i) {
            if (ranks.containsKey(i)) {
                double newScore = ranks.get(i) * boostFactor;
                ranks.put(i, newScore);
            }
        }

        double boostFirstFactor = boostParams.getBoostFirstFactor();
        if (!ranks.isEmpty() && boostFirstFactor > 0) {
            // boost first id
            int firstId = ranks.keySet().stream().min(Integer::compareTo).get();
            if (ranks.containsKey(firstId)) {
                double oldValue = ranks.get(firstId);
                ranks.put(firstId, oldValue * 10.0);
            }
        }
        return ranks;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Params {
        private int topK = 3;
        private double boostFactor = 1.2;
        private double boostFirstFactor = 10.0;
    }
}
