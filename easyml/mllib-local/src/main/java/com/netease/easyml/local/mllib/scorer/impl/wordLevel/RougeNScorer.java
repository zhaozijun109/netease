package com.netease.easyml.local.mllib.scorer.impl.wordLevel;


import com.netease.easyml.common.collection.Counter;
import com.netease.easyml.common.collection.Triple;
import com.netease.easyml.local.mllib.Utils;
import com.netease.easyml.local.mllib.featurizer.impl.wordLevel.NgramFeaturizer;
import com.netease.easyml.local.mllib.scorer.impl.IScorer;

import java.util.List;

/**
 * Created by linjiuning on 2018/6/2.
 */
public class RougeNScorer implements IScorer<List<String>> {
    private NgramFeaturizer ngramFeaturizer;

    public RougeNScorer(int n) {
        ngramFeaturizer = new NgramFeaturizer(n);
    }

    public Triple<Double, Double, Double> getPRFScores(List<String> cand, List<String> ref) {
        if (cand == null || cand.isEmpty() || ref == null || ref.isEmpty())
            return Triple.triple(0.0, 0.0, 0.0);
        List<String> ngram1 = ngramFeaturizer.getFeatures(cand);
        List<String> ngram2 = ngramFeaturizer.getFeatures(ref);

        if (ngram1.isEmpty() || ngram2.isEmpty())
            return Triple.triple(0.0, 0.0, 0.0);
        Counter<String> c1 = Counter.counter(ngram1);
        Counter<String> c2 = Counter.counter(ngram2);

        int hit = 0;
        for (String key : c1.keySet())
            hit += Math.min(c1.get(key), c2.get(key));

        return Utils.prf(ngram1.size(), ngram2.size(), hit);
    }

    @Override
    public double getScores(List<String> cand, List<String> ref) {
        return getPRFScores(cand, ref).v2();
    }

    public static RougeNScorer newInstance(int n) {
        return new RougeNScorer(n);
    }
}
