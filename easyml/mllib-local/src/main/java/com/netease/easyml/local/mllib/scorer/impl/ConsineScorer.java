package com.netease.easyml.local.mllib.scorer.impl;

import com.netease.easyml.common.util.MatrixUtil;

/**
 * Created by linjiuning on 2018/6/2.
 */
public class ConsineScorer implements IScorer<double[]> {
    private static ConsineScorer SCORER;

    @Override
    public double getScores(double[] v1, double[] v2) {
        assert v1.length == v2.length;
        return MatrixUtil.cosine(v1, v2);
    }

    public static ConsineScorer getInstance() {
        if (SCORER == null) {
            synchronized (ConsineScorer.class) {
                SCORER = new ConsineScorer();
            }
        }
        return SCORER;
    }
}
