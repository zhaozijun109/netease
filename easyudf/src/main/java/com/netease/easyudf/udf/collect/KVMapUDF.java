package com.netease.easyudf.udf.collect;

import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.*;

import java.util.HashMap;
import java.util.Map;

public class KVMapUDF extends GenericUDF {

    private transient StandardListObjectInspector input0;
    private transient StandardListObjectInspector input1;

    private transient StandardMapObjectInspector retValInspector;

    @Override
    public ObjectInspector initialize(ObjectInspector[] argOIs) throws UDFArgumentException {
        input0 = (StandardListObjectInspector) argOIs[0];
        input1 = (StandardListObjectInspector) argOIs[1];

        retValInspector = ObjectInspectorFactory.getStandardMapObjectInspector(
                input0.getListElementObjectInspector(),
                input1.getListElementObjectInspector());
        return retValInspector;
    }

    @Override
    public Object evaluate(DeferredObject[] args) throws HiveException {
        int length = input0.getListLength(args[0].get());

        Map retVal = (Map) retValInspector.create();
        for (int i = 0; i < length; i++) {
            Object key = input0.getListElement(args[0].get(), i);
            Object value = input1.getListElement(args[1].get(), i);
            Object keyStd = ObjectInspectorUtils.copyToStandardObject(key, retValInspector.getMapKeyObjectInspector());
            Object valStd = ObjectInspectorUtils.copyToStandardObject(value, retValInspector.getMapValueObjectInspector());
            retVal.put(keyStd, valStd);
        }
        return retVal;
    }

    @Override
    public String getDisplayString(String[] args) {
        return "kv_map(" + String.join(", ", args) + " )";
    }
}
