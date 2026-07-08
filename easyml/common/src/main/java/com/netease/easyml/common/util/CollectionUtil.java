package com.netease.easyml.common.util;

import scala.Predef;
import scala.collection.JavaConverters;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by linjiuning on 2020/7/8.
 */
public class CollectionUtil {
    public static Object fastJsonToJava(Object obj) {
        if (obj instanceof Map) {
            Map map = (Map) obj;
            Map nMap = new HashMap();
            for (Object k : map.keySet()) {
                nMap.put(k, fastJsonToJava(map.get(k)));
            }
            return nMap;
        } else if (obj instanceof Collection) {
            Collection col = (Collection) obj;
            Object nCol = new String[0];
            int i = 0;
            for (Object o : col) {
                o = fastJsonToJava(o);
                if (ArrayUtil.isEmpty(nCol)) {
                    nCol = ArrayUtil.zeros(o.getClass(), col.size());
                }
                ArrayUtil.set(nCol, i++, o);
            }
            return nCol;
        } else if (obj instanceof BigDecimal) {
            return ((BigDecimal) obj).doubleValue();
        }
        return obj;
    }

    public static Object jacksonToJava(Object obj) {
        if (obj instanceof Map) {
            Map map = (Map) obj;
            Map nMap = new HashMap();
            for (Object k : map.keySet()) {
                nMap.put(k, jacksonToJava(map.get(k)));
            }
            return nMap;
        } else if (obj instanceof Collection) {
            Collection col = (Collection) obj;
            Object nCol = new String[0];
            int i = 0;
            for (Object o : col) {
                o = jacksonToJava(o);
                if (ArrayUtil.isEmpty(nCol)) {
                    nCol = ArrayUtil.zeros(o.getClass(), col.size());
                }
                ArrayUtil.set(nCol, i++, o);
            }
            return nCol;
        }
        return obj;
    }

    public static <A, B> scala.collection.Map<A, B> toScalaMap(Map<A, B> m) {
        return JavaConverters.mapAsScalaMapConverter(m)
                .asScala()
                .toMap(Predef.conforms());
    }

    public static <A> scala.collection.Set<A> toScalaSet(Set<A> m) {
        return JavaConverters.asScalaSetConverter(m)
                .asScala()
                .toSet();
    }

    public static <A> scala.collection.immutable.List<A> toScalaList(Collection<A> m) {
        return JavaConverters.collectionAsScalaIterableConverter(m)
                .asScala()
                .toList();
    }

    public static List<Long> toList(long[] array) {
        return Arrays.stream(array).boxed().collect(Collectors.toList());
    }

    public static List<Integer> toList(int[] array) {
        return Arrays.stream(array).boxed().collect(Collectors.toList());
    }

    public static List<Double> toList(double[] array) {
        return Arrays.stream(array).boxed().collect(Collectors.toList());
    }

    public static List<Float> toList(float[] array) {
        List<Float> result = new ArrayList<>(array.length);
        for (float v : array)
            result.add(v);
        return result;
    }

    public static List<Boolean> toList(boolean[] array) {
        List<Boolean> result = new ArrayList<>(array.length);
        for (boolean v : array)
            result.add(v);
        return result;
    }

    public static <T> List<T> toList(T[] array) {
        return Arrays.stream(array).collect(Collectors.toList());
    }

    public static long[] toLongArray(List<Long> list) {
        long[] result = new long[list.size()];
        for (int i = 0; i < list.size(); i++)
            result[i] = list.get(i);
        return result;
    }

    public static int[] toIntArray(List<Integer> list) {
        int[] result = new int[list.size()];
        for (int i = 0; i < list.size(); i++)
            result[i] = list.get(i);
        return result;
    }

    public static double[] toDoubleArray(List<Double> list) {
        double[] result = new double[list.size()];
        for (int i = 0; i < list.size(); i++)
            result[i] = list.get(i);
        return result;
    }

    public static float[] toFloatArray(List<Float> list) {
        float[] result = new float[list.size()];
        for (int i = 0; i < list.size(); i++)
            result[i] = list.get(i);
        return result;
    }

    public static <T, R> List<R> map(T[] array, Function<? super T, ? extends R> mapper) {
        return Arrays.stream(array).map(mapper).collect(Collectors.toList());
    }

    public static <T, R> List<R> map(Collection<T> array, Function<? super T, ? extends R> mapper) {
        return array.stream().map(mapper).collect(Collectors.toList());
    }

    public static <R> List<R> map(int[] array, Function<? super Integer, ? extends R> mapper) {
        List<R> result = new ArrayList<>();
        for (int it : array) {
            result.add(mapper.apply(it));
        }
        return result;
    }

    public static <R> List<R> map(long[] array, Function<? super Long, ? extends R> mapper) {
        List<R> result = new ArrayList<>();
        for (long it : array) {
            result.add(mapper.apply(it));
        }
        return result;
    }

