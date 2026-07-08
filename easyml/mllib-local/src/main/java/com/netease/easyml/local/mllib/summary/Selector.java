package com.netease.easyml.local.mllib.summary;

import java.util.List;
import java.util.Map;

/**
 * Created by eddielin on 2019/2/18.
 */
public interface Selector {
    List<Integer> select(List<List<String>> tokens, double[][] similarity, Map<Integer, Double> ranks);
}
