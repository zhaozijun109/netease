package com.netease.easyml.local.mllib;

import com.netease.easyml.common.collection.Tuple;
import com.netease.easyml.common.util.SortUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.NotImplementedException;
import org.ujmp.core.DenseMatrix;
import org.ujmp.core.Matrix;
import org.ujmp.core.SparseMatrix;
import org.ujmp.core.calculation.Calculation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by eddielin on 2018/6/3.
 */
public class PageRank implements Serializable {
    public static final double DEFAULT_DAMPING = 0.85;
    public static final double DEFAULT_EPS = 0.00001;
    public static final int MAX_ITERATION = 100;

    public static <T> Matrix buildAdjacencyMatrix(Graph<T> g) {
        List<T> nodes = g.getNodes();
        int dim = nodes.size();
        Matrix mat = SparseMatrix.Factory.zeros(dim, dim);
        boolean allZero = true;
        for (int i = 0; i < dim; i++) {
            T cur = nodes.get(i);
            List<T> neighbors = g.getNeighbors(cur);
            if (neighbors.size() == 0)
                continue;
            double neighborsSum = neighbors.stream()
                    .map(item -> g.getWeight(Tuple.tuple(cur, item)))
                    .reduce((it1, it2) -> it1 + it2).get();
            neighborsSum -= g.getWeight(Tuple.tuple(cur, cur));

            if (neighborsSum > 0.0) {
                allZero = false;
                for (int j = 0; j < dim; j++) {
                    double w = g.getWeight(Tuple.tuple(cur, nodes.get(j)));
                    double normW = w / neighborsSum;
                    if (i != j && w != 0.0)
                        mat.setAsDouble(normW, i, j);
                }
            }
        }
        if (allZero) {
            mat = DenseMatrix.Factory.fill(1.0 / (dim - 1), dim, dim);
            for (int i = 0; i < dim; i++)
                mat.setAsDouble(0, i, i);
        }
        return mat.transpose();
    }

    public static <T> Matrix buildProbabilityMatrix(Graph<T> g) {
        List<T> nodes = g.getNodes();
        int dim = nodes.size();
        return DenseMatrix.Factory.fill(1.0 / dim, dim, dim);
    }

    public static Matrix principalEigenvector(Matrix mat) {
        Matrix[] eigen = mat.eig();
        Matrix vector = eigen[0];
        Matrix value = eigen[1];
        int idx = 0;
        double thre = 0.0;
        for (int i = 0; i < value.getSize(0); i++) {
            double v = Math.abs(value.getAsDouble(i, i));
            if (v > thre) {
                idx = i;
                thre = v;
            }
        }
        return vector.selectColumns(Calculation.Ret.LINK, idx);
    }

    @Deprecated
    /* PageRank algorithm with iteration based on matrix. */
    public static <T> Map<T, Double> fitIterationV1(Graph<T> g, double damping, double eps, int iter) {
        Matrix adjacencyMatrix = buildAdjacencyMatrix(g);

        List<T> nodes = g.getNodes();
        int dim = nodes.size();
        Matrix scores = DenseMatrix.Factory.fill(1.0, dim, 1);

        for (int i = 0; i < iter; i++) {
            Matrix curScore = adjacencyMatrix.mtimes(scores).times(damping);
            curScore = curScore.plus(1 - damping);
            Matrix diff = curScore.minus(scores);
            scores = curScore;
            double maxDiff = 0.0;
            for (int j = 0; j < dim; j++) {
                double diff_ = Math.abs(diff.getAsDouble(j, 0));
                if (diff_ > maxDiff)
                    maxDiff = diff_;
            }
            if (maxDiff <= eps)
                break;
        }

        Map<T, Double> res = new HashMap<>();
        for (int i = 0; i < nodes.size(); i++)
            res.put(nodes.get(i), Math.abs(scores.getAsDouble(i, 0)));
        res = SortUtil.sortByValueDesc(res);
        return res;
    }

