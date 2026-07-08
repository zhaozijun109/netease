package com.netease.easyml.local.mllib.miner;

/**
 * Created by eddielin on 2018/8/10.
 */
public interface Significant {
    double eps = 1e-8;
    double[][] compute(double[][] distribution);
}
