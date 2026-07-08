package com.netease.easyml.local.mllib.scorer.impl.wordLevel;

import com.netease.easyml.local.mllib.scorer.impl.IScorer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by linjiuning on 2018/6/2.
 */
public class JaccardScorer<V> implements IScorer<Collection<V>> {

    @Override
    public double getScores(Collection<V> v1, Collection<V> v2) {
        Set<V> set1 = new HashSet<>(v1);
        Set<V> set2 = new HashSet<>(v2);
        int size1 = set1.size();
        int size2 = set2.size();
        set1.addAll(set2);
        int totalSize = set1.size();
        return (size1 + size2 - totalSize) * 1.0 / totalSize;
    }

    public static <V> JaccardScorer<V> newInstance() {
        return new JaccardScorer<>();
    }
}
