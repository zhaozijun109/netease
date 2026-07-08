package com.netease.easyudf.udf.math;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.hadoop.hive.ql.exec.UDF;

public class BetaDistributionSampleUDF extends UDF {

    public Double evaluate(double alpha, double beta) {
        BetaDistribution d = new BetaDistribution(alpha, beta);
        return d.sample();
    }

}