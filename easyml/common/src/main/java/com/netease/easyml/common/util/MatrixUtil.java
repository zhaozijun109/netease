package com.netease.easyml.common.util;

import com.netease.easyml.common.collection.Tuple;

import java.util.*;
import java.util.stream.Collectors;

import static com.netease.easyml.common.util.MathUtil.isZero;


/**
 * Created by linjiuning on 2018/7/27.
 */
public class MatrixUtil {

    public static List<Double> norm(List<Double> vector) {
        if (vector.isEmpty())
            return vector;
        double square = vector.stream().map(item -> Math.pow(item, 2)).reduce((item1, item2) -> item1 + item2).get();
        if (isZero(square))
            return vector;
        double length = Math.sqrt(square);
        return vector.stream().map(item -> item / length).collect(Collectors.toList());
    }

    public static double[] norm(double[] vector) {
        if (vector.length < 2)
            return vector;
        double square = 0.0;
        for (double v : vector)
            square += v * v;

        if (isZero(square))
            return vector;
        double length = Math.sqrt(square);
        double[] normVector = new double[vector.length];
        for (int i = 0; i < vector.length; i++)
            normVector[i] = vector[i] / length;

        return normVector;
    }

    public static float[] norm(float[] vector) {
        if (vector.length < 2)
            return vector;
        double square = 0.0;
        for (double v : vector)
            square += v * v;

        if (isZero(square))
            return vector;
        float length = (float) Math.sqrt(square);
        float[] normVector = new float[vector.length];
        for (int i = 0; i < vector.length; i++)
            normVector[i] = vector[i] / length;

        return normVector;
    }

    public static void norm_(double[] vector) {
        if (vector.length < 2)
            return;
        double square = 0.0;
        for (double v : vector)
            square += v * v;

        if (isZero(square))
            return;
        double length = Math.sqrt(square);
        for (int i = 0; i < vector.length; i++)
            vector[i] /= length;
    }

    public static List<Double> plus(List<Double> vector, List<Double>... others) {
        List<Double> newVector = new ArrayList<>();
        newVector.addAll(vector);

        for (List<Double> other : others) {
            for (int i = 0; i < newVector.size(); i++) {
                newVector.add(i, newVector.get(i) + other.get(i));
            }
        }
        return newVector;
    }

    public static double[] plus(double[] vector, double[]... others) {
        double[] newVector = new double[vector.length];

        System.arraycopy(vector, 0, newVector, 0, newVector.length);

        for (double[] other : others) {
            for (int i = 0; i < newVector.length; i++)
                newVector[i] += other[i];
        }
        return newVector;
    }

    public static double[] plus(List<double[]> vectors) {
        if (vectors.isEmpty())
            return new double[0];

        if (vectors.size() == 1)
            return vectors.get(0);

        double[] newVector = new double[vectors.get(0).length];
        System.arraycopy(vectors.get(0), 0, newVector, 0, newVector.length);
        for (int i = 1; i < vectors.size(); i++) {
            for (int j = 0; j < newVector.length; j++)
                newVector[j] += vectors.get(i)[j];
        }
        return newVector;
    }

    public static void plus_(List<double[]> vectors) {
        if (vectors.size() < 2)
            return;

        double[] newVector = vectors.get(0);
        for (int i = 1; i < vectors.size(); i++) {
            for (int j = 0; j < newVector.length; j++)
                newVector[j] += vectors.get(i)[j];
        }
    }

    public static List<Double> plus(List<Double> vector, Double scalar) {
        return vector.stream().map(item -> item + scalar).collect(Collectors.toList());
    }

    public static double[] plus(double[] vector, double scalar) {
        double[] newVector = new double[vector.length];
        for (int i = 0; i < vector.length; i++)
            newVector[i] = vector[i] + scalar;
        return newVector;
    }

    public static List<Double> minus(List<Double> vector, List<Double>... others) {
        List<Double> newVector = new ArrayList<>();
        newVector.addAll(vector);

        for (List<Double> other : others) {
            for (int i = 0; i < newVector.size(); i++) {
                newVector.add(i, newVector.get(i) - other.get(i));
            }
        }
        return newVector;
    }

    public static double[] minus(double[] vector, double[]... others) {
        double[] newVector = new double[vector.length];

        System.arraycopy(vector, 0, newVector, 0, newVector.length);

        for (double[] other : others) {
            for (int i = 0; i < newVector.length; i++)
                newVector[i] -= other[i];
        }
        return newVector;
    }

