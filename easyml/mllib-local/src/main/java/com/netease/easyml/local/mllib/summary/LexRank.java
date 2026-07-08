package com.netease.easyml.local.mllib.summary;

import com.netease.easyml.local.mllib.PageRank;

import java.util.Map;

/**
 * Created by eddielin on 2019/2/18.
 */
public class LexRank extends PageRankBasedSummary {
    private static final double THRESHOLD = 0.1;
    private double threshold;

    public LexRank(Similarity similarity, Selector selector, PageRank.Params params) {
        this(similarity, selector, params, THRESHOLD);
    }

    public LexRank(Similarity similarity, Selector selector, PageRank.Params params, double threshold) {
        super(similarity, selector, params);
        this.threshold = threshold;
    }

    @Override
    public Map<Integer, Double> rank(double[][] similarity) {
        int snum = similarity.length;
        int[] C = new int[snum];

        for (int i = 0; i < snum; ++i) {
            C[i] = 0;
            for (int j = 0; j < snum; ++j) {
                if (i == j) continue;
                // TODO check why multiple 100?
                if (100 * similarity[i][j] >= threshold) {
                    C[i]++;
                    similarity[i][j] = 1;
                } else {
                    similarity[i][j] = 0;
                }
            }
        }

        for (int i = 0; i < snum; ++i) {
            for (int j = 0; j < snum; ++j) {
                if (C[j] > 0)
                    similarity[i][j] /= C[j];
            }
        }

        return super.rank(similarity);
    }
}
