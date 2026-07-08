package com.netease.easyml.local.mllib;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.netease.easyml.common.util.SegmentUtil.dictSegment;

/**
 * Created by linjiuning on 2019/7/17.
 * unknown 切词取平均
 */
public class WordVec {
    private Map<String, double[]> vectors;
    private boolean seg;
    private Map<String, double[]> cache;

    public boolean isSeg() {
        return seg;
    }

    public WordVec(Map<String, double[]> vectors, boolean seg, boolean cache) {
        this.vectors = vectors;
        this.seg = seg;
        if (seg && cache)
            this.cache = new HashMap<>();
    }

    public WordVec(Map<String, double[]> vectors, boolean seg) {
        this(vectors, seg, true);
    }

    public WordVec(Map<String, double[]> vectors) {
        this(vectors, true);
    }

    public int dim() {
        if (vectors.isEmpty())
            return 0;
        return vectors.values().iterator().next().length;
    }

    public boolean containsKey(String word) {
        return vectors.containsKey(word) || (cache != null && cache.containsKey(word));
    }

    private double[] getOrCache(String word) {
        return vectors.containsKey(word) ? vectors.get(word) : cache.get(word);
    }

    public double[] get(String word) {
        if (containsKey(word)) {
            return getOrCache(word);
        } else if (seg) {
            List<String> tokens = dictSegment(vectors.keySet(), word);
            double[] vec = new double[dim()];
            if (!tokens.isEmpty()) {
                for (String token : tokens) {
                    if (vectors.containsKey(token)) {
                        double[] tmp = vectors.get(token);
                        for (int i = 0; i < tmp.length; i++) {
                            vec[i] += tmp[i];
                        }
                    }
                }
                for (int i = 0; i < vec.length; i++) {
                    vec[i] /= tokens.size();
                }
            }
            if (cache != null)
                cache.putIfAbsent(word, vec);
            return vec;
        } else {
            return new double[dim()];
        }
    }
}