    /**
     * PageRank algorithm with iteration based on node not on matrix. Adding by xinfengli on 2018/06/18
     */
    public static <T> Map<T, Double> fitIterationV2(Graph<T> g, double damping, double eps, int iter) {
        List<T> nodes = g.getNodes();
        int nodeNum = nodes.size();

        // init, all node has rank 1.0
        double[] ranks = new double[nodeNum];
        for (int k = 0; k < nodeNum; ++k) ranks[k] = 1.0;

        // calculate out-weights for each node
        double[] outWeights = new double[nodeNum];
        for (int i = 0; i < nodeNum; ++i) {
            double sum = 0.0;
            T nodeI = nodes.get(i);
            for (int j = 0; j < nodeNum; ++j) {
                if (j == i) continue;
                T nodeJ = nodes.get(j);
                Tuple<T, T> edgeIJ = Tuple.tuple(nodeI, nodeJ);
                if (g.hasEdge(edgeIJ)) {
                    sum += g.getWeight(edgeIJ);
                }
            }
            outWeights[i] = sum;
        }

        // begin iteration
        for (int it = 0; it < iter; ++it) {
            double[] newRanks = new double[nodeNum];    // new ranks in this iteration
            for (int i = 0; i < nodeNum; ++i) {
                T nodeI = nodes.get(i);
                newRanks[i] = 1 - damping;
                for (int j = 0; j < nodeNum; ++j) {
                    if (j == i || outWeights[j] == 0) continue;
                    T nodeJ = nodes.get(j);
                    Tuple<T, T> edgeJI = Tuple.tuple(nodeJ, nodeI);
                    if (g.hasEdge(edgeJI)) {
                        newRanks[i] += damping * (g.getWeight(edgeJI) / outWeights[j]) * ranks[j];
                    }
                }
            }

            // calculate current iteration error with previous iteration
            double maxError = 0.0;
            for (int k = 0; k < nodeNum; ++k) {
                double error = Math.abs(ranks[k] - newRanks[k]);
                if (error > maxError) maxError = error;
            }

            // update ranks
            ranks = newRanks;

            // if error is enough small, break the iteration in advance
            if (maxError <= eps) break;
        }

        // format result
        Map<T, Double> result = new HashMap<>();
        for (int i = 0; i < nodeNum; ++i) {
            T nodeI = nodes.get(i);
            result.put(nodeI, ranks[i]);
        }
        result = SortUtil.sortByValueDesc(result);
        return result;
    }

    public static Map<Integer, Double> fitIterationV2(double[][] similarity, double damping, double eps, int iter) {
        int nodeNum = similarity.length;

        // init, all node has rank 1.0
        double[] ranks = new double[nodeNum];
        for (int k = 0; k < nodeNum; ++k) ranks[k] = 1.0;

        // calculate out-weights for each node
        double[] outWeights = new double[nodeNum];
        for (int i = 0; i < nodeNum; ++i) {
            double sum = 0.0;
            for (int j = 0; j < nodeNum; ++j) {
                if (j == i) continue;
                sum += similarity[i][j];
            }
            outWeights[i] = sum;
        }

        // TODO normalize weight
        // begin iteration
        for (int it = 0; it < iter; ++it) {
            double[] newRanks = new double[nodeNum];    // new ranks in this iteration
            for (int i = 0; i < nodeNum; ++i) {
                newRanks[i] = 1 - damping;
                for (int j = 0; j < nodeNum; ++j) {
                    if (j == i || outWeights[j] == 0) continue;
                    newRanks[i] += damping * (similarity[j][i] / outWeights[j]) * ranks[j];
                }
            }

            // calculate current iteration error with previous iteration
            double maxError = 0.0;
            for (int k = 0; k < nodeNum; ++k) {
                double error = Math.abs(ranks[k] - newRanks[k]);
                if (error > maxError) maxError = error;
            }

            // update ranks
            ranks = newRanks;

            // if error is enough small, break the iteration in advance
            if (maxError <= eps) break;
        }

        // format result
        Map<Integer, Double> result = new HashMap<>();
        for (int i = 0; i < nodeNum; ++i) {
            result.put(i, ranks[i]);
        }
        result = SortUtil.sortByValueDesc(result);
        return result;
    }

    public static <T> Map<T, Double> fitIterationV3(Graph<T> g, Map<T, Double> weight, double damping, double eps, int iter) {
        List<T> nodes = g.getNodes();
        int nodeNum = nodes.size();

        // init, all node has rank 1.0
        double[] ranks = new double[nodeNum];
        for (int k = 0; k < nodeNum; ++k) ranks[k] = 1.0;

        // calculate out-weights for each node
        double[] outWeights = new double[nodeNum];
        for (int i = 0; i < nodeNum; ++i) {
            double sum = 0.0;
            T nodeI = nodes.get(i);
            for (int j = 0; j < nodeNum; ++j) {
                if (j == i) continue;
                T nodeJ = nodes.get(j);
                Tuple<T, T> edgeIJ = Tuple.tuple(nodeI, nodeJ);
                if (g.hasEdge(edgeIJ)) {
                    sum += g.getWeight(edgeIJ);
                }
            }
            outWeights[i] = sum;
        }

        // begin iteration
        for (int it = 0; it < iter; ++it) {
            double[] newRanks = new double[nodeNum];    // new ranks in this iteration
            for (int i = 0; i < nodeNum; ++i) {
                T nodeI = nodes.get(i);
                newRanks[i] = (1 - damping) * weight.get(nodeI);
                for (int j = 0; j < nodeNum; ++j) {
                    if (j == i || outWeights[j] == 0) continue;
                    T nodeJ = nodes.get(j);
                    Tuple<T, T> edgeJI = Tuple.tuple(nodeJ, nodeI);
                    if (g.hasEdge(edgeJI)) {
                        newRanks[i] += damping * (g.getWeight(edgeJI) / outWeights[j]) * ranks[j];
                    }
                }
            }

            // calculate current iteration error with previous iteration
            double maxError = 0.0;
            for (int k = 0; k < nodeNum; ++k) {
                double error = Math.abs(ranks[k] - newRanks[k]);
                if (error > maxError) maxError = error;
            }

            // update ranks
            ranks = newRanks;

            // if error is enough small, break the iteration in advance
            if (maxError <= eps) break;
        }

        // format result
        Map<T, Double> result = new HashMap<>();
        for (int i = 0; i < nodeNum; ++i) {
            T nodeI = nodes.get(i);
            result.put(nodeI, ranks[i]);
        }
        result = SortUtil.sortByValueDesc(result);
        return result;
    }