    public static double[] minus(List<double[]> vectors) {
        if (vectors.isEmpty())
            return null;

        if (vectors.size() == 1)
            return vectors.get(0);

        double[] newVector = new double[vectors.get(0).length];
        System.arraycopy(vectors.get(0), 0, newVector, 0, newVector.length);
        for (int i = 1; i < vectors.size(); i++) {
            for (int j = 0; j < newVector.length; j++)
                newVector[j] -= vectors.get(i)[j];
        }
        return newVector;
    }


    public static List<Double> minus(List<Double> vector, Double scalar) {
        return vector.stream().map(item -> item - scalar).collect(Collectors.toList());
    }

    public static double[] minus(double[] vector, double scalar) {
        double[] newVector = new double[vector.length];
        for (int i = 0; i < vector.length; i++)
            newVector[i] = vector[i] - scalar;
        return newVector;
    }

    public static List<Double> mul(List<Double> vector, List<Double>... others) {
        List<Double> newVector = new ArrayList<>();
        newVector.addAll(vector);

        for (List<Double> other : others) {
            for (int i = 0; i < newVector.size(); i++) {
                newVector.add(i, newVector.get(i) * other.get(i));
            }
        }
        return newVector;
    }

    public static double[] mul(double[] vector, double[]... others) {
        double[] newVector = new double[vector.length];

        System.arraycopy(vector, 0, newVector, 0, newVector.length);

        for (double[] other : others) {
            for (int i = 0; i < newVector.length; i++)
                newVector[i] *= other[i];
        }
        return newVector;
    }

    public static double[] mul(List<double[]> vectors) {
        if (vectors.isEmpty())
            return null;

        if (vectors.size() == 1)
            return vectors.get(0);

        double[] newVector = new double[vectors.get(0).length];
        System.arraycopy(vectors.get(0), 0, newVector, 0, newVector.length);
        for (int i = 1; i < vectors.size(); i++) {
            for (int j = 0; j < newVector.length; j++)
                newVector[j] *= vectors.get(i)[j];
        }
        return newVector;
    }

    public static List<Double> mul(List<Double> vector, Double scalar) {
        return vector.stream().map(item -> item * scalar).collect(Collectors.toList());
    }

    public static double[] mul(double[] vector, double scalar) {
        double[] newVector = new double[vector.length];
        for (int i = 0; i < vector.length; i++)
            newVector[i] = vector[i] * scalar;
        return newVector;
    }

    public static void mul_(double[] vector, double scalar) {
        for (int i = 0; i < vector.length; i++)
            vector[i] = vector[i] * scalar;
    }

    public static List<Double> div(List<Double> vector, List<Double>... others) {
        List<Double> newVector = new ArrayList<>();
        newVector.addAll(vector);

        for (List<Double> other : others) {
            for (int i = 0; i < newVector.size(); i++) {
                newVector.add(i, newVector.get(i) / other.get(i));
            }
        }
        return newVector;
    }

    public static double[] div(double[] vector, double[]... others) {
        double[] newVector = new double[vector.length];

        System.arraycopy(vector, 0, newVector, 0, newVector.length);

        for (double[] other : others) {
            for (int i = 0; i < newVector.length; i++)
                newVector[i] /= other[i];
        }
        return newVector;
    }

    public static double[] div(List<double[]> vectors) {
        if (vectors.isEmpty())
            return null;

        if (vectors.size() == 1)
            return vectors.get(0);

        double[] newVector = new double[vectors.get(0).length];
        System.arraycopy(vectors.get(0), 0, newVector, 0, newVector.length);
        for (int i = 1; i < vectors.size(); i++) {
            for (int j = 0; j < newVector.length; j++)
                newVector[j] /= vectors.get(i)[j];
        }
        return newVector;
    }

    public static List<Double> div(List<Double> vector, Double scalar) {
        return vector.stream().map(item -> item / scalar).collect(Collectors.toList());
    }

    public static double[] div(double[] vector, double scalar) {
        double[] newVector = new double[vector.length];
        for (int i = 0; i < vector.length; i++)
            newVector[i] = vector[i] / scalar;
        return newVector;
    }

    public static void div_(double[] vector, double scalar) {
        for (int i = 0; i < vector.length; i++)
            vector[i] = vector[i] / scalar;
    }

    public static double matmul(List<Double> vector1, List<Double> vector2) {
        assert vector1.size() == vector2.size();
        Iterator<Double> iter1 = vector1.iterator();
        Iterator<Double> iter2 = vector2.iterator();
        double res = 0.0;
        while (iter1.hasNext()) {
            res += (iter1.next() * iter2.next());
        }
        return res;
    }

