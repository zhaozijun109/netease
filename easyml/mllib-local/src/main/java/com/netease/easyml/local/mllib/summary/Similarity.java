package com.netease.easyml.local.mllib.summary;

import java.util.List;

/**
 * Created by eddielin on 2019/2/18.
 */
public interface Similarity {
    double[][] score(List<List<String>> tokens);
}
