package com.netease.easyml.common.reflection;

import org.apache.spark.ml.param.Param;
import org.apache.spark.ml.param.Params;

import java.lang.reflect.Field;

/**
 * Created by linjiuning on 2020/8/5.
 */
public class FromObjectToMLParam extends FromValue {
    @Override
    public boolean isMatch(Field field, Object value) {
        return Param.class.isAssignableFrom(field.getType());
    }

    @Override
    public void set(Object instance, Field field, Object value) throws Exception {
        field.setAccessible(true);
        ((Params) instance).set((Param) field.get(instance), value);
    }
}
