package com.netease.easyml.common.reflection;

import org.apache.spark.ml.linalg.DenseMatrix;
import org.apache.spark.ml.linalg.Matrices;
import org.apache.spark.ml.linalg.Matrix;
import scipy.sparse.CSRMatrix;

import java.lang.reflect.Field;

import static com.netease.easyml.common.util.ArrayUtil.toArray;

/**
 * Created by linjiuning on 2020/8/5.
 */
public class FromCSRMatrix extends FromValue {
    @Override
    public boolean isMatch(Field field, Object value) {
        return value instanceof CSRMatrix && Matrix.class.isAssignableFrom(field.getType());
    }

    public static Matrix toMatrix(CSRMatrix csrMatrix) {
        int[] shape = csrMatrix.getArrayShape();
        int nRow = shape[0];
        int nCol = shape[1];

        int[] indices = (int[]) toArray(csrMatrix.getIndices());
        int[] indPtr = (int[]) toArray(csrMatrix.getIndPtr());
        double[] data = (double[]) toArray(csrMatrix.getData());

        return Matrices.sparse(nRow, nCol, indPtr, indices, data);
    }

    @Override
    public Object fromValue(Field field, Object value) throws Exception {
        CSRMatrix csrMatrix = (CSRMatrix) value;
        Class<?> clazz = field.getType();

        Matrix sparse = toMatrix(csrMatrix);
        if (clazz == DenseMatrix.class) {
            return sparse.toDense();
        } else {
            return sparse;
        }
    }
}
