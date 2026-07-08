package com.netease.easyml.common.util.lucene;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netease.easyml.common.util.IOUtil;
import com.netease.easyml.common.util.LuceneUtil;
import com.netease.easyml.common.util.StringUtil;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linjiuning on 2019/8/28.
 */
public class LuceneConfig {
    private static final String ID = "id";
    private static final String FIELDS = "fields";
    private static final String TYPE = "type";
    private static final String STORE = "store";
    private static final String SORT = "sort";

    public enum Type {
        TEXT,
        KEYWORD,
        DOUBLE,
        FLOAT,
        INTEGER
    }

    public static class Entry {
        private String name;
        private Type type = Type.KEYWORD;
        private boolean store = true;
        private boolean sort = false;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public boolean isStore() {
            return store;
        }

        public void setStore(boolean store) {
            this.store = store;
        }

        public boolean isSort() {
            return sort;
        }

        public void setSort(boolean sort) {
            this.sort = sort;
        }

        public Entry(String name) {
            this.name = name;
        }

        public Entry(String name, Type type, boolean store) {
            this.name = name;
            this.type = type;
            this.store = store;
        }

        public Entry(String name, Type type, boolean store, boolean sort) {
            this.name = name;
            this.type = type;
            this.store = store;
            this.sort = sort;
        }
    }

    private Map<String, Entry> configs;
    private String id;

    public LuceneConfig(Map<String, Entry> configs) {
        this.configs = configs;
    }

    public LuceneConfig(String id, Map<String, Entry> configs) {
        this.id = id;
        this.configs = configs;
    }

    public String getId() {
        return id;
    }

    public Document document(Map<String, String> values) {
        Document doc = new Document();

        for (Map.Entry<String, Entry> entry : configs.entrySet()) {
            String key = entry.getKey();
            Entry value = entry.getValue();

            String val = values.getOrDefault(key, "");

            Field.Store store;
            if (value.isStore())
                store = Field.Store.YES;
            else
                store = Field.Store.NO;

            switch (value.type) {
                case KEYWORD: {
                    doc.add(new StringField(key, val, store));
                    break;
                }
                case TEXT: {
                    doc.add(new TextField(key, val, store));
                    break;
                }
                case INTEGER: {
                    int pVal = Integer.parseInt(val);
                    List<Field> fields = LuceneUtil.intPoint(key, pVal, value.isSort(), value.isStore());
                    for (Field field : fields) {
                        doc.add(field);
                    }
                    break;
                }
                case DOUBLE: {
                    double pVal = Double.parseDouble(val);
                    List<Field> fields = LuceneUtil.doublePoint(key, pVal, value.isSort(), value.isStore());
                    for (Field field : fields) {
                        doc.add(field);
                    }
                    break;
                }
                case FLOAT: {
                    float pVal = Float.parseFloat(val);
                    List<Field> fields = LuceneUtil.floatPoint(key, pVal, value.isSort(), value.isStore());
                    for (Field field : fields) {
                        doc.add(field);
                    }
                    break;
                }
            }
        }
        return doc;
    }

    public static LuceneConfig create(String path) {
        InputStream inputStream = IOUtil.getInputStream(path);
        return create(inputStream);
    }

    public static LuceneConfig create(InputStream stream) {
        String jstr = StringUtil.join(IOUtil.readLines(stream), "");
        JSONObject jsonObject = JSON.parseObject(jstr);
        String id = "";
        if (jsonObject.containsKey(ID))
            id = jsonObject.getString(ID);

        JSONObject fields = jsonObject.getJSONObject(FIELDS);
        Map<String, Entry> configs = new HashMap<>();

        for (String key : fields.keySet()) {
            Entry entry = new Entry(key);

            JSONObject obj = fields.getJSONObject(key);
            String stype = obj.getString(TYPE);
            Type type = Type.valueOf(stype.toUpperCase());
            entry.setType(type);

            if (obj.containsKey(STORE)) {
                boolean store = obj.getBoolean(STORE);
                entry.setStore(store);
            }

            if (obj.containsKey(SORT)) {
                boolean sort = obj.getBoolean(SORT);
                entry.setSort(sort);
            }

            configs.put(key, entry);
        }
        return new LuceneConfig(id, configs);
    }
}
