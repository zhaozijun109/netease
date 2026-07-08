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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class ArrayDistinctUDF extends GenericUDF {
    private StandardListObjectInspector retValInspector;
    private ListObjectInspector listInspector;


    @Override
    public Object evaluate(DeferredObject[] args) throws HiveException {
        Set checkSet = new HashSet();
        Object firstUndeferred = args[0].get();
        int firstArrSize = listInspector.getListLength(firstUndeferred);
        for (int i = 0; i < firstArrSize; ++i) {
            Object unInspected = listInspector.getListElement(firstUndeferred, i);
            InspectableObject io = new InspectableObject(unInspected, listInspector.getListElementObjectInspector());
            checkSet.add(io);
        }

        List retVal = (List) retValInspector.create(0);
        for (Object io : checkSet) {
            InspectableObject inspObj = (InspectableObject) io;

            Object stdObj = ObjectInspectorUtils.copyToStandardObject(inspObj.o, inspObj.oi);
            retVal.add(stdObj);
        }
        return retVal.isEmpty() ? null : retVal;
    }


    @Override
    public String getDisplayString(String[] args) {
        return "array_distinct(" + args[0] + " )";
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