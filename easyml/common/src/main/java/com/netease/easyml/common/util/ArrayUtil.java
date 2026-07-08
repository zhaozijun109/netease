package com.netease.easyml.common.util;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * Created by linjiuning on 2020/7/8.
 */
public class ArrayUtil {

    public static boolean isArray(Object objs) {
        return isArray(objs.getClass());
    }

    public static boolean isArray(Class<?> cls) {
        return cls.getName().startsWith("[");
    }

    public static boolean isNDArray(Object array) {
        return isArray(array) || array instanceof List;
    }

    public static boolean isNDArray(Class<?> cls) {
        return isArray(cls) || List.class.isAssignableFrom(cls);
    }

    public static Iterator<Object> iterator(Object value) {
        if (value instanceof Collection) {
            return ((Collection) value).iterator();
        } else {
            final int[] i = {0};
            return new Iterator<Object>() {

                @Override
                public boolean hasNext() {
                    return i[0] < ArrayUtil.size0(value);
                }

                @Override
                public Object next() {
                    return ArrayUtil.get(value, i[0]++);
                }
            };
        }
    }

    public static Object get(Object objs, int index) {
        if (objs instanceof List) {
            List cobjs = (List) objs;
            return cobjs.get(index);
        } else {
            return Array.get(objs, index);
        }
    }

    public static <T> T get(Object objs, int index, Class<T> clazz) {
        Object o = get(objs, index);
        return clazz.cast(o);
    }

    public static void set(Object objs, int index, Object value) {
        if (objs instanceof List) {
            List cobjs = (List) objs;
            cobjs.set(index, value);
        } else {
            Array.set(objs, index, value);
        }
    }

    public static Object get(Object objs, int[] indices) {
        for (int index : indices) {
            objs = get(objs, index);
        }
        return objs;
    }

    public static Object get(Object objs, long[] indices) {
        for (long index : indices) {
            objs = get(objs, (int) index);
        }
        return objs;
    }

    public static <T> T get(Object objs, int[] indices, Class<T> clazz) {
        Object o = get(objs, indices);
        return clazz.cast(o);
    }

    public static <T> T get(Object objs, long[] indices, Class<T> clazz) {
        Object o = get(objs, indices);
        return clazz.cast(o);
    }

    public static void set(Object objs, int[] indices, Object value) {
        for (int i = 0; i < indices.length - 1; i++) {
            objs = get(objs, indices[i]);
        }
        set(objs, indices[indices.length - 1], value);
    }

    public static void set(Object objs, long[] indices, Object value) {
        for (int i = 0; i < indices.length - 1; i++) {
            objs = get(objs, (int) indices[i]);
        }
        set(objs, (int) indices[indices.length - 1], value);
    }

    public static int dim(Object objs) {
        if (objs instanceof List) {
            List cobjs = (List) objs;
            return cobjs.isEmpty() ? 1 : 1 + dim(get(cobjs, 0));
        } else {
            String name = objs.getClass().getName();
            int d = 0;
            while (d < name.length() && name.charAt(d) == '[') {
                d++;
            }
            return d;
        }
    }

    public static int size0(Object objs) {
        if (objs instanceof List) {
            List cobjs = (List) objs;
            return cobjs.size();
        } else {
            return Array.getLength(objs);
        }
    }

    public static int size(Object objs) {
        int[] shape = shape(objs);
        if (shape.length < 1) {
            return 0;
        }
        int sz = 1;
        for (int i : shape) {
            sz *= i;
        }
        return sz;
    }

    public static boolean isEmpty(Object objs) {
        return size(objs) == 0;
    }

    public static Class<?> componentType(Class<?> clazz) {
        while (clazz.isArray()) {
            clazz = clazz.getComponentType();
        }
        return clazz;
    }

    public static Class<?> componentType(Object o) {
        Class<?> c = o.getClass();
        while (c.isArray()) {
            c = c.getComponentType();
        }
        if (Object.class.isAssignableFrom(c)) {
            while (isNDArray(o) && !isEmpty(o)) {
                o = get(o, 0);
            }
            c = o.getClass();
            if (c == Byte.class) {
                return byte.class;
            } else if (c == Short.class) {
                return short.class;
            } else if (c == Integer.class) {
                return int.class;
            } else if (c == Long.class) {
                return long.class;
            } else if (c == Float.class) {
                return float.class;
            } else if (c == Double.class) {
                return double.class;
            }
        }
        return c;
    }

    public static int[] shape(Object array) {
        int dim = dim(array);
        int[] shape = new int[dim];
        for (int i = 0; i < dim; i++) {
            int sz = size0(array);
            shape[i] = sz;
            if (sz == 0) {
                break;
            }
            array = get(array, 0);
        }
        return shape;
    }

