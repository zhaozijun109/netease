package com.netease.easyml.local.mllib.scorer.impl.charLevel;

import com.netease.easyml.local.mllib.scorer.impl.IScorer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linjiuning on 2018/7/30.
 */
public class CharWrapperScorer implements IScorer<String> {
    private IScorer<List<String>> wordLevelScorer;

    public CharWrapperScorer(IScorer<List<String>> wordLevelScorer) {
        this.wordLevelScorer = wordLevelScorer;
    }

    @Override
    public double getScores(String v1, String v2) {
        List<String> v1_ = new ArrayList<>(v1.length());
        for (char c : v1.toCharArray())
            v1_.add(String.valueOf(c));
        List<String> v2_ = new ArrayList<>(v2.length());
        for (char c : v2.toCharArray())
            v2_.add(String.valueOf(c));

        return wordLevelScorer.getScores(v1_, v2_);
    }
}
