package com.netease.easyml.local.mllib.miner;

/**
 * Created by eddielin on 2018/8/10.
 * chi-square distribution, χ2-distribution
 * chi^2 = N * (AD-BC)^2 / (A+B)(C+D)(A+C)(B+D)
 */
public class ChiSquare implements Significant {

    @Override
    public double[][] compute(double[][] distribution) {
        return compute(distribution, true);
    }

    public double[][] compute(double[][] distribution, boolean sign) {
        int rows = distribution.length;
        if (rows == 0)
            return new double[0][];
        int cols = distribution[0].length;

        double total = 0;
        double[] rowSum = new double[rows];
        double[] colSum = new double[cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double val = distribution[i][j];
                rowSum[i] += val;
                colSum[j] += val;
                total += val;
            }
        }

        double[][] significant = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double val = distribution[i][j];
                double row = rowSum[i];
                double col = colSum[j];
                double rc = row * col / total;
                significant[i][j] = Math.pow(val - rc, 2) / rc;
                if (sign && val < rc)
                    significant[i][j] *= -1;
            }
        }
        return significant;
    }
}
