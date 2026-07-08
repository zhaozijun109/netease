package com.netease.easyml.local.mllib;

import com.netease.easyml.common.collection.Triple;
import com.netease.easyml.common.collection.Tuple;
import com.netease.easyml.common.util.MathUtil;
import com.netease.easyml.common.util.MatrixUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.DoubleStream;

/**
 * Created by linjiuning on 2018/6/2.
 */
public class Utils {

    public static final double EPS = 1e-06;
    public static final double LOG_2 = Math.log(2);

    public static Triple<Double, Double, Double> prf(int candLen, int refLen, int same) {
        double p = same * 1.0 / candLen;
        double r = same * 1.0 / refLen;
        double deno = p + r;
        double f1 = deno > 0 ? (2 * p * r) / deno : 0;
        return Triple.triple(p, r, f1);
    }

    public static double f1(int len1, int len2, int same) {
        double p = same * 1.0 / len1;
        double r = same * 1.0 / len2;
        return p + r > 0 ? (2 * p * r) / (p + r) : 0;
    }

    public static int editDistance(List<String> s1, List<String> s2) {
        if (s1.size() == 0 || s2.size() == 0)
            return Math.max(s1.size(), s2.size());
        int edits[][] = new int[s1.size() + 1][s2.size() + 1];
        for (int i = 0; i <= s1.size(); i++)
            edits[i][0] = i;
        for (int j = 1; j <= s2.size(); j++)
            edits[0][j] = j;
        for (int i = 1; i <= s1.size(); i++) {
            for (int j = 1; j <= s2.size(); j++) {
                int u = (s1.get(i - 1).equals(s2.get(j - 1)) ? 0 : 1);
                edits[i][j] = Math.min(
                        edits[i - 1][j] + 1,
                        Math.min(
                                edits[i][j - 1] + 1,
                                edits[i - 1][j - 1] + u
                        )
                );
            }
        }
        return edits[s1.size()][s2.size()];
    }

