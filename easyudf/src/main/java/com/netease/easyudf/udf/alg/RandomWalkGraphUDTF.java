package com.netease.easyudf.udf.alg;

import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDTF;
import org.apache.hadoop.hive.serde2.objectinspector.ListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.IntObjectInspector;

import java.util.Arrays;
import java.util.List;

public class RandomWalkGraphUDTF extends GenericUDTF {
    private static final String S = "s";
    private static final String D = "d";
    private transient ListObjectInspector listObjectInspector;
    private transient IntObjectInspector intObjectInspector;

    private Object[] forwardListObj;

    @Override
    public StructObjectInspector initialize(ObjectInspector[] argOIs) throws UDFArgumentException {
        this.listObjectInspector = (ListObjectInspector) argOIs[0];
        this.intObjectInspector = (IntObjectInspector) argOIs[1];

        List<String> structFieldNames = Arrays.asList(S, D);
        ObjectInspector listElementObjectInspector = listObjectInspector.getListElementObjectInspector();
        List<ObjectInspector> structFieldObjectInspectors = Arrays.asList(listElementObjectInspector, listElementObjectInspector);
        return ObjectInspectorFactory.getStandardStructObjectInspector(structFieldNames, structFieldObjectInspectors);
    }

    public void process(Object[] array, int window) throws HiveException {
        if (array == null || array.length < 2) {
            return;
        }

        for (int i = 0; i <= array.length - window; i++) {
            for (int j = i + 1; j < i + window; j++) {
                forwardListObj[0] = array[i];
                forwardListObj[1] = array[j];
                forward(forwardListObj);
            }
        }
    }

    @Override
    public void process(Object[] args) throws HiveException {
        int length = listObjectInspector.getListLength(args[0]);
        Object[] array = new Object[length];
        for (int i = 0; i < length; i++) {
            array[i] = listObjectInspector.getListElement(args[0], i);
        }
        int w = intObjectInspector.get(args[1]);
        forwardListObj = new Object[2];
        process(array, w);
    }

    @Override
    public void close() throws HiveException {

    }
}
