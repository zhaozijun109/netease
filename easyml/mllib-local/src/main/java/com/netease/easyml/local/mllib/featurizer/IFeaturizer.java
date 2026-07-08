package com.netease.easyml.local.mllib.featurizer;

/**
 * Created by eddielin on 2018/6/2.
 */
public interface IFeaturizer<I, O> {
    O getFeatures(I text);
    int numFeatures();
}