    public static double matmul(double[] vector1, double[] vector2) {
        assert vector1.length == vector2.length;

        double res = 0.0;
        for (int i = 0; i < vector1.length; i++)
            res += vector1[i] * vector2[i];
        return res;
    }

    public static float matmul(float[] vector1, float[] vector2) {
        assert vector1.length == vector2.length;

        float res = 0.0f;
        for (int i = 0; i < vector1.length; i++)
            res += vector1[i] * vector2[i];
        return res;
    }

    /**
     * matmul a=[K, T], b=[1, T] => c=[1, K]
     */
    public static double[] matmul(double[][] vector1, double[] vector2) {
        double[] resMatrix = new double[vector1.length];
        for (int i = 0; i < resMatrix.length; i++) {
            double res = 0.0;
            for (int j = 0; j < vector1[i].length; j++)
                res += vector1[i][j] * vector2[j];
            resMatrix[i] = res;
        }
        return resMatrix;
    }

    /**
     * matmul a=[1, K], b=[K, T] => c=[1, T]
     */
    public static double[] matmul(double[] vector1, double[][] vector2) {
        double[] resMatrix = new double[vector2[0].length];
        for (int i = 0; i < resMatrix.length; i++) {
            double res = 0.0;
            for (int j = 0; j < vector2.length; j++)
                res += vector1[j] * vector2[j][i];
            resMatrix[i] = res;
        }
        return resMatrix;
    }

    public static double[] zeros(int size) {
        return new double[size];
    }

    public static double[] fill(int size, double value) {
        double[] vector = new double[size];
        for (int i = 0; i < vector.length; i++)
            vector[i] = value;
        return vector;
    }

    public static double cosine(double[] vector1, double[] vector2) {
        double[] norm1 = norm(vector1);
        double[] norm2 = norm(vector2);

        return matmul(norm1, norm2);
    }

    public static float cosine(float[] vector1, float[] vector2) {
        float[] norm1 = norm(vector1);
        float[] norm2 = norm(vector2);

        return matmul(norm1, norm2);
    }

    public static double[] sqrt(double[] vector) {
        double[] sqrtVec = new double[vector.length];
        for (int i = 0; i < sqrtVec.length; i++)
            sqrtVec[i] = Math.sqrt(vector[i]);
        return sqrtVec;
    }

    /**
     * Following is the sparse version
     */

    public static List<Tuple<Integer, Double>> sparseNorm(List<Tuple<Integer, Double>> vector) {
        if (vector.isEmpty())
            return vector;
        double square = vector.stream().map(item -> item.v2() * item.v2()).reduce((item1, item2) -> item1 + item2).get();
        if (isZero(square))
            return vector;
        double length = Math.sqrt(square);
        return vector.stream().map(item -> Tuple.tuple(item.v1(), item.v2() / length)).collect(Collectors.toList());
    }

    public static List<Tuple<Integer, Double>> sparsePlus(List<Tuple<Integer, Double>> vector, List<Tuple<Integer, Double>>... others) {
        if (others.length == 0)
            return vector;
        Map<Integer, Double> map = new TreeMap<>();
        vector.forEach(it -> map.put(it.v1(), it.v2()));
        for (List<Tuple<Integer, Double>> other : others) {
            for (Tuple<Integer, Double> value : other) {
                if (map.containsKey(value.v1()))
                    map.put(value.v1(), map.get(value.v1()) + value.v2());
                else
                    map.put(value.v1(), value.v2());
            }
        }
        List<Tuple<Integer, Double>> newVector = new ArrayList<>();
        map.forEach((k, v) -> {
            if (!isZero(v)) newVector.add(Tuple.tuple(k, v));
        });
        return newVector;
    }

    public static List<Tuple<Integer, Double>> sparsePlus(List<List<Tuple<Integer, Double>>> vectors) {
        if (vectors.isEmpty())
            return null;

        if (vectors.size() == 1)
            return vectors.get(0);

        Map<Integer, Double> map = new TreeMap<>();
        for (List<Tuple<Integer, Double>> other : vectors) {
            for (Tuple<Integer, Double> value : other) {
                if (map.containsKey(value.v1()))
                    map.put(value.v1(), map.get(value.v1()) + value.v2());
                else
                    map.put(value.v1(), value.v2());
            }
        }
        List<Tuple<Integer, Double>> newVector = new ArrayList<>();
        map.forEach((k, v) -> {
            if (!isZero(v)) newVector.add(Tuple.tuple(k, v));
        });
        return newVector;
    }

