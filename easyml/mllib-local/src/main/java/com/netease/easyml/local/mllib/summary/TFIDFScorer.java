package com.netease.easyml.local.mllib.summary;

import com.netease.easyml.common.collection.Counter;
import com.netease.easyml.local.mllib.scorer.impl.IScorer;
import lombok.AllArgsConstructor;

import java.util.*;

/**
 * Created by eddielin on 2019/2/18.
 */
@AllArgsConstructor
public class TFIDFScorer implements IScorer<List<String>> {
    private Map<String, Double> idf;

    @Override
    public double getScores(List<String> tokens1, List<String> tokens2) {
        int len1 = tokens1.size();
        int len2 = tokens2.size();

        Set<String> words = new HashSet<>();
        Map<String, Integer> vec1 = new HashMap<>();
        for (String wd : tokens1) {
            words.add(wd);
            int val = vec1.getOrDefault(wd, 0);
            val += 1;
            vec1.put(wd, val);
        }

        Map<String, Integer> vec2 = new HashMap<>();
        for (String wd : tokens2) {
            words.add(wd);
            int val = vec2.getOrDefault(wd, 0);
            val += 1;
            vec2.put(wd, val);
        }

        double l1 = 0.0;
        double l2 = 0.0;
        double cos = 0.0;
        for (String wd : words) {
            if (!idf.containsKey(wd))
                continue;
            if (vec1.containsKey(wd)) {
                l1 += Math.pow(1.0 * vec1.get(wd) / len1 * idf.get(wd), 2);
            }
            if (vec2.containsKey(wd)) {
                l2 += Math.pow(1.0 * vec2.get(wd) / len2 * idf.get(wd), 2);
            }
            if (vec1.containsKey(wd) && vec2.containsKey(wd)) {
                cos += Math.pow(idf.get(wd), 2) * vec1.get(wd) / len1 * vec2.get(wd) / len2;
            }
        }
        return cos == 0.0 ? 0.0 : cos / Math.pow(l1 * l2, 0.5);
    }

    public static TFIDFScorer getScorer(List<List<String>> texts) {
        Counter<String> counter = new Counter<>();
        texts.forEach(sent -> {
            Set<String> uniq = new HashSet<>(sent);
            uniq.forEach(counter::add);
        });
        int docLength = texts.size();
        Map<String, Double> idfCounter = new HashMap<>();
        counter.getCounter().forEach((k, v) ->
                idfCounter.put(k, Math.log(docLength + 1.0) - Math.log(v + 1.0) + 1.0));
        return new TFIDFScorer(idfCounter);
    }
}
