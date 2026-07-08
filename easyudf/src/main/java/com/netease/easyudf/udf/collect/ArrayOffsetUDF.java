package com.netease.easyudf.udf.collect;

import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.*;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.BooleanObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.IntWritable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linjiuning on 2023/6/14.
 */
public class ArrayOffsetUDF extends GenericUDF {
    private static final String INDEX = "index";
    private static final String DATA = "data";
    private transient ListObjectInspector listObjectInspector;
    private transient BooleanObjectInspector booleanObjectInspector;

    private StandardStructObjectInspector structObjectInspector;
    private StandardListObjectInspector retValInspector;

    @Override
    public ObjectInspector initialize(ObjectInspector[] argOIs) throws UDFArgumentException {
        listObjectInspector = (ListObjectInspector) argOIs[0];
        if (argOIs.length == 2) {
            booleanObjectInspector = (BooleanObjectInspector) argOIs[1];
        }

        List<String> structFieldNames = new ArrayList<>();
        List<ObjectInspector> structFieldObjectInspectors = new ArrayList<>();
        structFieldNames.add(INDEX);
        structFieldObjectInspectors.add(PrimitiveObjectInspectorFactory.writableIntObjectInspector);
        structFieldNames.add(DATA);
        structFieldObjectInspectors.add(listObjectInspector.getListElementObjectInspector());

        structObjectInspector = ObjectInspectorFactory.getStandardStructObjectInspector(structFieldNames, structFieldObjectInspectors);
        retValInspector = ObjectInspectorFactory.getStandardListObjectInspector(structObjectInspector);
        return retValInspector;
    }

    @Override
    public Object evaluate(DeferredObject[] args) throws HiveException {
        boolean reverse = false;
        if (booleanObjectInspector != null) {
            reverse = booleanObjectInspector.get(args[1].get());
        }
        int length = listObjectInspector.getListLength(args[0].get());
        Object result = retValInspector.create(length);
        for (int i = 0; i < length; ++i) {
            Object obj = listObjectInspector.getListElement(args[0].get(), i);
            int offset = reverse ? length - i - 1 : i;
            Object struct = structObjectInspector.create();
            structObjectInspector.setStructFieldData(struct, structObjectInspector.getStructFieldRef(INDEX), new IntWritable(offset));
            structObjectInspector.setStructFieldData(struct, structObjectInspector.getStructFieldRef(DATA), obj);
            retValInspector.set(result, i, struct);
        }
        return result;
    }

    @Override
    public String getDisplayString(String[] args) {
        return "array_offset(" + String.join(", ", args) + " )";
    }
}