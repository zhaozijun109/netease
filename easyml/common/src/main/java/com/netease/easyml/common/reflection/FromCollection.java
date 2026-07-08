package com.netease.easyml.common.reflection;

import com.netease.easyml.common.util.ArrayUtil;
import numpy.core.NDArray;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * Created by linjiuning on 2020/8/5.
 */
public class FromCollection extends FromValue {
    @Override
    public boolean isMatch(Field field, Object value) {
        Class<?> declaringClass = field.getType();
//        return Collection.class.isAssignableFrom(declaringClass) && value instanceof Collection;
        return (Collection.class.isAssignableFrom(declaringClass) || ArrayUtil.isNDArray(declaringClass)) &&
                (value instanceof Collection || ArrayUtil.isNDArray(value));
    }

    public Object newInstance(Class<?> clazz, Object value) throws IllegalAccessException, InstantiationException {
        int size;
        if (value instanceof Collection) {
            size = ((Collection) value).size();
        } else {
            size = ArrayUtil.size0(value);
        }
        Object collection;
        if (List.class.isAssignableFrom(clazz)) {
            collection = new ArrayList();
        } else if (Set.class.isAssignableFrom(clazz)) {
            collection = new HashSet();
        } else if (ArrayUtil.isArray(clazz)) {
            Class<?> componentType = ArrayUtil.componentType(clazz);
            collection = ArrayUtil.zeros(componentType, size);
        } else {
            collection = clazz.newInstance();
        }
        return collection;
    }

    public Class<?> componentType(Field field) {
        Class<?> declaringClass = field.getType();
        if (ArrayUtil.isArray(declaringClass)) {
            return ArrayUtil.componentType(declaringClass);
        } else {
            ParameterizedType genericType = (ParameterizedType) field.getGenericType();
            return (Class<?>) genericType.getActualTypeArguments()[0];
        }
    }

    public void add(Object collection, int i, Object value) {
        if (collection instanceof Collection) {
            ((Collection) collection).add(value);
        } else {
            ArrayUtil.set(collection, i, value);
        }
    }

    public Iterator<Object> iterator(Object value) {
        return ArrayUtil.iterator(value);
    }

    @Override
    public Object fromValue(Field field, Object value) throws Exception {
        if (value != null) {
            Class<?> clazz = field.getType();
            Class<?> componentType = componentType(field);

            Object collection = newInstance(clazz, value);
            Iterator<Object> iterator = iterator(value);
            int i = 0;
            while (iterator.hasNext()) {
                Object o = iterator.next();
                if (o instanceof NDArray) {
                    o = FromNDArray.toArray((NDArray) o);
                } else {
                    o = fromValue(componentType, o);
                }
                add(collection, i++, o);
            }
            value = collection;
        }
        return value;
    }
}