    public static <R> List<R> map(float[] array, Function<? super Float, ? extends R> mapper) {
        List<R> result = new ArrayList<>();
        for (float it : array) {
            result.add(mapper.apply(it));
        }
        return result;
    }

    public static <R> List<R> map(double[] array, Function<? super Double, ? extends R> mapper) {
        List<R> result = new ArrayList<>();
        for (double it : array) {
            result.add(mapper.apply(it));
        }
        return result;
    }

    public static <R> List<R> map(boolean[] array, Function<? super Boolean, ? extends R> mapper) {
        List<R> result = new ArrayList<>();
        for (boolean it : array) {
            result.add(mapper.apply(it));
        }
        return result;
    }

    public static <T> List<T> filter(T[] array, Predicate<? super T> predicate) {
        return Arrays.stream(array).filter(predicate).collect(Collectors.toList());
    }

    public static <T> List<T> filter(Collection<T> array, Predicate<? super T> predicate) {
        return array.stream().filter(predicate).collect(Collectors.toList());
    }

    public static List<Integer> filter(int[] array, Predicate<? super Integer> predicate) {
        List<Integer> result = new ArrayList<>();
        for (int it : array) {
            if (predicate.test(it))
                result.add(it);
        }
        return result;
    }

    public static List<Long> filter(long[] array, Predicate<? super Long> predicate) {
        List<Long> result = new ArrayList<>();
        for (long it : array) {
            if (predicate.test(it))
                result.add(it);
        }
        return result;
    }

    public static List<Float> filter(float[] array, Predicate<? super Float> predicate) {
        List<Float> result = new ArrayList<>();
        for (float it : array) {
            if (predicate.test(it))
                result.add(it);
        }
        return result;
    }

    public static List<Double> filter(double[] array, Predicate<? super Double> predicate) {
        List<Double> result = new ArrayList<>();
        for (double it : array) {
            if (predicate.test(it))
                result.add(it);
        }
        return result;
    }

    public static List<Boolean> filter(boolean[] array, Predicate<? super Boolean> predicate) {
        List<Boolean> result = new ArrayList<>();
        for (boolean it : array) {
            if (predicate.test(it))
                result.add(it);
        }
        return result;
    }

    public static long[][] repeat(long[] array, int size, boolean copy) {
        long[][] res = new long[size][array.length];
        for (int i = 0; i < size; i++) {
            if (copy) {
                System.arraycopy(array, 0, res[i], 0, array.length);
            } else {
                res[i] = array;
            }
        }
        return res;
    }

    public static int[][] repeat(int[] array, int size, boolean copy) {
        int[][] res = new int[size][array.length];
        for (int i = 0; i < size; i++) {
            if (copy) {
                System.arraycopy(array, 0, res[i], 0, array.length);
            } else {
                res[i] = array;
            }
        }
        return res;
    }

    public static double[][] repeat(double[] array, int size, boolean copy) {
        double[][] res = new double[size][array.length];
        for (int i = 0; i < size; i++) {
            if (copy) {
                System.arraycopy(array, 0, res[i], 0, array.length);
            } else {
                res[i] = array;
            }
        }
        return res;
    }

    public static float[][] repeat(float[] array, int size, boolean copy) {
        float[][] res = new float[size][array.length];
        for (int i = 0; i < size; i++) {
            if (copy) {
                System.arraycopy(array, 0, res[i], 0, array.length);
            } else {
                res[i] = array;
            }
        }
        return res;
    }

    public static boolean[][] repeat(boolean[] array, int size, boolean copy) {
        boolean[][] res = new boolean[size][array.length];
        for (int i = 0; i < size; i++) {
            if (copy) {
                System.arraycopy(array, 0, res[i], 0, array.length);
            } else {
                res[i] = array;
            }
        }
        return res;
    }

    public enum SIDE {
        LEFT,
        RIGHT
    }

    public static int lowerBound(double[] x, double value) {
        int count = x.length;

        int it, step, first = 0;

        while (count > 0) {
            it = first;
            step = count / 2;
            it += step;

            if (x[it] < value) {
                first = ++it;
                count -= step + 1;
            } else
                count = step;
        }
        return first;
    }

    public static int upperBound(double[] x, double value) {
        int count = x.length;

        int it, step, first = 0;

        while (count > 0) {
            it = first;
            step = count / 2;
            it += step;

            if (x[it] <= value) {
                first = ++it;
                count -= step + 1;
            } else
                count = step;
        }
        return first;
    }

    public static int searchsorted(double[] x, double value, SIDE side) {
        return side.equals(SIDE.LEFT) ? lowerBound(x, value) : upperBound(x, value);
    }

    public static int searchsorted(double[] x, double value) {
        return searchsorted(x, value, SIDE.LEFT);
    }

    public static int lowerBound(float[] x, float value) {
        int count = x.length;

        int it, step, first = 0;

        while (count > 0) {
            it = first;
            step = count / 2;
            it += step;

            if (x[it] < value) {
                first = ++it;
                count -= step + 1;
            } else
                count = step;
        }
        return first;
    }

