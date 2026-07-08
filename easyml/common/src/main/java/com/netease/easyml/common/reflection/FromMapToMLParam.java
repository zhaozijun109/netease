package com.netease.easyml.common.reflection;

import com.netease.easyml.common.util.CollectionUtil;
import org.apache.spark.ml.param.Param;
import org.apache.spark.ml.param.Params;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by linjiuning on 2020/8/5.
 */
public class FromMapToMLParam extends FromValue {
    @Override
    public boolean isMatch(Field field, Object value) {
        if (Param.class.isAssignableFrom(field.getType()) && value instanceof Map) {
            Type gType = field.getGenericType();
            if (gType instanceof ParameterizedType) {
                ParameterizedType genericType = (ParameterizedType) gType;
                Type type = genericType.getActualTypeArguments()[0];
                if (type instanceof ParameterizedTypeImpl) {
                    ParameterizedTypeImpl ctype = (ParameterizedTypeImpl) type;
                    Class<?> rawType = ctype.getRawType();
                    return scala.collection.Map.class.isAssignableFrom(rawType);
                }
            }
        }
        return false;
    }

    @Override
    public Object fromValue(Field field, Object value) throws Exception {
        if (value != null) {
            Type gType = field.getGenericType();
            ParameterizedType genericType = (ParameterizedType) gType;
            Type type = genericType.getActualTypeArguments()[0];

            ParameterizedTypeImpl ctype = (ParameterizedTypeImpl) type;
            Class<?> actualTypeArgument = (Class<?>) ctype.getActualTypeArguments()[1];

            Map map = new HashMap();
            for (Object k : ((Map) value).keySet()) {
                Object o = ((Map) value).get(k);
                o = fromValue(actualTypeArgument, o);
                map.put(k, o);
            }
            value = map;
            value = CollectionUtil.toScalaMap((Map) value);
        }
        return value;
    }

    @Override
    public void set(Object instance, Field field, Object value) throws Exception {
        field.setAccessible(true);
        ((Params) instance).set((Param) field.get(instance), value);
    }
}
