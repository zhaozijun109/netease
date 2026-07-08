package com.netease.easyml.local.mllib.scorer.impl;

/**
 * Created by linjiuning on 2018/6/2.
 */
public interface IScorer<V> {
    double getScores(V v1, V v2);
}
