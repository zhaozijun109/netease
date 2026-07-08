package com.netease.easyml.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linjiuning on 2020/8/4.
 */
public class ReflectionUtil {
    private static final Logger log = LoggerFactory.getLogger(ReflectionUtil.class);

    public static ClassLoader getEasyMLClassLoader() {
        return ReflectionUtil.class.getClassLoader();
    }

    public static ClassLoader getContextOrEasyMLClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = getEasyMLClassLoader();
        }
        return classLoader;
    }

    /**
     * Preferred alternative to Class.forName(className)
     */
    public static Class<?> classForName(String className) throws ClassNotFoundException {
        return Class.forName(className, true, getContextOrEasyMLClassLoader());
    }

    public static List<Field> getFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        if (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                fields.add(field);
            }
            Class<?> superclass = clazz.getSuperclass();

            List<Field> superFields = getFields(superclass);
            fields.addAll(superFields);
        }
        return fields;
    }

    public static Object getScalaObject(String objName) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Field field = classForName(objName + "$").getField("MODULE$");
        field.setAccessible(true);
        return field.get(null);
    }
}
