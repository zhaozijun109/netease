package com.netease.easyml.local.mllib.featurizer.impl.wordLevel;

import com.netease.easyml.common.collection.Counter;
import com.netease.easyml.common.collection.Tuple;
import com.netease.easyml.local.mllib.featurizer.IFeaturizer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by linjiuning on 2018/6/4.
 */
public class BowFeaturizer implements IFeaturizer<List<String>, List<Tuple<Integer, Integer>>> {
    private Map<String, Integer> word2idx;

    public BowFeaturizer(Map<String, Integer> word2idx) {
        this.word2idx = word2idx;
    }

    @Override
    public int numFeatures() {
        return 0;
    }

    @Override
    public List<Tuple<Integer, Integer>> getFeatures(List<String> tokens) {
        return Counter.counter(tokens).getCounter().entrySet().stream().map(it ->
                Tuple.tuple(word2idx.get(it.getKey()), it.getValue())).collect(Collectors.toList());
    }

    public static BowFeaturizer newInstance(Map<String, Integer> word2idx) {
        return new BowFeaturizer(word2idx);
    }
}