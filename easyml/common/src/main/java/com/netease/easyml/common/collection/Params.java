package com.netease.easyml.common.collection;

import com.netease.easyml.common.util.CollectionUtil;
import com.netease.easyml.common.util.IOUtil;
import com.netease.easyml.common.util.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by linjiuning on 2020/7/6.
 */
public class Params {
    private static final Logger log = LoggerFactory.getLogger(Params.class);
    private Map<String, Object> map;

    public Map<String, Object> getMap() {
        return map;
    }

    public Map<String, String> getStringMap() {
        Map<String, String> res = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            res.put(entry.getKey(), entry.getValue().toString());
        }
        return res;
    }

    public Params() {
        this(new HashMap<>());
    }

    public Params(Map<String, Object> map) {
        this.map = map;
    }

    public Object put(String key, Object value) {
        return map.put(key, value);
    }

    public void putAll(Map<String, Object> m) {
        map.putAll(m);
    }

    public void putAll(Params m) {
        map.putAll(m.map);
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    public Object remove(String key) {
        return map.remove(key);
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public Collection<Object> values() {
        return map.values();
    }

    public Object get(String key) {
        return map.getOrDefault(key, null);
    }

    public <T> T get(String key, Class<T> clazz) {
        if (clazz.equals(Params.class)) {
            if (map.containsKey(key) && Params.class.isAssignableFrom(map.get(key).getClass())) {
                return (T) map.get(key);
            }
            Map val = (Map) map.getOrDefault(key, new HashMap<>());
            return (T) new Params(val);
        }
        return clazz.cast(map.getOrDefault(key, null));
    }

    public <T> T get(String key, T obj) {
        if (containsKey(key)) {
            return (T) get(key, obj.getClass());
        }
        return obj;
    }

    public Set<Map.Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> newMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Params) {
                value = ((Params) value).toMap();
            }
            newMap.put(entry.getKey(), value);
        }
        return newMap;
    }

    @Override
    public String toString() {
        return toJson();
    }

    public String toJson(boolean prettyFormat) {
        return JacksonUtil.beanToJson(map, prettyFormat);
    }

    public String toJson() {
        return toJson(false);
    }

    public Params duplicate() {
        return fromJson(toJson());
    }

    public static Params fromJson(String json) {
        Map map = JacksonUtil.jsonToBean(json, Map.class);
        map = (Map) CollectionUtil.jacksonToJava(map);
        return new Params(map);
    }

    public static Params fromFile(String path) {
        StringBuilder sb = new StringBuilder();
        InputStream stream;
        if (IOUtil.exists(path)) {
            stream = IOUtil.getInputStream(path);
        } else {
            stream = IOUtil.getResourceAsStream(path);
        }
        if (stream == null) {
            log.error(String.format("Can't read %s from local/hdfs/classpath.", path));
        }
        for (String line : IOUtil.readLines(stream)) {
            sb.append(line);
        }
        String json = sb.toString();
        return fromJson(json);
    }
}