    public static Map<Integer, Double> fitIterationV3(double[][] similarity, double[] weight, double damping, double eps, int iter) {
        int nodeNum = similarity.length;

        // init, all node has rank 1.0
        double[] ranks = new double[nodeNum];
        for (int k = 0; k < nodeNum; ++k) ranks[k] = 1.0;

        // calculate out-weights for each node
        double[] outWeights = new double[nodeNum];
        for (int i = 0; i < nodeNum; ++i) {
            double sum = 0.0;
            for (int j = 0; j < nodeNum; ++j) {
                if (j == i) continue;
                sum += similarity[i][j];
            }
            outWeights[i] = sum;
        }

        // begin iteration
        for (int it = 0; it < iter; ++it) {
            double[] newRanks = new double[nodeNum];    // new ranks in this iteration
            for (int i = 0; i < nodeNum; ++i) {
                newRanks[i] = (1 - damping) * weight[i];
                for (int j = 0; j < nodeNum; ++j) {
                    if (j == i || outWeights[j] == 0) continue;
                    newRanks[i] += damping * (similarity[j][i] / outWeights[j]) * ranks[j];
                }
            }

            // calculate current iteration error with previous iteration
            double maxError = 0.0;
            for (int k = 0; k < nodeNum; ++k) {
                double error = Math.abs(ranks[k] - newRanks[k]);
                if (error > maxError) maxError = error;
            }

            // update ranks
            ranks = newRanks;

            // if error is enough small, break the iteration in advance
            if (maxError <= eps) break;
        }

        // format result
        Map<Integer, Double> result = new HashMap<>();
        for (int i = 0; i < nodeNum; ++i) {
            result.put(i, ranks[i]);
        }
        result = SortUtil.sortByValueDesc(result);
        return result;
    }

    public static <T> Map<T, Double> fitIteration(Graph<T> g) {
        return fitIterationV2(g, DEFAULT_DAMPING, DEFAULT_EPS, MAX_ITERATION);
    }

    public static <T> Map<T, Double> fitEigen(Graph<T> g, double damping) {
        Matrix adjacencyMatrix = buildAdjacencyMatrix(g);
        Matrix probabilityMatrix = buildProbabilityMatrix(g);

        Matrix pagerankMatrix = adjacencyMatrix.times(damping).plus(probabilityMatrix.times(1 - damping));

        Matrix vec = principalEigenvector(pagerankMatrix);
        List<T> nodes = g.getNodes();

        Map<T, Double> res = new HashMap<>();
        for (int i = 0; i < nodes.size(); i++)
            res.put(nodes.get(i), Math.abs(vec.getAsDouble(i, 0)));
        res = SortUtil.sortByValueDesc(res);
        return res;
    }

    public static <T> Map<T, Double> fitEigen(Graph<T> g) {
        return fitEigen(g, DEFAULT_DAMPING);
    }

    public static <T> Map<T, Double> fit(Params params, Graph<T> g) {
        switch (params.getAlgo()) {
            case EIGEN:
                return fitEigen(g, params.getDamping());
            case ITER:
                return fitIterationV1(g, params.getDamping(), params.getEps(), params.getStep());
            case ITER_NOT_MATRIX:
                return fitIterationV2(g, params.getDamping(), params.getEps(), params.getStep());
            default:
                throw new NotImplementedException("Only Algo.ITER_NOT_MATRIX, Algo.EIGEN and Algo.ITER is supported yet.");
        }
    }

    public static Map<Integer, Double> fit(Params params, double[][] similarity) {
        if (params.getAlgo() == Algo.ITER_NOT_MATRIX) {
            return fitIterationV2(similarity, params.getDamping(), params.getEps(), params.getStep());
        }
        throw new NotImplementedException("Only Algo.ITER_NOT_MATRIX is supported yet.");
    }

    public static <T> Map<T, Double> fit(Params params, Graph<T> g, Map<T, Double> weight) {
        return fitIterationV3(g, weight, params.getDamping(), params.getEps(), params.getStep());
    }

    public static Map<Integer, Double> fit(Params params, double[][] similarity, double[] weight) {
        return fitIterationV3(similarity, weight, params.getDamping(), params.getEps(), params.getStep());
    }

    public enum Algo {
        EIGEN,
        ITER,
        ITER_NOT_MATRIX,
    }

    @Accessors(chain = true)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Params {
        private double damping = DEFAULT_DAMPING;
        private double eps = DEFAULT_EPS;
        private int step = MAX_ITERATION;
        private Algo algo = Algo.ITER_NOT_MATRIX;
    }
}
