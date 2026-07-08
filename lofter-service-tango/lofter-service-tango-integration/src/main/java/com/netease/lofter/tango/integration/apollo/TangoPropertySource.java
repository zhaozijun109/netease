package com.netease.lofter.tango.integration.apollo;

import com.netease.lofter.tango.integration.consts.TangoIntegrationConsts;
import org.springframework.core.env.EnumerablePropertySource;

import java.util.Map;
import java.util.Set;

public class TangoPropertySource extends EnumerablePropertySource<Map<String, String>> {

    private static final String[] EMPTY_ARRAY = new String[0];

    TangoPropertySource(Map<String, String> source) {
        super(TangoIntegrationConsts.TANGO_PROPERTY_SOURCES, source);
    }

    @Override
    public boolean containsProperty(String name) {
        return this.source.containsKey(name);
    }

    @Override
    public String[] getPropertyNames() {
        Set<String> propertyNames = this.source.keySet();
        if (propertyNames.isEmpty()) {
            return EMPTY_ARRAY;
        }
        return propertyNames.toArray(new String[0]);
    }

    @Override
    public Object getProperty(String name) {
        return this.source.getOrDefault(name, null);
    }

    public void removeProperty(String name) {
        this.source.remove(name);
    }

    public void putProperty(String name, String value) {
        this.source.put(name, value);
    }

}
