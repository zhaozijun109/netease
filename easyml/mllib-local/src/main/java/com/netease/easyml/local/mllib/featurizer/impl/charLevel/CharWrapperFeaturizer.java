package com.netease.easyml.local.mllib.featurizer.impl.charLevel;

import com.netease.easyml.local.mllib.featurizer.IFeaturizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linjiuning on 2018/7/30.
 */
public class CharWrapperFeaturizer<O> implements IFeaturizer<String, O> {
    private IFeaturizer<List<String>, O> wordLevelFeaturizer;

    public CharWrapperFeaturizer(IFeaturizer<List<String>, O> wordLevelFeaturizer) {
        this.wordLevelFeaturizer = wordLevelFeaturizer;
    }

    @Override
    public O getFeatures(String text) {
        List<String> tokens = new ArrayList<>(text.length());
        for (char c : text.toCharArray())
            tokens.add(String.valueOf(c));
        return wordLevelFeaturizer.getFeatures(tokens);
    }

    @Override
    public int numFeatures() {
        return wordLevelFeaturizer.numFeatures();
    }
}
