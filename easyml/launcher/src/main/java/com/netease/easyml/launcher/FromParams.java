package com.netease.easyml.launcher;


import com.google.common.base.CaseFormat;
import com.netease.easyml.annotation.Alias;
import com.netease.easyml.common.util.ArrayUtil;
import com.netease.easyml.common.util.CollectionUtil;
import com.netease.easyml.ml.util.MLUtils;
import org.apache.spark.ml.param.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linjiuning on 2020/7/6.
 */
public class FromParams {
    private static final Logger log = LoggerFactory.getLogger(FromParams.class);
    private static final String TYPE = "type";
    private static final String PATH = "@path";
    private static Map<Object, Map<String, Class<? extends Params>>> REGISTRY = new HashMap<>();
    private static Map<Object, String> DEFAULT = new HashMap<>();

    public static void register(String name, Object parent, Class<? extends Params> clazz) {
        register(name, parent, clazz, false, false);
    }

    public static void register(String name, Object parent, Class<? extends Params> clazz, boolean existOk, boolean isDefault) {
        REGISTRY.putIfAbsent(parent, new HashMap<>());
        Map<String, Class<? extends Params>> map = REGISTRY.get(parent);
        if (map.containsKey(name)) {
            if (!existOk) {
                log.warn(String.format("Cannot register %s as %s; " +
                        "name already in use for %s", name, clazz.getCanonicalName(), map.get(name).getCanonicalName()));
                return;
            } else {
                log.info(String.format("Register %s as %s; " +
                        "name already in use for %s, overwrite anyway.", name, clazz.getCanonicalName(), map.get(name).getCanonicalName()));
            }
        }
        map.put(name, clazz);
        if (isDefault) {
            if (DEFAULT.containsKey(parent)) {
                log.warn(String.format("Cannot set default implement of %s as %s; " +
                        "name already in use", parent.getClass().getSimpleName(), name));

            }
        }
    }

    public static <T extends Params> Class<T> byName(Class<T> parent, String name) {
        Class<T> clazz = null;
        if (REGISTRY.containsKey(parent) && REGISTRY.get(parent).containsKey(name)) {
            clazz = (Class<T>) REGISTRY.get(parent).get(name);
        }
        if (clazz == null) {
            log.error("parent: {}, name: {}, is not registered.", parent, name);
        }
        return clazz;
    }

