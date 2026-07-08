package com.netease.easyml.local.mllib.keyword;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.netease.easyml.common.util.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

import static com.netease.easyml.local.mllib.keyword.Keywords.genCandidateTerms;
import static com.netease.easyml.local.mllib.keyword.Keywords.removeStopTerms;

/**
 * Created by xinfengli on 2018/06/13.
 * Modified by eddielin on 2018/8/2.
 */
@Slf4j
public class EmbedKeyword implements IKeyword {
    private static final float DEFAULT_EMBED_FACTOR = 0.5f;
    private static final float DEFAULT_TFIDF_FACTOR = 0.13f;
    private Set<String> stopWord;
    private Map<String, Float> idfWeight;
    private Map<String, float[]> wordvec;
    private float defaultIdf;

    private float embedFactor;
    private float tfIdfFactor;

    public EmbedKeyword(Set<String> stopWord, Map<String, Float> idfWeight, Map<String, float[]> wordvec,
                        float defaultIdf, float embedFactor, float tfIdfFactor) {
        this.stopWord = stopWord;
        this.idfWeight = idfWeight;
        this.wordvec = wordvec;

        this.defaultIdf = defaultIdf;

        this.embedFactor = embedFactor;
        this.tfIdfFactor = tfIdfFactor;
    }

    public EmbedKeyword(Set<String> stopWord, Map<String, Float> idfWeight, Map<String, float[]> wordvec) {
        this(stopWord, idfWeight, wordvec, 5.0f, DEFAULT_EMBED_FACTOR, DEFAULT_TFIDF_FACTOR);
    }

    @Override
    public Map<String, Double> transform(List<Term> text) {
        List<Term> cleanTerms = removeStopTerms(stopWord, text);
        List<Term> candidateTerms = genCandidateTerms(cleanTerms);

        Map<String, Double> result = new HashMap<>();

        // word tf
        Map<String, Integer> tf = new HashMap<>();
        for (Term t : candidateTerms)
            tf.put(t.word, tf.getOrDefault(t.word, 0) + 1);


        // word idf
        Map<String, Float> wordIDF = new HashMap<>();
        for (Term t : cleanTerms) {
            if (!wordIDF.containsKey(t.word)) {
                wordIDF.put(t.word, idfWeight.getOrDefault(t.word, defaultIdf));
            }
        }

        // sentence vector
        float[] s2v = new float[wordvec.values().iterator().next().length];

        for (Term term : cleanTerms) {
            String word = term.word;
            if (wordvec.containsKey(word)) {
                float[] vec = wordvec.get(word);
                for (int k = 0; k < vec.length; ++k) {
                    s2v[k] += wordIDF.get(word) * vec[k];
                }
            }
        }

        // for each word, calculate its word embedding cosine distance to the sentence
        for (Term term : candidateTerms) {
            String word = term.word;

            // cosine similarity
            double cosDist = 0.1;
            if (wordvec.containsKey(word)) {
                float[] w2v = wordvec.get(word);
                cosDist = MatrixUtil.cosine(w2v, s2v);
            }

            // tf idf score
            double tfScore = tf.containsKey(word) ? tfIdfFactor * Math.log(1 + tf.get(word)) * wordIDF.get(word) : 0.0f;

            // word biz score
            //double bizScore = 0.002;
            //if (wordBizScore.containsKey(word)) {
            //    bizScore = wordBizScore.get(word);
            //}

            // ensemble
            double score = embedFactor * cosDist + tfScore;

            result.put(word, score);
        }

        // sort result
        return SortUtil.sortByValueDesc(result);
    }

    @Override
    public Map<String, Double> transform(String text) {
        String cleanText = Keywords.cleanStr(text);
        List<Term> termList = HanLP.segment(cleanText);
        return transform(termList);
    }

    @Setter
    @Accessors(chain = true)
    public static class Builder {
        private Set<String> stopWords;
        private Map<String, Float> idfWeight;
        private Map<String, float[]> wordvec;
        private float defaultIdf = 5.0f;

        private String kvDelimiter = " ";
        private String vDelimiter = " ";

        private float embedFactor = DEFAULT_EMBED_FACTOR;
        private float tfIdfFactor = DEFAULT_TFIDF_FACTOR;

        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private String stopWordsPath;

        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private String idfWeightPath;

        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private String wordvecPath;

        public Builder setStopWords(String stopWords) {
            this.stopWordsPath = stopWords;
            return this;
        }

        public Builder setIdfWeight(String idfWeight) {
            this.idfWeightPath = idfWeight;
            return this;
        }

        public Builder setWordvec(String wordvec) {
            this.wordvecPath = wordvec;
            return this;
        }

        public EmbedKeyword build() {
            if (!StringUtil.isEmpty(stopWordsPath)) {
                stopWords = new HashSet<>(IOUtil.readLines(stopWordsPath));
            }
            if (!StringUtil.isEmpty(idfWeightPath)) {
                try {
                    idfWeight = ResourceUtil.loadWordWeight(idfWeightPath, kvDelimiter);
                } catch (IOException e) {
                    log.error(String.format("Failed to load idf weight from %s", idfWeightPath));
                }
            }
            if (!StringUtil.isEmpty(wordvecPath)) {
                try {
                    wordvec = ResourceUtil.loadWordVec(wordvecPath, kvDelimiter, vDelimiter);
                } catch (IOException e) {
                    log.error(String.format("Failed to load wordvec from %s", wordvec));
                }
            }
            return new EmbedKeyword(stopWords, idfWeight, wordvec, defaultIdf,
                    embedFactor, tfIdfFactor);
        }
    }
}
