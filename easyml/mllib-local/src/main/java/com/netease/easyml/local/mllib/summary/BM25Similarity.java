package com.netease.easyml.local.mllib.summary;

import com.netease.easyml.local.mllib.scorer.impl.wordLevel.BM25Scorer;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by eddielin on 2019/2/19.
 */
@AllArgsConstructor
public class BM25Similarity implements Similarity {
    private static final double DEFAULT_IDF = 5.0;
    private Map<String, Double> idf;
    private double defaultIdf;
    private double diagValue;
    private double threshold;

    public BM25Similarity(Map<String, Double> idf) {
        this.idf = idf;
        this.defaultIdf = DEFAULT_IDF;
    }

    @Override
    public double[][] score(List<List<String>> tokens) {
        Optional<Integer> reduce = tokens.stream().map(List::size).reduce((it1, it2) -> it1 + it2);
        int snum = tokens.size();
        double[][] similarity = new double[snum][snum];
        if (!reduce.isPresent())
            return similarity;
        int totalLen = reduce.get();
        double avgLen = totalLen * 1.0 / tokens.size();

        BM25Scorer scorer = new BM25Scorer(idf, defaultIdf, avgLen);

        for (int i = 0; i < tokens.size(); ++i) {
            similarity[i][i] = diagValue;
            for (int j = i + 1; j < tokens.size(); ++j) {
                List<String> wordList1 = tokens.get(i);
                List<String> wordList2 = tokens.get(j);

                // skip empty lines (no valid words)
                if (wordList1.isEmpty() || wordList2.isEmpty()) continue;

                double sc = scorer.getScores(wordList1, wordList2);
                if (sc > threshold)
                    similarity[i][j] = similarity[j][i] = sc;
            }
        }
        return similarity;
    }
}
