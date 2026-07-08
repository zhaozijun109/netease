package com.netease.easyml.local.mllib.tokenizer.transformers;


import org.javatuples.Pair;

import java.util.Objects;

/**
 * Created by linjiuning on 2020/2/15.
 */
public class TruncateStrategy {
    public static final String LONGEST_FIRST = "longest_first";
    public static final String ONLY_FIRST = "only_first";
    public static final String ONLY_SECOND = "only_second";

    public static Pair<int[], int[]> truncateSequences(int[] ids, int[] pair, int numTokensToRemove) {
        if (numTokensToRemove <= 0) {
            return new Pair<>(ids, pair);
        }
        if (pair == null) {
            ids = truncateSequences(ids, numTokensToRemove);
            return new Pair<>(ids, pair);
        }

        boolean swap = false;

        int[] more = ids;
        int[] less = pair;
        if (ids.length < pair.length) {
            less = ids;
            more = pair;
            swap = true;
        }
        int diff = more.length - less.length;
        int m = 0;
        int l = 0;
        if (diff >= numTokensToRemove) {
            m = numTokensToRemove;
        } else {
            int half = (numTokensToRemove - diff) / 2;
            m = diff + half;
            l = numTokensToRemove - m;
        }

        more = truncateSequences(more, m);
        less = truncateSequences(less, l);

        if (swap) {
            return new Pair<>(less, more);
        }
        return new Pair<>(more, less);
    }

    private static int[] truncateSequences(int[] ids, int numTokensToRemove) {
        if (numTokensToRemove <= 0) {
            return ids;
        }
        int length = Math.max(0, ids.length - numTokensToRemove);
        int[] newIds = new int[length];
        System.arraycopy(ids, 0, newIds, 0, newIds.length);
        return newIds;
    }

    public static Pair<int[], int[]> truncateSequences(int[] ids, int[] pair, int numTokensToRemove, String strategy) {
        if (numTokensToRemove <= 0) {
            return new Pair<>(ids, pair);
        }
        if (Objects.equals(strategy, LONGEST_FIRST)) {
            return truncateSequences(ids, pair, numTokensToRemove);
        } else if (Objects.equals(strategy, ONLY_FIRST)) {
            ids = truncateSequences(ids, numTokensToRemove);
        } else if (Objects.equals(strategy, ONLY_SECOND)) {
            pair = truncateSequences(pair, numTokensToRemove);
        } else {
            throw new IllegalArgumentException("Truncation_strategy should be selected in ['longest_first', 'only_first', 'only_second']");
        }
        return new Pair<>(ids, pair);
    }
}
