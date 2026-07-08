package com.netease.easyml.local.mllib.summary;


import com.netease.easyml.local.mllib.PageRank;

import java.util.Map;

/**
 * Created by eddielin on 2019/2/18.
 */
public class PageRankBasedSummary extends AbstractSimilarityBasedSummary {
    private PageRank.Params params;

    public PageRankBasedSummary(Similarity similarity, Selector selector, PageRank.Params params) {
        super(similarity, selector);
        this.params = params;
    }

    @Override
    protected Map<Integer, Double> rank(double[][] similarity) {
        return PageRank.fit(params, similarity);
    }
}