    public static List<Tuple<Integer, Double>> sparsePlus(List<Tuple<Integer, Double>> vector, Double scalar) {
        return vector.stream()
                .map(item -> Tuple.tuple(item.v1(), item.v2() + scalar))
                .filter(it -> !isZero(it.v2()))
                .collect(Collectors.toList());
    }

    public static List<Tuple<Integer, Double>> sparseMinus(List<Tuple<Integer, Double>> vector, List<Tuple<Integer, Double>>... others) {
        if (others.length == 0)
            return vector;
        Map<Integer, Double> map = new TreeMap<>();
        vector.forEach(it -> map.put(it.v1(), it.v2()));
        for (List<Tuple<Integer, Double>> other : others) {
            for (Tuple<Integer, Double> value : other) {
                if (map.containsKey(value.v1()))
                    map.put(value.v1(), map.get(value.v1()) - value.v2());
                else
                    map.put(value.v1(), value.v2());
            }
        }
        List<Tuple<Integer, Double>> newVector = new ArrayList<>();
        map.forEach((k, v) -> {
            if (!isZero(v)) newVector.add(Tuple.tuple(k, v));
        });
        return newVector;
    }

    public static List<Tuple<Integer, Double>> sparseMinus(List<List<Tuple<Integer, Double>>> vectors) {
        if (vectors.isEmpty())
            return null;

        if (vectors.size() == 1)
            return vectors.get(0);

        Map<Integer, Double> map = new TreeMap<>();
        for (List<Tuple<Integer, Double>> other : vectors) {
            for (Tuple<Integer, Double> value : other) {
                if (map.containsKey(value.v1()))
                    map.put(value.v1(), map.get(value.v1()) - value.v2());
                else
                    map.put(value.v1(), value.v2());
            }
        }
        List<Tuple<Integer, Double>> newVector = new ArrayList<>();
        map.forEach((k, v) -> {
            if (!isZero(v)) newVector.add(Tuple.tuple(k, v));
        });
        return newVector;
    }

    public static List<Tuple<Integer, Double>> sparseMinus(List<Tuple<Integer, Double>> vector, Double scalar) {
        return vector.stream()
                .map(item -> Tuple.tuple(item.v1(), item.v2() - scalar))
                .filter(it -> !isZero(it.v2()))
                .collect(Collectors.toList());
    }

    public static List<Tuple<Integer, Double>> sparseMul(List<Tuple<Integer, Double>> vector, List<Tuple<Integer, Double>>... others) {
        if (others.length == 0)
            return vector;
        Map<Integer, Double> map = new TreeMap<>();
        vector.forEach(it -> map.put(it.v1(), it.v2()));
        for (List<Tuple<Integer, Double>> other : others) {
            for (Tuple<Integer, Double> value : other) {
                if (map.containsKey(value.v1()))
                    map.put(value.v1(), map.get(value.v1()) * value.v2());
                else
                    map.put(value.v1(), value.v2());
            }
        }
        List<Tuple<Integer, Double>> newVector = new ArrayList<>();
        map.forEach((k, v) -> {
            if (!isZero(v)) newVector.add(Tuple.tuple(k, v));
        });
        return newVector;
    }

    public static List<Tuple<Integer, Double>> sparseMul(List<List<Tuple<Integer, Double>>> vectors) {
        if (vectors.isEmpty())
            return null;

        if (vectors.size() == 1)
            return vectors.get(0);

        Map<Integer, Double> map = new TreeMap<>();
        for (List<Tuple<Integer, Double>> other : vectors) {
            for (Tuple<Integer, Double> value : other) {
                if (map.containsKey(value.v1()))
                    map.put(value.v1(), map.get(value.v1()) * value.v2());
                else
                    map.put(value.v1(), value.v2());
            }
        }
        List<Tuple<Integer, Double>> newVector = new ArrayList<>();
        map.forEach((k, v) -> {
            if (!isZero(v)) newVector.add(Tuple.tuple(k, v));
        });
        return newVector;
    }

    public static List<Tuple<Integer, Double>> sparseMul(List<Tuple<Integer, Double>> vector, Double scalar) {
        return vector.stream()
                .map(item -> Tuple.tuple(item.v1(), item.v2() * scalar))
                .filter(it -> !isZero(it.v2()))
                .collect(Collectors.toList());
    }

