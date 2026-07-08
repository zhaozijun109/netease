package com.netease.easyml.ndarray.util;

import ai.djl.ndarray.NDArray;
import com.netease.easyml.common.util.ArrayUtil;
import org.junit.Test;

import java.util.Random;

/**
 * Created by linjiuning on 2020/8/3.
 */
public class NDArrayUtilTest {
    private final Random rng = new Random(1024);

    @Test
    public void convert() {
        Object javaArray = ArrayUtil.zeros(double.class, new int[]{3, 3});
        ArrayUtil.map(javaArray, (ignore) -> rng.nextDouble());
        System.out.println("Java Array:");
        System.out.println(ArrayUtil.toString(javaArray));

        NDArray ndArray = NDArrayUtil.toNDArray(javaArray);
        System.out.println("NdArray Array:");
        System.out.println(ndArray.toString());

        Object backJavaArray = NDArrayUtil.toJavaArray(ndArray);
        System.out.println("Back Java Array Array:");
        System.out.println(ArrayUtil.toString(backJavaArray));

        Object backJavaNDArray = NDArrayUtil.toJavaNDArray(ndArray);
        System.out.println("Back Java NDArray Array:");
        System.out.println(ArrayUtil.toString(backJavaNDArray));
    }
}