package com.netease.easyudf.udf.collect;

import com.alibaba.fastjson.JSON;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StandardListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.StringObjectInspector;

import java.util.HashMap;
import java.util.Map;

public class KVJsonUDF extends GenericUDF {

    private transient StandardListObjectInspector input0;
    private transient StandardListObjectInspector input1;

    private transient StringObjectInspector retValInspector;

    @Override
    public ObjectInspector initialize(ObjectInspector[] argOIs) throws UDFArgumentException {
        input0 = (StandardListObjectInspector) argOIs[0];
        input1 = (StandardListObjectInspector) argOIs[1];

        retValInspector = PrimitiveObjectInspectorFactory.javaStringObjectInspector;
        return retValInspector;
    }

    @Override
    public Object evaluate(DeferredObject[] args) throws HiveException {
        int length = input0.getListLength(args[0].get());
        Map<Object, Object> map = new HashMap<>();
        for (int i = 0; i < length; i++) {
            Object key = input0.getListElement(args[0].get(), i);
            Object value = input1.getListElement(args[1].get(), i);
            map.put(key, value);
        }
        return JSON.toJSONString(map);
    }

    @Override
    public String getDisplayString(String[] args) {
        return "kv_json(" + String.join(", ", args) + " )";
    }
}