    public static List<Tuple<Integer, Double>> sparseDiv(List<Tuple<Integer, Double>> vector, List<Tuple<Integer, Double>>... others) {
        if (others.length == 0)
            return vector;
        Map<Integer, Double> map = new TreeMap<>();
        vector.forEach(it -> map.put(it.v1(), it.v2()));
        for (List<Tuple<Integer, Double>> other : others) {
            for (Tuple<Integer, Double> value : other) {
                if (map.containsKey(value.v1()))
                    map.put(value.v1(), map.get(value.v1()) / value.v2());
                else
                    map.put(value.v1(), value.v2());
            }
        }
        List<Tuple<Integer, Double>> newVector = new ArrayList<>();
        map.forEach((k, v) -> {
            if (!isZero(v)) newVector.add(Tuple.tuple(k, v));
        });
        return newVector;
    }

    public static List<Tuple<Integer, Double>> sparseDiv(List<List<Tuple<Integer, Double>>> vectors) {
        if (vectors.isEmpty())
            return null;

        if (vectors.size() == 1)
            return vectors.get(0);

        Map<Integer, Double> map = new TreeMap<>();
        for (List<Tuple<Integer, Double>> other : vectors) {
            for (Tuple<Integer, Double> value : other) {
                if (map.containsKey(value.v1()))
                    map.put(value.v1(), map.get(value.v1()) / value.v2());
                else
                    map.put(value.v1(), value.v2());
            }
        }
        List<Tuple<Integer, Double>> newVector = new ArrayList<>();
        map.forEach((k, v) -> {
            if (!isZero(v)) newVector.add(Tuple.tuple(k, v));
        });
        return newVector;
    }

    public static List<Tuple<Integer, Double>> sparseDiv(List<Tuple<Integer, Double>> vector, Double scalar) {
        return vector.stream()
                .map(item -> Tuple.tuple(item.v1(), item.v2() / scalar))
                .filter(it -> !isZero(it.v2()))
                .collect(Collectors.toList());
    }

    public static double sparseMatmul(List<Tuple<Integer, Double>> vector1, List<Tuple<Integer, Double>> vector2) {
        double res = 0.0;
        int i = 0, j = 0;
        while (i < vector1.size() && j < vector2.size()) {
            Tuple<Integer, Double> value1 = vector1.get(i);
            Tuple<Integer, Double> value2 = vector2.get(j);
            if (value1.v1().equals(value2.v1())) {
                res += (value1.v2() * value2.v2());
                i++;
                j++;
            } else if (value1.v1() < value2.v1())
                i++;
            else
                j++;
        }
        return res;
    }

    public static List<Tuple<Integer, Double>> sparseZeros() {
        return Collections.emptyList();
    }

    public static List<Tuple<Integer, Double>> sparseFill(int size, double value) {
        if (isZero(value))
            return Collections.emptyList();
        List<Tuple<Integer, Double>> vector = new ArrayList<>();
        for (int i = 0; i < size; i++)
            vector.add(Tuple.tuple(i, value));
        return vector;
    }

    public static double sparseCosine(List<Tuple<Integer, Double>> vector1, List<Tuple<Integer, Double>> vector2) {
        List<Tuple<Integer, Double>> norm1 = sparseNorm(vector1);
        List<Tuple<Integer, Double>> norm2 = sparseNorm(vector2);

        return sparseMatmul(norm1, norm2);
    }

    public static List<Tuple<Integer, Double>> sparseSqrt(List<Tuple<Integer, Double>> vector) {
        List<Tuple<Integer, Double>> sqrtVec = new ArrayList<>();
        for (Tuple<Integer, Double> elem : vector)
            sqrtVec.add(Tuple.tuple(elem.v1(), Math.sqrt(elem.v2())));
        return sqrtVec;
    }

    /**
     * Following is the convert between sparse and dense vector
     */

    public static double[] toDense(List<Tuple<Integer, Double>> sparse, int size) {
        double[] dense = new double[size];
        for (Tuple<Integer, Double> value : sparse) {
            dense[value.v1()] = value.v2();
        }
        return dense;
    }

    public static List<Tuple<Integer, Double>> toSparse(double[] dense) {
        List<Tuple<Integer, Double>> sparse = new ArrayList<>();
        for (int i = 0; i < dense.length; i++) {
            if (!isZero(dense[i]))
                sparse.add(Tuple.tuple(i, dense[i]));
        }
        return sparse;
    }

    public static List<Tuple<Integer, Double>> toSparse(List<Double> dense) {
        List<Tuple<Integer, Double>> sparse = new ArrayList<>();
        for (int i = 0; i < dense.size(); i++) {
            if (!isZero(dense.get(i)))
                sparse.add(Tuple.tuple(i, dense.get(i)));
        }
        return sparse;
    }
}
