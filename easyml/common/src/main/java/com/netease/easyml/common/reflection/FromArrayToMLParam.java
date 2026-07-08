package com.netease.easyml.common.reflection;

import com.netease.easyml.common.util.ArrayUtil;
import org.apache.spark.ml.param.Param;
import org.apache.spark.ml.param.Params;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static com.netease.easyml.common.util.ArrayUtil.componentType;
import static com.netease.easyml.common.util.ArrayUtil.isArray;

/**
 * Created by linjiuning on 2020/8/5.
 */
public class FromArrayToMLParam extends FromValue {
    @Override
    public boolean isMatch(Field field, Object value) {
        return Param.class.isAssignableFrom(field.getType()) && isArray(value);
    }

    @Override
    public Object fromValue(Field field, Object value) throws Exception {
        if (value != null) {
            Class<?> ctype;
            Class<?> actualTypeArgument;
            if (isArray(value)) {
                actualTypeArgument = componentType(value);
                ctype = actualTypeArgument;
            } else {
                Type gType = field.getGenericType();

                ParameterizedType genericType = (ParameterizedType) gType;
                Type type = genericType.getActualTypeArguments()[0];

                ctype = ((Class<?>) type).getComponentType();

                actualTypeArgument = ctype.getComponentType();
            }

            int size = ArrayUtil.size(value);
            Object array = ArrayUtil.zeros(ctype, size);
            for (int i = 0; i < size; i++) {
                Object o = ArrayUtil.get(value, i);
                o = fromValue(actualTypeArgument, o);
                ArrayUtil.set(array, i, o);
            }
            value = array;
        }
        return value;
    }

    @Override
    public void set(Object instance, Field field, Object value) throws Exception {
        field.setAccessible(true);
        ((Params) instance).set((Param) field.get(instance), value);
    }
}