    private static List<Field> getFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())
                    || !Param.class.isAssignableFrom(field.getType())) {
                continue;
            }
            fields.add(field);
        }
        Class<?> superclass = clazz.getSuperclass();
        if (Params.class.isAssignableFrom(superclass)) {
            List<Field> superFields = getFields(superclass);
            fields.addAll(superFields);
        }

        return fields;
    }

    private static boolean isArray(Class<?> clazz) {
        return clazz.getName().startsWith("[");
    }

    public static <T extends Params> T fromParams(Class<T> clazz, com.netease.easyml.common.collection.Params params) throws IllegalAccessException {
        if (params.containsKey(TYPE)) {
            String name = params.get(TYPE, String.class);
            params.remove(TYPE);
            Class<T> clazz_ = byName(clazz, name);
            if (clazz_ == null) {
                clazz_ = clazz;
            }
            return fromParams(clazz_, params);
        }

        try {
            T instance;
            if (params.containsKey(PATH) && Archive.isReadable(clazz)) {
                String path = params.get(PATH, String.class);
                params.remove(PATH);
                instance = MLUtils.read(clazz, path);
            } else {
                instance = clazz.newInstance();
            }
            List<Field> fields = getFields(clazz);
            for (Field field : fields) {
                String name = field.getName();
                if (!params.containsKey(name)) {
                    Alias alias = field.getAnnotation(Alias.class);
                    if (alias != null) {
                        for (String s : alias.name()) {
                            if (params.containsKey(s)) {
                                name = s;
                                break;
                            }
                        }
                    }
                }
                if (!params.containsKey(name)) {
                    name = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
                }
                if (params.containsKey(name)) {
                    Object value = params.get(name);
                    if (value == null) {
                        log.warn("Null value: {} = {}", name, value);
                    }

                    if (value != null) {
                        Type gType = field.getGenericType();
                        if (gType instanceof ParameterizedType) {
                            ParameterizedType genericType = (ParameterizedType) gType;
                            Type type = genericType.getActualTypeArguments()[0];
                            if (type instanceof ParameterizedTypeImpl) {
                                ParameterizedTypeImpl ctype = (ParameterizedTypeImpl) type;
                                Class<?> rawType = ctype.getRawType();
                                if (scala.collection.Map.class.isAssignableFrom(rawType)) {
                                    Class<?> actualTypeArgument = (Class<?>) ctype.getActualTypeArguments()[1];
                                    if (Params.class.isAssignableFrom(actualTypeArgument)) {
                                        Map map = new HashMap();
                                        for (Object k : ((Map) value).keySet()) {
                                            Object o = ((Map) value).get(k);
                                            o = fromObjects(actualTypeArgument, o);
                                            map.put(k, o);
                                        }
                                        value = map;
                                    }
                                    value = CollectionUtil.toScalaMap((Map) value);
                                } else if (scala.collection.Iterable.class.isAssignableFrom(rawType)) {
                                    Class<?> actualTypeArgument = (Class<?>) ctype.getActualTypeArguments()[0];
                                    boolean fromObj = Params.class.isAssignableFrom(actualTypeArgument);
                                    throw new IllegalArgumentException("Iterable params is not support yet.");
                                }
                            } else {
                                Class<?> ctype = (Class<?>) type;
                                if (isArray(ctype)) {
                                    Class<?> actualTypeArgument = ctype.getComponentType();
                                    if (Params.class.isAssignableFrom(actualTypeArgument)) {
                                        int size = ArrayUtil.size(value);
                                        Object array = ArrayUtil.zeros(ctype.getComponentType(), size);
                                        for (int i = 0; i < size; i++) {
                                            Object o = ArrayUtil.get(value, i);
                                            o = fromObjects(actualTypeArgument, o);
                                            ArrayUtil.set(array, i, o);
                                        }
                                        value = array;
                                    }

                                } else if (Params.class.isAssignableFrom(ctype)) {
                                    value = fromObjects(ctype, value);
                                }
                            }
                        } else if (field.getType() == IntArrayParam.class) {
                            int size = ArrayUtil.size(value);
                            Object array = ArrayUtil.zeros(int.class, size);
                            for (int i = 0; i < size; i++) {
                                Object o = ArrayUtil.get(value, i);
                                ArrayUtil.set(array, i, o);
                            }
                            value = array;
                        } else if (field.getType() == IntParam.class) {
                            value = Integer.parseInt(value.toString());
                        } else if (field.getType() == LongParam.class) {
                            value = Long.parseLong(value.toString());
                        } else if (field.getType() == DoubleParam.class) {
                            value = Double.parseDouble(value.toString());
                        } else if (field.getType() == FloatParam.class) {
                            value = Float.parseFloat(value.toString());
                        }
                    }
                    field.setAccessible(true);
                    instance.set((Param<? super Object>) field.get(instance), value);
                }
            }
            return instance;
        } catch (InstantiationException e) {
            log.error("InstantiationException: {}", clazz);
        }
        return null;
    }

    private static Object fromObjects(Class<?> actualTypeArgument, Object o) throws IllegalAccessException {
        if (o != null && !actualTypeArgument.isAssignableFrom(o.getClass())) {
            Map<String, Object> map = new HashMap<>();
            if (o instanceof String) {
                map.put(TYPE, o);
            } else {
                map = (Map) o;
            }
            com.netease.easyml.common.collection.Params p = new com.netease.easyml.common.collection.Params(map);
            o = fromParams((Class<Params>) actualTypeArgument, p);
        }
        return o;
    }
}
