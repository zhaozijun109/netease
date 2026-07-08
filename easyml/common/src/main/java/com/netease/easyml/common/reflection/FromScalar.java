package com.netease.easyml.common.reflection;

import numpy.core.Scalar;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by linjiuning on 2020/8/5.
 */
public class FromScalar extends FromValue {
    @Override
    public boolean isMatch(Field field, Object value) {
        return value instanceof Scalar;
    }

    @Override
    public Object fromValue(Field field, Object value) throws Exception {
        Scalar ndarray = (Scalar) value;
        List<?> content = ndarray.getContent();
        return content.get(0);
    }
}
