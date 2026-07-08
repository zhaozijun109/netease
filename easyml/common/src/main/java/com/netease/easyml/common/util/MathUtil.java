package com.netease.easyml.common.util;

import org.javatuples.Pair;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by linjiuning on 2018/6/5.
 */
public class MathUtil {
    public static final double EPS = 1e-8;

    public static boolean isZero(double value) {
        return value >= -EPS && value <= EPS;
    }

    public static void shuffle(Random rnd, float[] values) {
        for (int i = values.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            float a = values[index];
            values[index] = values[i];
            values[i] = a;
        }
    }

    public static void shuffle(float[] values) {
        shuffle(ThreadLocalRandom.current(), values);
    }

    public static void shuffle(Random rnd, double[] values) {
        for (int i = values.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            double a = values[index];
            values[index] = values[i];
            values[i] = a;
        }
    }

    public static void shuffle(double[] values) {
        shuffle(ThreadLocalRandom.current(), values);
    }

    public static void shuffle(Random rnd, long[] values) {
        for (int i = values.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            long a = values[index];
            values[index] = values[i];
            values[i] = a;
        }
    }

    public static void shuffle(long[] values) {
        shuffle(ThreadLocalRandom.current(), values);
    }

    public static void shuffle(Random rnd, int[] values) {
        for (int i = values.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = values[index];
            values[index] = values[i];
            values[i] = a;
        }
    }

    public static void shuffle(int[] values) {
        shuffle(ThreadLocalRandom.current(), values);
    }

    public static <T> void shuffle(Random rnd, T[] values) {
        for (int i = values.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            T a = values[index];
            values[index] = values[i];
            values[i] = a;
        }
    }

    public static <T> void shuffle(T[] values) {
        shuffle(ThreadLocalRandom.current(), values);
    }

    public static <T> void shuffle(Random rnd, List<T> values) {
        Collections.shuffle(values, rnd);
    }

    public static <T> void shuffle(List<T> values) {
        Collections.shuffle(values, ThreadLocalRandom.current());
    }

    public static <T> void shuffleSorted(Random rnd, List<T> sortedValues, Comparator<T> comparator) {
        if (sortedValues.size() < 2)
            return;
        int start = 0;
        int end = 1;
        List<Pair<Integer, Integer>> ranges = new ArrayList<>();
        while (end < sortedValues.size()) {
            if (comparator.compare(sortedValues.get(end), sortedValues.get(start)) == 0)
                end++;
            else {
                if (end - start > 1)
                    ranges.add(new Pair<>(start, end));
                start = end;
            }
        }
        if (end - start > 1)
            ranges.add(new Pair<>(start, end));
        for (Pair<Integer, Integer> range : ranges) {
            shuffle(rnd, sortedValues.subList(range.getValue0(), range.getValue1()));
        }
    }

    public static <T> void shuffleSorted(List<T> sortedValues, Comparator<T> comparator) {
        shuffleSorted(ThreadLocalRandom.current(), sortedValues, comparator);
    }

    public static double safeDiv(double a, double b) {
        return b != 0.0 ? a / b : a / (b + EPS);
    }

    public static <T> List<T> repeatPad(List<T> values, int number) {
        if (values == null || values.isEmpty() || number <= 0)
            return values;
        if (values.size() > number)
            return values.subList(0, number);
        else if (values.size() == number)
            return values;
        else {
            List<T> padValues = new ArrayList<>(number);
            while (padValues.size() < number) {
                padValues.addAll(
                        values.subList(0, Math.min(number - padValues.size(), values.size()))
                );
            }
            return padValues;
        }
    }

    public static <T> T choice(Random rnd, Collection<T> values) {
        if (values == null || values.isEmpty())
            return null;
        Iterator<T> iterator = values.iterator();
        if (values.size() == 1)
            return iterator.next();
        int idx = rnd.nextInt(values.size());
        T value = null;
        while (idx-- >= 0) {
            value = iterator.next();
        }
        return value;
    }

    public static <T> T choice(Collection<T> values) {
        return choice(ThreadLocalRandom.current(), values);
    }

    public static <T> T choice(Random rnd, T[] values) {
        if (values == null || values.length == 0)
            return null;
        if (values.length == 1)
            return values[0];
        int idx = rnd.nextInt(values.length);
        return values[idx];
    }

    public static <T> T choice(T[] values) {
        return choice(ThreadLocalRandom.current(), values);
    }

    public static <T> T choice(Random rnd, List<T> values, double[] probs) {
        assert values.size() == probs.length;
        if (probs.length == 0)
            return null;
        double sum = 0.0;
        double[] cumSum = new double[probs.length - 1];
        for (int i = 0; i < probs.length; i++) {
            sum += probs[i];
            if (i < cumSum.length)
                cumSum[i] = sum;
        }
        double next = rnd.nextDouble() * sum;
        int idx = CollectionUtil.lowerBound(cumSum, next);
        return values.get(idx);
    }

    public static <T> T choice(List<T> values, double[] probs) {
        return choice(ThreadLocalRandom.current(), values, probs);
    }

    public static <T> T choice(Random rnd, T[] values, double[] probs) {
        assert values.length == probs.length;
        if (probs.length == 0)
            return null;
        double sum = 0.0;
        double[] cumSum = new double[probs.length - 1];
        for (int i = 0; i < probs.length; i++) {
            sum += probs[i];
            if (i < cumSum.length)
                cumSum[i] = sum;
        }
        double next = rnd.nextDouble() * sum;
        int idx = CollectionUtil.lowerBound(cumSum, next);
        return values[idx];
    }

    public static <T> T choice(T[] values, double[] probs) {
        return choice(ThreadLocalRandom.current(), values, probs);
    }

    public static <T> List<List<T>> combinations(List<T> values, int num) {
        List<List<T>> comb = new ArrayList<>();
        if (num == 0) {
            comb.add(Collections.emptyList());
            return comb;
        }
        for (int i = 0; i <= values.size() - num; i++) {
            List<T> subList = values.subList(i, i + num);
            comb.add(subList);
        }
        return comb;
    }

    public static <T extends Comparable<T>> boolean overlap(T s1, T e1, T s2, T e2) {
        int c1 = s1.compareTo(e2);
        int c2 = s2.compareTo(e1);
        if (c1 >= 0 || c2 >= 0)
            return false;
        return c1 * c2 >= 0;
    }
}
