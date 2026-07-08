package com.netease.easyudf.udf.collect;

import org.apache.hadoop.hive.ql.exec.UDAF;
import org.apache.hadoop.hive.ql.exec.UDAFEvaluator;

import java.util.ArrayList;
import java.util.List;

/**
 * Compute the weighted average.  This replaces the common idiom:
 * <p>
 * SUM(value * weight) / SUM(weight)
 * <p>
 * If either the value or the weight is NULL, then NULL is returned.  NULL is
 * also returned if there are no non-NULL rows are seen.
 */
public final class ArrayDoubleWeightAvgUDAF extends UDAF {

    public static class UDAFArrayDoubleEvaluator implements UDAFEvaluator {

        public static final class State {
            public List<Double> sumValue = null;
            public double sumWeight = 0;
        }

        State state;

        public UDAFArrayDoubleEvaluator() {
            init();
        }

        @Override
        public void init() {
            state = new State();
        }

        public boolean iterate(List<Double> value, Double weight) {
            if (value == null || weight == null) {
                return true;
            }

            if (state.sumValue == null) {
                state.sumValue = new ArrayList<>();
                for (Double v : value) {
                    state.sumValue.add(v * weight);
                }
            } else {
                for (int i = 0; i < value.size(); i++) {
                    state.sumValue.set(i, state.sumValue.get(i) + value.get(i) * weight);
                }
            }

            state.sumWeight += weight;
            return true;
        }

        public State terminatePartial() {
            return state;
        }

        public boolean merge(State other) {
            if (other == null) {
                return true;
            }

            if (other.sumValue != null) {
                if (state.sumValue == null) {
                    state.sumValue = new ArrayList<>();
                    state.sumValue.addAll(other.sumValue);
                } else {
                    for (int i = 0; i < state.sumValue.size(); i++) {
                        state.sumValue.set(i, state.sumValue.get(i) + other.sumValue.get(i));
                    }
                }
            }
            state.sumWeight += other.sumWeight;
            return true;
        }

        public List<Double> terminate() {
            if (state.sumValue == null) {
                return null;
            }

            for (int i = 0; i < state.sumValue.size(); i++) {
                state.sumValue.set(i, state.sumValue.get(i) / state.sumWeight);
            }
            return state.sumValue;
        }
    }

}