    public static int editDistance(String s1, String s2) {
        if (s1.length() == 0 || s2.length() == 0)
            return Math.max(s1.length(), s2.length());
        int[][] edits = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++)
            edits[i][0] = i;
        for (int j = 1; j <= s2.length(); j++)
            edits[0][j] = j;
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int u = (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1);
                edits[i][j] = Math.min(
                        edits[i - 1][j] + 1,
                        Math.min(
                                edits[i][j - 1] + 1,
                                edits[i - 1][j - 1] + u
                        )
                );
            }
        }
        return edits[s1.length()][s2.length()];
    }

    /**
     * Entropy
     * H(p) = -sum_i(p_i * log(p_i)
     */
    public static double entropy(double[] P) {
        double score = 0.0;
        for (double sc : P) {
            if (MathUtil.isZero(sc))
                continue;
            score += -sc * Math.log(sc);
        }
        return score;
    }

    public static double entropy(List<Tuple<Integer, Double>> P) {
        double score = 0.0;
        for (Tuple<Integer, Double> tuple : P) {
            double sc = tuple.v2();
            if (MathUtil.isZero(sc))
                continue;
            score += -sc * Math.log(sc);
        }
        return score;
    }

    /**
     * Cross Entropy
     * H(p, q) = -sum_i(p_i * log(q_i)
     */
    public static double crossEntropy(double[] P, double[] Q, double minProb) {
        double score = 0.0;
        for (int i = 0; i < P.length; i++) {
            double sc1 = P[i];
            double sc2 = Q[i];
            sc2 = Math.max(sc2, minProb);
            score += -sc1 * Math.log(sc2);
        }
        return score;
    }

    public static double crossEntropy(double[] P, double[] Q) {
        return crossEntropy(P, Q, EPS);
    }

    public static double crossEntropy(List<Tuple<Integer, Double>> P, List<Tuple<Integer, Double>> Q, double minProb) {
        double score = 0.0;
        int i = 0, j = 0;
        while (i < P.size() && j < Q.size()) {
            Tuple<Integer, Double> value1 = P.get(i);
            Tuple<Integer, Double> value2 = Q.get(j);
            double sc1 = value1.v2();
            double sc2 = value2.v2();
            if (value1.v1().equals(value2.v1())) {
                sc2 = Math.max(sc2, minProb);
                score += -sc1 * Math.log(sc2);
                i++;
                j++;
            } else if (value1.v1() < value2.v1()) {
                score += -sc1 * Math.log(minProb);
                i++;
            } else
                j++;
        }
        return score;
    }

    public static double crossEntropy(List<Tuple<Integer, Double>> P, List<Tuple<Integer, Double>> Q) {
        return crossEntropy(P, Q, MathUtil.EPS);
    }

    /**
     * Kullback–Leibler Divergence
     * DKL(P || Q) = H(P, Q) - H(P)
     */
    public static double KLDivergence(double[] P, double[] Q, double minProb) {
        return crossEntropy(P, Q, minProb) - entropy(P);
    }

    public static double KLDivergence(double[] P, double[] Q) {
        return KLDivergence(P, Q, EPS);
    }

    public static double KLDivergence(List<Tuple<Integer, Double>> P, List<Tuple<Integer, Double>> Q, double minProb) {
        return crossEntropy(P, Q, minProb) - entropy(P);
    }

    public static double KLDivergence(List<Tuple<Integer, Double>> P, List<Tuple<Integer, Double>> Q) {
        return KLDivergence(P, Q, EPS);
    }

    /**
     * Jensen–Shannon divergence
     * JSD(P || Q) = (KL(P || M) + KL(Q || M) / 2
     * M = (P + Q) / 2
     */
    public static double JSDivergence(double[] P, double[] Q, double minProb) {
        double[] M = new double[P.length];
        for (int i = 0; i < M.length; i++)
            M[i] = 0.5 * (P[i] + Q[i]);
        return 0.5 * (KLDivergence(P, M, minProb) + KLDivergence(Q, M, minProb));
    }

    public static double JSDivergence(double[] P, double[] Q) {
        return JSDivergence(P, Q, EPS);
    }

    public static double JSDivergence(List<Tuple<Integer, Double>> P, List<Tuple<Integer, Double>> Q, double minProb) {
        List<Tuple<Integer, Double>> M = MatrixUtil.sparsePlus(P, Q);
        M = MatrixUtil.sparsePlus(M, 0.5);
        return 0.5 * (KLDivergence(P, M, minProb) + KLDivergence(Q, M, minProb));
    }

    public static double JSDivergence(List<Tuple<Integer, Double>> P, List<Tuple<Integer, Double>> Q) {
        return JSDivergence(P, Q, EPS);
    }

    /**
     * Hellinger distance
     * H(P, Q) = 1 / sqrt(2) * || sqrt(P) - sqrt(Q) ||_2
     */
    public static double hellingerDistance(double[] P, double[] Q) {
        double score = 0.0;
        for (int i = 0; i < P.length; i++) {
            double diff = Math.sqrt(P[i]) - Math.sqrt(Q[i]);
            score += diff * diff;
        }
        // 1/√2 = 0.7071067812
        return 0.7071067812 * Math.sqrt(score);
    }

    public static double hellingerDistance(List<Tuple<Integer, Double>> P, List<Tuple<Integer, Double>> Q) {
        double score = 0.0;
        int i = 0, j = 0;
        while (i < P.size() && j < Q.size()) {
            Tuple<Integer, Double> value1 = P.get(i);
            Tuple<Integer, Double> value2 = Q.get(j);
            double sc1 = value1.v2();
            double sc2 = value2.v2();
            double diff;
            if (value1.v1().equals(value2.v1())) {
                diff = Math.sqrt(sc1) - Math.sqrt(sc2);
                i++;
                j++;
            } else if (value1.v1() < value2.v1()) {
                diff = Math.sqrt(sc1);
                i++;
            } else {
                diff = Math.sqrt(sc2);
                j++;
            }
            score += diff * diff;
        }
        return MathUtil.isZero(score) ? 0.0 : 1.0 / Math.sqrt(2) * Math.sqrt(score);
    }

    public static double sigmoid(double score) {
        return 1.0 / (1 + Math.exp(-score));
    }

    /**
     * Discounted Cumulative Gain
     * DCG = rel_1 + sum_i(rel_i / log2(i))
     */
    public static double discountedCumulativeGain(double[] ranks) {
        double score = 0.0;
        if (ranks.length == 0)
            return score;
        double log2 = Math.log(2);
        for (int i = 1; i < ranks.length; i++)
            score += ranks[i] / (Math.log(i + 1) / LOG_2);
        score *= log2;
        score += ranks[0];
        return score;
    }

    /**
     * Normalize Discounted Cumulative Gain
     * NDCG = DCG / IDCG
     */
    public static double normalizeDiscountedCumulativeGain(double[] ranks) {
        if (ranks.length == 0)
            return 0.0;

        double dcg = discountedCumulativeGain(ranks);

        double[] tmpRanks = DoubleStream.of(ranks).boxed()
                .sorted(Collections.reverseOrder())
                .mapToDouble(Double::doubleValue).toArray();

        double idealDcg = discountedCumulativeGain(tmpRanks);

        return MathUtil.safeDiv(dcg, idealDcg);
    }

    public static String LCSCalculate(String s1, String s2) {
        int size1 = s1.length();
        int size2 = s2.length();
        int[][] chess = new int[size1 + 1][size2 + 1];
        for (int i = 1; i <= size1; i++) {
            for (int j = 1; j <= size2; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    chess[i][j] = chess[i - 1][j - 1] + 1;
                } else {
                    chess[i][j] = Math.max(chess[i][j - 1], chess[i - 1][j]);
                }
            }
        }
        int i = size1;
        int j = size2;
        StringBuilder sb = new StringBuilder();
        while ((i != 0) && (j != 0)) {
            if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                sb.append(s1.charAt(i - 1));
                i--;
                j--;
            } else {
                if (chess[i][j - 1] > chess[i - 1][j]) {
                    j--;
                } else {
                    i--;
                }
            }
        }
        return sb.reverse().toString();
    }

    public static List<String> LCSCalculate(List<String> s1, List<String> s2) {
        int size1 = s1.size();
        int size2 = s2.size();
        int[][] chess = new int[size1 + 1][size2 + 1];
        for (int i = 1; i <= size1; i++) {
            for (int j = 1; j <= size2; j++) {
                if (s1.get(i - 1).equals(s2.get(j - 1))) {
                    chess[i][j] = chess[i - 1][j - 1] + 1;
                } else {
                    chess[i][j] = Math.max(chess[i][j - 1], chess[i - 1][j]);
                }
            }
        }
        int i = size1;
        int j = size2;
        List<String> lcs = new ArrayList<>();
        while ((i != 0) && (j != 0)) {
            if (s1.get(i - 1).equals(s2.get(j - 1))) {
                lcs.add(0, s1.get(i - 1));
                i--;
                j--;
            } else {
                if (chess[i][j - 1] > chess[i - 1][j]) {
                    j--;
                } else {
                    i--;
                }
            }
        }
        return lcs;
    }
}
