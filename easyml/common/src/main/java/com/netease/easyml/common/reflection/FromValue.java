package com.netease.easyml.common.reflection;

import com.google.common.base.CaseFormat;
import com.netease.easyml.annotation.Alias;
import com.netease.easyml.annotation.Ignore;
import com.netease.easyml.common.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by linjiuning on 2020/8/4.
 */
public class FromValue {
    private final Logger log = LoggerFactory.getLogger(FromValue.class);
    protected Environment env;

    public FromValue setEnv(Environment env) {
        this.env = env;
        return this;
    }

    public boolean isMatch(Field field, Object value) {
        return true;
    }

    public Object fromValue(Field field, Object value) throws Exception {
        return value;
    }

    public Object newInstance(Class<?> clazz) throws Exception {
        return clazz.newInstance();
    }

    public void set(Object instance, Field field, Object value) throws Exception {
        field.setAccessible(true);
        field.set(instance, value);
    }

    public void fromValue(Object instance, Map<String, Object> params) throws Exception {
        for (Field field : ReflectionUtil.getFields(instance.getClass())) {
            Ignore ignore = field.getAnnotation(Ignore.class);
            if (ignore != null) {
                continue;
            }
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
                } else {
                    FromValue fromValue = env.get(field, value);
                    if (fromValue != null) {
                        value = fromValue.fromValue(field, value);
                        fromValue.set(instance, field, value);
                    } else {
                        set(instance, field, value);
                    }
                }
            }
        }
    }

    public Object fromValue(Class<?> clazz, Object o) throws Exception {
        if (o != null && !clazz.isAssignableFrom(o.getClass()) && o instanceof Map) {
            Object instance = newInstance(clazz);
            fromValue(instance, (Map) o);
            o = instance;
        }
        return o;
    }
}
