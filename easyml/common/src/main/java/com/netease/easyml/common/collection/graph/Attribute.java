package com.netease.easyml.common.collection.graph;

import com.netease.easyml.common.util.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Created by linjiuning on 2018/12/17.
 */
public class Attribute extends HashMap<Object, Object> {
    private static final Logger log = LoggerFactory.getLogger(Attribute.class);

    protected void putUnique(Object key, Object value) {
        if (this.containsKey(key))
            log.warn("Key: " + key + " already exist, ignore anyway");
        else
            this.put(key, value);
    }

    @Override
    public String toString() {
        String json = JacksonUtil.beanToJson(this);
        if (json == null) {
            json = "";
        }
        return json;
    }
}
