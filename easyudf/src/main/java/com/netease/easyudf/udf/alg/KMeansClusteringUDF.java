package com.netease.easyudf.udf.alg;

import com.netease.easyml.common.util.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.javatuples.Pair;
import smile.clustering.KMeans;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by linjiuning on 2022/7/15.
 */
@Slf4j
public class KMeansClusteringUDF extends UDF {

    public static class MyKMeans extends KMeans {

        public MyKMeans(double distortion, double[][] centroids, int[] y) {
            super(distortion, centroids, y);
        }


        public Pair<Integer, Double> predictWithDist(double[] x) {
            double nearest = Double.MAX_VALUE;
            int label = 0;

            for (int i = 0; i < k; i++) {
                double dist = distance(centroids[i], x);
                if (dist < nearest) {
                    nearest = dist;
                    label = i;
                }
            }

            return new Pair<>(label, nearest);
        }

    }

    public List<List<String>> evaluate(List<String> words, List<List<Double>> vectors, int k, int maxIter, double tol) {
        if (CollectionUtil.isEmpty(vectors)) {
            return null;
        }
        if (words.size() <= k) {
            return words.stream().map(Collections::singletonList).collect(Collectors.toList());
        }
        long startTime = System.currentTimeMillis();
        double[][] data = new double[vectors.size()][vectors.get(0).size()];
        for (int i = 0; i < vectors.size(); i++) {
            List<Double> vector = vectors.get(i);
            for (int j = 0; j < vector.size(); j++) {
                data[i][j] = vector.get(j);
            }
        }
        KMeans model = KMeans.fit(data, k, maxIter, tol);
        MyKMeans myKMeans = new MyKMeans(model.distortion, model.centroids, model.y);

        Map<Integer, List<Pair<String, Double>>> clusters = new HashMap<>();
        for (int i = 0; i < data.length; i++) {
            Pair<Integer, Double> ret = myKMeans.predictWithDist(data[i]);
            if (!clusters.containsKey(ret.getValue0())) {
                clusters.put(ret.getValue0(), new ArrayList<>());
            }
            clusters.get(ret.getValue0()).add(new Pair<>(words.get(i), ret.getValue1()));
        }
        List<List<String>> ret = new ArrayList<>();
        for (Map.Entry<Integer, List<Pair<String, Double>>> entry : clusters.entrySet()) {
            List<Pair<String, Double>> pairs = entry.getValue();
            if (pairs.size() > 1) {
                pairs.sort(Comparator.comparing(Pair::getValue1));
            }
            List<String> cWords = pairs.stream().map(Pair::getValue0).collect(Collectors.toList());
            ret.add(cWords);
        }
        long endTime = System.currentTimeMillis();
        log.info(String.format("Length=%d, cost=%dms", words.size(), endTime - startTime));
        return ret;
    }

    public List<List<String>> evaluate(List<String> words, List<List<Double>> vectors, int k, int maxIter) {
        return evaluate(words, vectors, k, maxIter, 1e-4);
    }

    public List<List<String>> evaluate(List<String> words, List<List<Double>> vectors, int k) {
        return evaluate(words, vectors, k, 100);
    }
}
