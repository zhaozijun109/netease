package com.netease.easyml.common.reflection;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by linjiuning on 2020/8/5.
 */
public class FromMap extends FromValue {
    @Override
    public boolean isMatch(Field field, Object value) {
        Class<?> declaringClass = field.getType();
        return Map.class.isAssignableFrom(declaringClass) && value instanceof Map;
    }

    @Override
    public Object fromValue(Field field, Object value) throws Exception {
        Class<?> declaringClass = field.getType();
        if (value != null) {
            if (Map.class.isAssignableFrom(declaringClass)) {
                ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                Type type = genericType.getActualTypeArguments()[1];
                Class<?> actualTypeArgument = ((ParameterizedTypeImpl) type).getRawType();
                Map map = new HashMap();
                for (Object k : ((Map) value).keySet()) {
                    Object o = ((Map) value).get(k);
                    o = fromValue(actualTypeArgument, o);
                    map.put(k, o);
                }
                value = map;
            }
        }
        return value;
    }
}
