package com.netease.easyml.common.reflection;

import com.netease.easyml.common.util.ArrayUtil;
import com.netease.easyml.common.util.StringUtil;
import numpy.core.NDArray;
import org.apache.spark.ml.linalg.*;

import java.lang.reflect.Field;

/**
 * Created by linjiuning on 2020/8/5.
 */
public class FromNDArray extends FromValue {
    public boolean isArray(Field field) {
        return field.getType().getName().startsWith("[");
    }

    @Override
    public boolean isMatch(Field field, Object value) {
        return value instanceof NDArray &&
                (Vector.class.isAssignableFrom(field.getType()) ||
                        Matrix.class.isAssignableFrom(field.getType()) ||
                        isArray(field));
    }

    public static Matrix toMatrix(NDArray ndArray) {
        if (ndArray == null) {
            return null;
        }
        int[] shape = ndArray.getArrayShape();
        Boolean fortranOrder = ndArray.getFortranOrder();
        double[] data = toDoubleArray(ndArray);
        int nRows = 1;
        int nCols = shape[shape.length - 1];
        if (shape.length == 2) {
            nRows = shape[0];
        }
        return new DenseMatrix(nRows, nCols, data, !fortranOrder);
    }

    public static Vector toVector(NDArray ndArray) {
        if (ndArray == null) {
            return null;
        }
        double[] data = toDoubleArray(ndArray);
        return Vectors.dense(data);
    }

    public static Object toArray(NDArray ndArray) {
        if (ndArray == null) {
            return null;
        }
        return ArrayUtil.toArray(ndArray.getContent());
    }

    public static double[] toDoubleArray(NDArray ndArray) {
        if (ndArray == null) {
            return null;
        }
        int[] shape = ndArray.getArrayShape();
        int size = 1;
        if (shape.length == 0) {
            size = 0;
        } else {
            for (int i : shape) {
                size *= i;
            }
        }
        double[] data = (double[]) ArrayUtil.zeros(double.class, size);
        ArrayUtil.map(ndArray.getContent(), data, (o) -> {
            if (o.getClass() == Double.class) {
                return o;
            } else {
                return Double.parseDouble(o.toString());
            }
        });
        return data;
    }

    @Override
    public Object fromValue(Field field, Object value) throws Exception {
        NDArray ndarray = (NDArray) value;
        Class<?> clazz = field.getType();
        int[] shape = ndarray.getArrayShape();

        if (shape.length == 1) {
            if (Matrix.class.isAssignableFrom(clazz)) {
                Matrices.dense(1, shape[0], toDoubleArray(ndarray));
            } else {
                if (isArray(field)) {
                    return toArray(ndarray);
                } else {
                    return Vectors.dense(toDoubleArray(ndarray));
                }
            }
        } else if (shape.length == 2) {
            return Matrices.dense(shape[0], shape[1], toDoubleArray(ndarray));
        } else {
            throw new IllegalArgumentException(String.format("NDArray shape: %s is not supported.", StringUtil.join(shape, ",")));
        }
        return value;
    }
}
