package com.netease.easyml.local.mllib.featurizer.impl.charLevel;

import com.netease.easyml.local.mllib.featurizer.IFeaturizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eddielin on 2018/6/2.
 */
public class NgramFeaturizer implements IFeaturizer<String, List<String>> {
    private int num;

    public NgramFeaturizer(int num) {
        this.num = num;
    }

    @Override
    public int numFeatures() {
        return 0;
    }

//    @Override
//    public List<String> getFeatures(List<String> tokens) {
//        List<String> ngram = new ArrayList<>();
//        for (int i = 0; i <= tokens.size() - num; i++) {
//            String text = tokens.subList(i, i + num).stream().collect(Collectors.joining(""));
//            ngram.add(text);
//        }
//        return ngram;
//    }

    @Override
    public List<String> getFeatures(String text) {
        List<String> ngram = new ArrayList<>();
        for (int i = 0; i <= text.length() - num; i++) {
            String chars = text.substring(i, i + num);
            ngram.add(chars);
        }
        return ngram;
    }

    public static NgramFeaturizer newInstance(int num) {
        return new NgramFeaturizer(num);
    }
}

