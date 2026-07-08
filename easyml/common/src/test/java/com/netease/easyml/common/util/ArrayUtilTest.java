package com.netease.easyml.common.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by linjiuning on 2020/8/5.
 */
public class ArrayUtilTest {
    @Test
    public void componentType() {
        List<Integer> array = Arrays.asList(1, 2);
        Class<?> clazz = ArrayUtil.componentType(array);
        assertEquals(clazz, int.class);

        array = Collections.emptyList();
        clazz = ArrayUtil.componentType(array);
        assertNotEquals(clazz, int.class);
    }
}