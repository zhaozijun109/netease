package com.netease.easyml.local.mllib.summary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eddielin on 2019/2/18.
 */
@AllArgsConstructor
public class SubModular implements Summary {
    private Params params;
    private Similarity similarity;
    private int maxLen;
    private boolean charLevel;

    public SubModular(Params params, Similarity similarity, int maxLen) {
        this(params, similarity, maxLen, true);
    }

    @Override
    public List<Integer> transform(List<List<String>> tokens) {
        double[][] sim = similarity.score(tokens);
        return greedy(tokens, sim);
    }

    // submodular function 1
    private double submod1(List<Integer> summaryId, double[][] sim, double[] sumSim, double alpha, int id) {
        double score = 0;
        int snum = sim.length;
        for (int i = 0; i < snum; i++) {
            if (i == id) continue;
            double sum = 0;
            for (int j : summaryId)
                if (i != j)
                    sum += sim[i][j];
            if (id != -1)
                sum += sim[i][id];

            if (sum > sumSim[i] * alpha)
                sum = sumSim[i] * alpha;
            score += sum;
        }
        return score;
    }

    // submodular function 2
    private double submod2(List<Integer> summaryId, double[][] sim, int id, int op) {
        double score = 0;
        for (int i : summaryId) {
            if (op == 1) {
                score += sim[id][i];
            } else {
                if (sim[id][i] > score)
                    score = sim[id][i];
            }
        }
        return -score;
    }

    public double[] calcSumSim(double[][] sim) {
        int snum = sim.length;
        double[] sumSim = new double[snum];
        for (int i = 0; i < snum; i++) {
            sumSim[i] = 0;
            for (int j = 0; j < snum; j++)
                if (i != j)
                    sumSim[i] += sim[i][j];
        }
        return sumSim;
    }

    /* pick sentence using greedy algorithm */
    public List<Integer> greedy(List<List<String>> tokens, double[][] sim) {
        int snum = sim.length;
        boolean[] chosen = new boolean[snum];
        int len = 0;
        double[] sumSim = calcSumSim(sim);
        List<Integer> summaryId = new ArrayList<>();
        List<Integer> sentLen = Utils.getSentenceLength(tokens, charLevel);
        double lambda = params.getLambda();
        double beta = params.getBeta();
        double alpha = params.getAlpha();
        int op = params.getOp();
        if (op == 1)
            alpha = 1.0;
        else if (alpha <= 0)
            alpha = 0.1 * snum;
        while (true) {
            double maxInc = -10, initScore = submod1(summaryId, sim, sumSim, alpha, -1);
            int maxId = -1;
            for (int i = 0; i < snum; i++) {
                if (!chosen[i] && len + sentLen.get(i) < maxLen) {
                    double inc = (lambda * submod1(summaryId, sim, sumSim, alpha, i) + (1 - lambda) * submod2(summaryId, sim, i, op) - initScore * lambda) / Math.pow(sentLen.get(i), beta);
                    if (inc > maxInc) {
                        maxInc = inc;
                        maxId = i;
                    }
                }
            }

            if (maxId == -1) break;
            chosen[maxId] = true;
            len += sentLen.get(maxId);
            summaryId.add(maxId);
            if (len >= maxLen - 20)
                break;
        }
        return summaryId;
    }

    @Accessors(chain = true)
    @Data
    public static class Params {
        private int op;
        private double alpha, beta = 0.1, lambda = 0.5;
    }
}