    public static long[] shapeAsLong(Object array) {
        int dim = dim(array);
        long[] shape = new long[dim];
        for (int i = 0; i < dim; i++) {
            shape[i] = size0(array);
            array = get(array, 0);
        }
        return shape;
    }

    public static Object zeros(Class<?> componentType, int length) {
        return Array.newInstance(componentType, length);
    }

    public static Object zeros(Class<?> componentType, int[] dimensions) {
        return Array.newInstance(componentType, dimensions);
    }

    public static Object zeros(Class<?> componentType, long length) {
        return Array.newInstance(componentType, (int) length);
    }

    public static Object zeros(Class<?> componentType, long[] dimensions) {
        int[] shape = new int[dimensions.length];
        for (int i = 0; i < dimensions.length; i++) {
            shape[i] = (int) dimensions[i];
        }
        return Array.newInstance(componentType, shape);
    }

    public static Object zerosLike(Object array) {
        Class<?> dtype = componentType(array);
        int[] shape = shape(array);
        return Array.newInstance(dtype, shape);
    }

    public static Object clone(Object array) {
        Object tgt = zerosLike(array);
        map(array, tgt, (e) -> {
            if (e instanceof Cloneable) {
                try {
                    Method clone = e.getClass().getDeclaredMethod("clone");
                    e = clone.invoke(e);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
                }
            }
            return e;
        });
        return tgt;
    }

    public static void fill(Object array, Object value) {
        map(array, array, (it) -> value);
    }

    public static Object flatten(Object array) {
        if (!isArray(array)) {
            return array;
        }
        int dim = dim(array);
        if (dim == 1) {
            return array;
        }

        int size = size(array);
        Object flatArray = zeros(componentType(array), size);
        flatten(flatArray, 0, array);
        return flatArray;
    }

    private static void flatten(Object flatArray, int offset, Object array) {
        if (!isArray(array)) {
            set(flatArray, offset, array);
            return;
        }
        int size0 = size0(array);
        int step = size(array) / size0;
        for (int i = 0; i < size0; i++) {
            flatten(flatArray, offset, get(array, i));
            offset += step;
        }
    }

    public static Object reshape1D(Object flatArray, int[] shape) {
        if (shape.length == 1) {
            return flatArray;
        }
        Object newArray = zeros(componentType(flatArray), shape);
        reshape1D(newArray, 0, flatArray);
        return newArray;
    }

    private static void reshape1D(Object newArray, int offset, Object flatArray) {
        int dim = dim(newArray);
        int size0 = size0(newArray);
        if (dim == 1) {
            for (int i = 0; i < size0; i++) {
                Object o = get(flatArray, offset);
                set(newArray, i, o);
                offset++;
            }
            return;
        }

        int step = size(newArray) / size0;
        for (int i = 0; i < size0; i++) {
            reshape1D(get(newArray, i), offset, flatArray);
            offset += step;
        }
    }

    public static <T> T concat(T... arrays) {
        Class<?> componentType = arrays[0].getClass().getComponentType();
        int newLength = 0;
        for (T array : arrays) {
            newLength += size0(array);
        }

        Object newArray = zeros(componentType, newLength);
        int i = 0;
        for (T array : arrays) {
            int j = 0;
            while (j < size0(array)) {
                set(newArray, i, get(array, j));
                i++;
                j++;
            }
        }
        return (T) newArray;
    }

    public static <T> T stack(T... arrays) {
        Class<?> componentType = arrays[0].getClass();
        int length = arrays.length;

        Object newArray = zeros(componentType, length);
        for (int i = 0; i < length; i++) {
            set(newArray, i, arrays[i]);
        }
        return (T) newArray;
    }

    public static <T, R> void map(Object srcArray, Object dstArray, Function<T, R> function) {
        // TODO: shape validation
        if (isNDArray(srcArray)) {
            int length = size0(srcArray);
            if (dim(srcArray) == 1) {
                for (int i = 0; i < length; i++) {
                    T srcValue = (T) get(srcArray, i);
                    R dstValue = function.apply(srcValue);
                    set(dstArray, i, dstValue);
                }
            } else {
                for (int i = 0; i < length; i++) {
                    map(get(srcArray, i), get(dstArray, i), function);
                }
            }
        }
    }

    public static <T> void map(Object srcArray, Function<T, T> function) {
        map(srcArray, srcArray, function);
    }

    public static String toString(Object array) {
        return JSON.toJSONString(array);
    }

    public static Object toArray(Object array) {
        if (isArray(array)) {
            return array;
        }
        return clone(array);
    }
}
