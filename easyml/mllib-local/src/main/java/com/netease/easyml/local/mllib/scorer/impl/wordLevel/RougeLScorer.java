package com.netease.easyml.local.mllib.scorer.impl.wordLevel;

import com.netease.easyml.common.collection.Triple;
import com.netease.easyml.local.mllib.Utils;
import com.netease.easyml.local.mllib.scorer.impl.IScorer;

import java.util.List;

/**
 * Created by linjiuning on 2018/6/2.
 */
public class RougeLScorer implements IScorer<List<String>> {
    private double beta;

    public RougeLScorer() {
        this(1.2);
    }

    public RougeLScorer(double beta) {
        this.beta = beta;
    }

    private Triple<Double, Double, Double> prf(int len1, int len2, int same) {
        double p = same * 1.0 / len1;
        double r = same * 1.0 / len2;
        double deno = beta * beta * p + r;
        double f1 = deno > 0 ? ((1 + beta * beta) * p * r) / deno : 0;
        return Triple.triple(p, r, f1);
    }

    public Triple<Double, Double, Double> getPRFScores(List<String> cand, List<String> ref) {
        if (cand == null || cand.isEmpty() || ref == null || ref.isEmpty())
            return Triple.triple(0.0, 0.0, 0.0);
        List<String> lcs = Utils.LCSCalculate(cand, ref);
        int dist = lcs.size();
        return prf(cand.size(), ref.size(), dist);
    }

    /**
     * @return f1
     */
    @Override
    public double getScores(List<String> cand, List<String> ref) {
        return getPRFScores(cand, ref).v3();
    }

    public static RougeLScorer newInstance() {
        return new RougeLScorer();
    }
}
