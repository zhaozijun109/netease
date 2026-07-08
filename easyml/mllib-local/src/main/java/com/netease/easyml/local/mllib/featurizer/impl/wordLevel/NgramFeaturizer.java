package com.netease.easyml.local.mllib.featurizer.impl.wordLevel;

import com.netease.easyml.local.mllib.featurizer.IFeaturizer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by linjiuning on 2018/6/2.
 */
public class NgramFeaturizer implements IFeaturizer<List<String>, List<String>> {
    private static final String SEP = "";
    private int num;

    private String sep;

    public NgramFeaturizer(int num, String sep) {
        this.num = num;
        this.sep = sep;
    }

    public NgramFeaturizer(int num) {
        this(num, SEP);
    }

    @Override
    public int numFeatures() {
        return 0;
    }

    @Override
    public List<String> getFeatures(List<String> tokens) {
        List<String> ngram = new ArrayList<>();
        for (int i = 0; i <= tokens.size() - num; i++) {
            String text = tokens.subList(i, i + num).stream().collect(Collectors.joining(sep));
            ngram.add(text);
        }
        return ngram;
    }

//    @Override
//    public List<String> getFeatures(String text) {
//        List<String> ngram = new ArrayList<>();
//        for (int i = 0; i <= text.length() - num; i++) {
//            String chars = text.substring(i, i + num);
//            ngram.add(chars);
//        }
//        return ngram;
//    }

    public static NgramFeaturizer newInstance(int num) {
        return new NgramFeaturizer(num);
    }
}

