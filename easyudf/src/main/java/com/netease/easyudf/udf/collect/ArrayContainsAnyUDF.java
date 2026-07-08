package com.netease.easyudf.udf.collect;


import com.netease.easyudf.udf.util.InspectableObject;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorUtils;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;

import java.util.HashSet;
import java.util.Set;

public class ArrayContainsAnyUDF extends GenericUDF {
    private ListObjectInspector[] listInspector;

    @Override
    public Object evaluate(DeferredObject[] args) throws HiveException {
        Set checkSet = new HashSet();
        Object firstUndeferred = args[0].get();
        int firstArrSize = listInspector[0].getListLength(firstUndeferred);
        for (int i = 0; i < firstArrSize; ++i) {
            Object unInspected = listInspector[0].getListElement(firstUndeferred, i);
            InspectableObject io = new InspectableObject(unInspected, listInspector[0].getListElementObjectInspector());
            checkSet.add(io);
        }

        boolean retVal = false;
        Object secondUndeferred = args[1].get();
        int secondArrSize = listInspector[1].getListLength(secondUndeferred);
        for (int i = 0; i < secondArrSize; ++i) {
            Object unInspected = listInspector[1].getListElement(secondUndeferred, i);
            InspectableObject io = new InspectableObject(unInspected, listInspector[1].getListElementObjectInspector());
            if (checkSet.contains(io)) {
                retVal = true;
                break;
            }
        }

        return retVal;
    }

    @Override
    public String getDisplayString(String[] args) {
        return "array_contains_any(" + String.join(", ", args) + ")";
    }

    @Override
    public ObjectInspector initialize(ObjectInspector[] params)
            throws UDFArgumentException {
        try {
            listInspector = new ListObjectInspector[2];
            listInspector[0] = (ListObjectInspector) params[0];
            listInspector[1] = (ListObjectInspector) params[1];
            if (!ObjectInspectorUtils.compareTypes(listInspector[0], listInspector[1])) {
                throw new UDFArgumentException(" Array types must match " + listInspector[0].getListElementObjectInspector().getTypeName() + " != " + listInspector[1].getListElementObjectInspector().getTypeName());
            }
            return PrimitiveObjectInspectorFactory.javaBooleanObjectInspector;
        } catch (ClassCastException e) {
            throw new UDFArgumentException("array_contains_any expects two list arguments and the list type to match the type of the value being compared");
        }
    }

}
