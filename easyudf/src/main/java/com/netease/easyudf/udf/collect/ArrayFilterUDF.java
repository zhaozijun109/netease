package com.netease.easyudf.udf.collect;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class ArrayFilterUDF extends GenericUDF {
    private StandardListObjectInspector retValInspector;
    private ListObjectInspector listInspector;
    private ListObjectInspector secondListInspector;
    private PrimitiveObjectInspector secondPrimitiveInspector;

    @Override
    public Object evaluate(DeferredObject[] args) throws HiveException {
        List retVal = (List) retValInspector.create(0);

        if (args.length == 1) {
            Object obj = args[0].get();
            int firstArrSize = listInspector.getListLength(obj);
            for (int i = 0; i < firstArrSize; ++i) {
                Object unInspected = listInspector.getListElement(obj, i);
                if (unInspected != null) {
                    retVal.add(unInspected);
                }
            }
        } else {
            Set checkSet = new HashSet();
            if (secondListInspector != null) {
                Object obj = args[1].get();
                int secondArrSize = secondListInspector.getListLength(obj);
                for (int i = 0; i < secondArrSize; ++i) {
                    Object unInspected = secondListInspector.getListElement(obj, i);
                    checkSet.add(unInspected);
                }
            } else {
                Object unInspected = secondPrimitiveInspector.preferWritable() ? secondPrimitiveInspector.getPrimitiveWritableObject(args[1].get()) : secondPrimitiveInspector.getPrimitiveJavaObject(args[1].get());
                checkSet.add(unInspected);
            }

            Object obj = args[0].get();
            int firstArrSize = listInspector.getListLength(obj);
            for (int i = 0; i < firstArrSize; ++i) {
                Object unInspected = listInspector.getListElement(obj, i);
                if (!checkSet.contains(unInspected)) {
                    retVal.add(unInspected);
                }
            }
        }
        return retVal.isEmpty() ? null : retVal;
    }


    @Override
    public String getDisplayString(String[] args) {
        return "array_filter(" + String.join(", ", args) + " )";
    }


    @Override
    public ObjectInspector initialize(ObjectInspector[] args)
            throws UDFArgumentException {
        ObjectInspector first = args[0];
        ObjectInspector firstElem;
        if (first.getCategory() == ObjectInspector.Category.LIST) {
            listInspector = (ListObjectInspector) first;
            firstElem = listInspector.getListElementObjectInspector();
        } else {
            throw new UDFArgumentException(" Expecting an array as first argument ");
        }

        if (args.length == 2) {
            ObjectInspector second = args[1];
            ObjectInspector secondElem;
            if (second.getCategory() == ObjectInspector.Category.LIST) {
                secondListInspector = (ListObjectInspector) second;
                secondElem = secondListInspector.getListElementObjectInspector();
            } else if (second.getCategory() == ObjectInspector.Category.PRIMITIVE) {
                secondPrimitiveInspector = (PrimitiveObjectInspector) second;
                secondElem = second;
            } else {
                throw new UDFArgumentException(" Expecting an array or primitive as second argument ");
            }

            if (!ObjectInspectorUtils.compareTypes(firstElem, secondElem)) {
                throw new UDFArgumentException(" Array types must match " + firstElem.getTypeName() + " != " + secondElem.getTypeName());
            }
        }
        retValInspector = (StandardListObjectInspector) ObjectInspectorUtils.getStandardObjectInspector(first);
        return retValInspector;
    }
}