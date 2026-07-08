package com.netease.easyml.local.mllib.scorer.impl;

import com.netease.easyml.common.util.MathUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by linjiuning on 2018/8/29.
 */
public class WeightedJaccardScorer<T> implements IScorer<Map<T, Double>> {

    @Override
    public double getScores(Map<T, Double> v1, Map<T, Double> v2) {
        Map<T, Double> union = new HashMap<>();
        Map<T, Double> small = v1;
        Map<T, Double> big = v2;

        if (v1.size() > v2.size()) {
            big = v1;
            small = v2;
        }

        for (T key : small.keySet()) {
            if (big.containsKey(key))
                union.put(key, Double.MAX_VALUE);
        }

        double weight = 0.0;
        for (Map.Entry<T, Double> entry : small.entrySet()) {
            T key = entry.getKey();
            double value = entry.getValue();
            weight += value;
            if (union.containsKey(key) && value < union.get(key)) {
                union.put(key, value);
            }
        }

        for (Map.Entry<T, Double> entry : big.entrySet()) {
            T key = entry.getKey();
            double value = entry.getValue();
            weight += value;
            if (union.containsKey(key) && value < union.get(key)) {
                union.put(key, value);
            }
        }

        double weightInter = 0.0;
        for (Map.Entry<T, Double> entry : union.entrySet())
            weightInter += entry.getValue();
        return MathUtil.safeDiv(weightInter, weight - weightInter);
    }
}