    public static int upperBound(float[] x, float value) {
        int count = x.length;

        int it, step, first = 0;

        while (count > 0) {
            it = first;
            step = count / 2;
            it += step;

            if (x[it] <= value) {
                first = ++it;
                count -= step + 1;
            } else
                count = step;
        }
        return first;
    }

    public static int searchsorted(float[] x, float value, SIDE side) {
        return side.equals(SIDE.LEFT) ? lowerBound(x, value) : upperBound(x, value);
    }

    public static int searchsorted(float[] x, float value) {
        return searchsorted(x, value, SIDE.LEFT);
    }

    public static int lowerBound(int[] x, int value) {
        int count = x.length;

        int it, step, first = 0;

        while (count > 0) {
            it = first;
            step = count / 2;
            it += step;

            if (x[it] < value) {
                first = ++it;
                count -= step + 1;
            } else
                count = step;
        }
        return first;
    }

    public static int upperBound(int[] x, int value) {
        int count = x.length;

        int it, step, first = 0;

        while (count > 0) {
            it = first;
            step = count / 2;
            it += step;

            if (x[it] <= value) {
                first = ++it;
                count -= step + 1;
            } else
                count = step;
        }
        return first;
    }

    public static int searchsorted(int[] x, int value, SIDE side) {
        return side.equals(SIDE.LEFT) ? lowerBound(x, value) : upperBound(x, value);
    }

    public static int searchsorted(int[] x, int value) {
        return searchsorted(x, value, SIDE.LEFT);
    }

    public static int lowerBound(long[] x, long value) {
        int count = x.length;

        int it, step, first = 0;

        while (count > 0) {
            it = first;
            step = count / 2;
            it += step;

            if (x[it] < value) {
                first = ++it;
                count -= step + 1;
            } else
                count = step;
        }
        return first;
    }

    public static int upperBound(long[] x, long value) {
        int count = x.length;

        int it, step, first = 0;

        while (count > 0) {
            it = first;
            step = count / 2;
            it += step;

            if (x[it] <= value) {
                first = ++it;
                count -= step + 1;
            } else
                count = step;
        }
        return first;
    }

    public static int searchsorted(long[] x, long value, SIDE side) {
        return side.equals(SIDE.LEFT) ? lowerBound(x, value) : upperBound(x, value);
    }

    public static int searchsorted(long[] x, long value) {
        return searchsorted(x, value, SIDE.LEFT);
    }

    public static <T> boolean isEmpty(Collection<T> values) {
        return values == null || values.isEmpty();
    }

    public static <T> boolean isEmpty(T[] values) {
        return values == null || values.length == 0;
    }

    //求交集
    public static <T> Set<T> intersection(Collection<T> coll1, Collection<T> coll2) {
        return Stream.concat(coll1.stream(), coll2.stream())
                .filter(coll1::contains)
                .filter(coll2::contains)
                .collect(Collectors.toSet());
    }

    public static Object deepMerge(Object obj1, Object obj2) {
        Object r = obj1;
        try {
            if (obj1 instanceof Collection) {
                r = new ArrayList<>();
                ((Collection) r).addAll((Collection) obj1);
                ((Collection) r).addAll((Collection) obj2);
            } else if (obj1 instanceof Map) {
                r = new HashMap<>();
                for (Object o : ((Map) obj1).keySet()) {
                    if (((Map) obj2).containsKey(o)) {
                        ((Map) r).put(o, deepMerge(((Map) obj1).get(o), ((Map) obj2).get(o)));
                    } else
                        ((Map) r).put(o, ((Map) obj1).get(o));
                }
                for (Object o : ((Map) obj2).keySet()) {
                    if (!((Map) obj1).containsKey(o)) {
                        ((Map) r).put(o, ((Map) obj2).get(o));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    public static Map map(Object... arrays) {
        if (arrays.length % 2 != 0) {
            throw new IllegalArgumentException("arrays length must be even.");
        }
        Map maps = new HashMap();
        for (int i = 0; i < arrays.length; i += 2) {
            maps.put(arrays[i], arrays[i + 1]);
        }
        return maps;
    }

    public static <T> List<List<T>> groupsOf(List<T> examples, int batchSize) {
        List<List<T>> batches = new ArrayList<>();

        int start = 0;
        while (start < examples.size()) {
            int end = Math.min(examples.size(), start + batchSize);
            List<T> batch = examples.subList(start, end);
            batches.add(batch);
            start = end;
        }
        return batches;
    }

    public static <T> List<List<T>> groupsOf(T[] examples, int batchSize) {
        List<List<T>> batches = new ArrayList<>();

        int start = 0;
        while (start < examples.length) {
            int end = Math.min(examples.length, start + batchSize);
            List<T> batch = new ArrayList<>();
            for (int i = start; i < end; i++) {
                batch.add(examples[i]);
            }
            batches.add(batch);
            start = end;
        }
        return batches;
    }
}
