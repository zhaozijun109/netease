package com.netease.easyml.local.mllib.featurizer.impl.wordLevel;

import com.netease.easyml.common.util.CollectionUtil;
import com.netease.easyml.common.util.MatrixUtil;
import com.netease.easyml.local.mllib.WordVec;
import com.netease.easyml.local.mllib.WordWeight;
import com.netease.easyml.local.mllib.featurizer.IFeaturizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by linjiuning on 2018/6/2.
 */
public class SentVecFeaturizer implements IFeaturizer<List<String>, double[]> {
    private WordVec word2vec;
    private WordWeight weight;
    private Set<String> stopWords;
    private boolean attention;

    public SentVecFeaturizer(WordVec word2vec, WordWeight weight, Set<String> stopWords, boolean attention) {
        this.word2vec = word2vec;
        this.weight = weight;
        this.stopWords = stopWords;
        this.attention = attention;
    }

    @Override
    public double[] getFeatures(List<String> tokens) {
        if (!CollectionUtil.isEmpty(stopWords)) {
            tokens = tokens.stream()
                    .filter(it -> !stopWords.contains(it))
                    .collect(Collectors.toList());
        }
        List<double[]> allFeat = new ArrayList<>();
        for (String token : tokens) {
            double[] tvec = word2vec.get(token);
            if (weight != null)
                tvec = MatrixUtil.mul(tvec, weight.get(token));
            allFeat.add(tvec);
        }

        if (allFeat.isEmpty())
            return new double[numFeatures()];

        double[] sum = MatrixUtil.plus(allFeat);
        double[] mean = MatrixUtil.div(sum, allFeat.size());

        if (!attention)
            return mean;

        List<double[]> attenFeat = allFeat.stream()
                .map(feat -> {
                    double score = MatrixUtil.cosine(mean, feat);
                    return MatrixUtil.mul(feat, score);
                }).collect(Collectors.toList());

        return MatrixUtil.plus(attenFeat);
    }

    @Override
    public int numFeatures() {
        return word2vec.dim();
    }

    public static class Builder {
        private WordVec wordVec;
        private WordWeight weight;
        private Set<String> stopWords;
        private boolean attention = false;

        public WordVec getWordVec() {
            return wordVec;
        }

        public Builder setWordVec(WordVec wordVec) {
            this.wordVec = wordVec;
            return this;
        }

        public WordWeight getWeight() {
            return weight;
        }

        public Builder setWeight(WordWeight weight) {
            this.weight = weight;
            return this;
        }

        public Set<String> getStopWords() {
            return stopWords;
        }

        public Builder setStopWords(Set<String> stopWords) {
            this.stopWords = stopWords;
            return this;
        }

        public boolean isAttention() {
            return attention;
        }

        public Builder setAttention(boolean attention) {
            this.attention = attention;
            return this;
        }

        public SentVecFeaturizer build() {
            return new SentVecFeaturizer(wordVec, weight, stopWords, attention);
        }
    }
}
