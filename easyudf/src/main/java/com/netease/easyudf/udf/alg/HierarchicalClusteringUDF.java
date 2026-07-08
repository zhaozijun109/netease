package com.netease.easyudf.udf.alg;

import com.netease.easyml.common.util.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.hclust.Hclust;
import org.hclust.MethodType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by linjiuning on 2022/3/18.
 */
@Slf4j
public class HierarchicalClusteringUDF extends UDF {
    private static final String SEP = "_!_";

    public double distance(List<Double> v1, List<Double> v2) {
        double s = 0;
        for (int i = 0; i < v1.size(); i++) {
            s += v1.get(i) * v2.get(i);
        }
        return 1 - s;
    }

    public Hclust fit(List<List<Double>> vectors, int method) {
        int npoints = vectors.size();
        double[] distmat = new double[(npoints * (npoints - 1)) / 2];
        int k = 0;
        for (int i = 0; i < npoints; i++) {
            for (int j = i + 1; j < npoints; j++) {
                distmat[k] = distance(vectors.get(i), vectors.get(j));
                k++;
            }
        }
        Hclust hclust = new Hclust(npoints, MethodType.valueOf(method));
        hclust.fit(distmat);
        return hclust;
    }

    public List<String> evaluate(List<String> ids, List<List<Double>> vectors, int method, double cdist) {
        if (CollectionUtil.isEmpty(ids)) {
            return null;
        }
        if (ids.size() < 2) {
            return Collections.singletonList(ids.get(0) + SEP + 0);
        }
        long startTime = System.currentTimeMillis();
        Hclust hclust = fit(vectors, method);
        int[] labels = cdist >= 2 ? hclust.cutreeK((int) cdist) : hclust.cutreeCdist(cdist);
        List<String> res = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            res.add(ids.get(i) + SEP + labels[i]);
        }
        long endTime = System.currentTimeMillis();
        log.info(String.format("Length=%d, cost=%dms", ids.size(), endTime - startTime));
        return res;
    }

    public List<String> evaluate(List<String> ids, List<List<Double>> vectors, double cdist) {
        return evaluate(ids, vectors, 0, cdist);
    }
}
