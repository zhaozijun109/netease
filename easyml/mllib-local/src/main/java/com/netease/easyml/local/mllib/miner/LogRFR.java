package com.netease.easyml.local.mllib.miner;

import static com.netease.easyml.common.util.MathUtil.isZero;

/**
 * Created by eddielin on 2018/8/10.
 * Log Relative Frequency Ratio
 * lrfr = log(pi / pj)
 */
public class LogRFR implements Significant {

    @Override
    public double[][] compute(double[][] distribution) {
        int rows = distribution.length;
        if (rows == 0)
            return new double[0][];
        int cols = distribution[0].length;

        double[] colSum = new double[cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++)
                colSum[j] += distribution[i][j];
        }
        double total = 0.0;
        for (int j = 0; j < cols; j++)
            total += colSum[j];
        double[][] significant = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            double sum = 0.0;
            for (int j = 0; j < cols; j++) {
                sum += distribution[i][j];
            }

            if (isZero(sum))
                continue;
            for (int j = 0; j < cols; j++) {
                if (j > 0 && cols == 2) {
                    significant[i][j] = -significant[i][j - 1];
                    continue;
                }
                significant[i][j] = Math.log(distribution[i][j] * (total - colSum[j]) + eps) - Math.log((sum - distribution[i][j]) * colSum[j] + eps);
            }
        }
        return significant;
    }
}
