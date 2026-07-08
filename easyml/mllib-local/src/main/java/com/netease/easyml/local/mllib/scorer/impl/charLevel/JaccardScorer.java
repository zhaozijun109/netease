package com.netease.easyml.local.mllib.scorer.impl.charLevel;

import com.netease.easyml.local.mllib.scorer.impl.IScorer;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by linjiuning on 2018/6/2.
 */
public class JaccardScorer implements IScorer<String> {
    private static JaccardScorer SCORER;

    @Override
    public double getScores(String v1, String v2) {
        Set<Character> set1 = new HashSet<>();

        for (char c : v1.toCharArray())
            set1.add(c);

        Set<Character> set2 = new HashSet<>();
        for (char c : v2.toCharArray())
            set2.add(c);

        int size1 = set1.size();
        int size2 = set2.size();
        set1.addAll(set2);
        int totalSize = set1.size();
        return (size1 + size2 - totalSize) * 1.0 / totalSize;
    }

    public static JaccardScorer getInstance() {
        if (SCORER == null) {
            synchronized (JaccardScorer.class) {
                SCORER = new JaccardScorer();
            }
        }
        return SCORER;
    }
}
