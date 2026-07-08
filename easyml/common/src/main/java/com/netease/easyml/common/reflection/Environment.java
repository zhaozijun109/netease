package com.netease.easyml.common.reflection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by linjiuning on 2020/8/4.
 */
public class Environment {
    private List<FromValue> fromValues = new ArrayList<>();
    private FromValue entry;

    public Environment add(FromValue fromValue) {
        fromValues.add(fromValue);
        fromValue.setEnv(this);
        return this;
    }

    public Environment setEntry(FromValue entry) {
        this.entry = entry;
        return this;
    }

    public List<FromValue> getFromValues() {
        return fromValues;
    }

    public FromValue get(Field field, Object value) {
        FromValue fromValue = null;
        for (FromValue fVal : fromValues) {
            if (fVal.isMatch(field, value)) {
                fromValue = fVal;
                break;
            }
        }
        return fromValue;
    }

    public Object fromValue(Object object) throws Exception {
        if (entry == null) {
            entry = get(null, object);
        }
        return entry.fromValue((Field) null, object);
    }

    public void fromValue(Object instance, Map<String, Object> params) throws Exception {
        if (entry == null) {
            entry = get(null, params);
        }
        entry.fromValue(instance, params);
    }
}
