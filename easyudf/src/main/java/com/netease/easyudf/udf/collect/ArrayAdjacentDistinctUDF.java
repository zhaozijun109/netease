package com.netease.easyudf.udf.collect;

import com.netease.easyudf.udf.util.InspectableObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorUtils;
import org.apache.hadoop.hive.serde2.objectinspector.StandardListObjectInspector;

import java.util.List;
import java.util.Objects;

@Slf4j
public class ArrayAdjacentDistinctUDF extends GenericUDF {
    private StandardListObjectInspector retValInspector;
    private ListObjectInspector listInspector;

    @Override
    public Object evaluate(DeferredObject[] args) throws HiveException {
        InspectableObject last = null;
        List retVal = (List) retValInspector.create(0);
        Object firstUndeferred = args[0].get();
        int firstArrSize = listInspector.getListLength(firstUndeferred);
        for (int i = 0; i < firstArrSize; ++i) {
            Object unInspected = listInspector.getListElement(firstUndeferred, i);
            InspectableObject io = new InspectableObject(unInspected, listInspector.getListElementObjectInspector());
            if (Objects.equals(io, last)) {
                continue;
            }
            Object stdObj = ObjectInspectorUtils.copyToStandardObject(io.o, io.oi);
            retVal.add(stdObj);
            last = io;
        }
        return retVal.isEmpty() ? null : retVal;
    }


    @Override
    public String getDisplayString(String[] args) {
        return "array_adjacent_distinct(" + args[0] + " )";
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