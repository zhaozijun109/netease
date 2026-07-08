package com.netease.easyml.local.mllib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.netease.easyml.common.util.SegmentUtil.dictSegment;

/**
 * Created by linjiuning on 2019/7/22.
 * unknown 切词取 平均/最大/最小/默认值
 */
public class WordWeight {
    private Map<String, Double> weights;
    private Map<String, Double> cache;
    private boolean seg;
    private ReduceStrategy strategy;
    private double defWeight;

    public boolean isSeg() {
        return seg;
    }

    public WordWeight(Map<String, Double> weights, boolean seg, boolean cache, ReduceStrategy strategy, double defWeight) {
        this.weights = weights;
        this.seg = seg;
        this.strategy = strategy;
        this.defWeight = defWeight;

        if (seg && cache)
            this.cache = new HashMap<>();
    }

    public WordWeight(Map<String, Double> weights) {
        this(weights, true, true, ReduceStrategy.MEAN, 0);
    }

    public WordWeight(Map<String, Double> weights, double defWeight) {
        this(weights, false, false, null, defWeight);
    }

    public boolean containsKey(String word) {
        return weights.containsKey(word) || (cache != null && cache.containsKey(word));
    }

    private double getOrCache(String word) {
        return weights.containsKey(word) ? weights.get(word) : cache.get(word);
    }

    public double get(String word) {
        if (containsKey(word)) {
            return getOrCache(word);
        } else if (seg) {
            List<String> tokens = dictSegment(weights.keySet(), word);
            double val = defWeight;
            List<Double> vals = new ArrayList<>();
            for (String token : tokens) {
                if (weights.containsKey(token)) {
                    vals.add(weights.get(token));
                }
            }
            if (!vals.isEmpty()) {
                switch (strategy) {
                    case MAX:
                        val = vals.stream().max(Double::compareTo).get();
                        break;
                    case MIN:
                        val = vals.stream().min(Double::compareTo).get();
                        break;
                    case MEAN:
                        val = vals.stream().reduce((it1, it2) -> it1 + it2).get() / vals.size();
                        break;
                    case MEDIAN:
                        vals.sort(Double::compareTo);
                        int mid = vals.size() / 2;
                        if (vals.size() % 2 == 0) {
                            val = 0.5 * (vals.get(mid) + vals.get(mid - 1));
                        } else
                            val = vals.get(mid);
                }
            }
            if (cache != null)
                cache.putIfAbsent(word, val);
            return val;
        } else {
            return defWeight;
        }
    }

    public static class Builder {
        private Map<String, Double> weights;
        private boolean seg = true;
        private boolean cache = true;
        private ReduceStrategy strategy = ReduceStrategy.MEAN;
        private double defWeight;

        public Map<String, Double> getWeights() {
            return weights;
        }

        public Builder setWeights(Map<String, Double> weights) {
            this.weights = weights;
            return this;
        }

        public boolean isSeg() {
            return seg;
        }

        public Builder setSeg(boolean seg) {
            this.seg = seg;
            return this;
        }

        public boolean isCache() {
            return cache;
        }

        public Builder setCache(boolean cache) {
            this.cache = cache;
            return this;
        }

        public ReduceStrategy getStrategy() {
            return strategy;
        }

        public Builder setStrategy(ReduceStrategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public double getDefWeight() {
            return defWeight;
        }

        public Builder setDefWeight(double defWeight) {
            this.defWeight = defWeight;
            return this;
        }

        public WordWeight build() {
            return new WordWeight(weights, seg, cache, strategy, defWeight);
        }
    }
}
