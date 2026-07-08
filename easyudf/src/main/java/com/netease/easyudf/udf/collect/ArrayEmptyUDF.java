package com.netease.easyudf.udf.collect;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorUtils;
import org.apache.hadoop.hive.serde2.objectinspector.StandardListObjectInspector;

import java.util.List;

@Slf4j
public class ArrayEmptyUDF extends GenericUDF {
    private StandardListObjectInspector retValInspector;
    private ListObjectInspector listInspector;

    @Override
    public Object evaluate(DeferredObject[] args) throws HiveException {
        List retVal = (List) retValInspector.create(0);
        Object undeferred = args[0].get();
        int size = listInspector.getListLength(undeferred);
        for (int i = 0; i < size; ++i) {
            Object unInspected = listInspector.getListElement(undeferred, i);
            retVal.add(unInspected);
        }
        return retVal.isEmpty() ? null : retVal;
    }


    @Override
    public String getDisplayString(String[] args) {
        return "array_empty(" + args[0] + " )";
    }


    @Override
    public ObjectInspector initialize(ObjectInspector[] args)
            throws UDFArgumentException {
        ObjectInspector first = args[0];

        if (first.getCategory() == ObjectInspector.Category.LIST) {
            listInspector = (ListObjectInspector) first;
        } else {
            throw new UDFArgumentException(" Expecting an array as argument ");
        }

        retValInspector = (StandardListObjectInspector) ObjectInspectorUtils.getStandardObjectInspector(first);
        return retValInspector;
    }
}