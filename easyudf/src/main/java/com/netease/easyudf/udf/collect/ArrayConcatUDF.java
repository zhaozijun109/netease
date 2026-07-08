package com.netease.easyudf.udf.collect;


import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.*;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector.Category;

import java.util.List;

public class ArrayConcatUDF extends GenericUDF {
    private StandardListObjectInspector retValInspector;
    private ObjectInspector[] inputOiList;

    @Override
    public Object evaluate(DeferredObject[] args) throws HiveException {
        List retVal = (List) retValInspector.create(0);
        for (int i = 0; i < args.length; ++i) {
            Object undeferred = args[i].get();
            ObjectInspector objectInspector = inputOiList[i];
            if (objectInspector.getCategory() == Category.LIST) {
                ListObjectInspector listObjectInspector = (ListObjectInspector) objectInspector;
                for (int j = 0; j < listObjectInspector.getListLength(undeferred); ++j) {
                    Object obj = listObjectInspector.getListElement(undeferred, j);
                    retVal.add(obj);
                }
            } else {
                PrimitiveObjectInspector prim = (PrimitiveObjectInspector) objectInspector;
                Object obj = prim.preferWritable() ? prim.getPrimitiveWritableObject(undeferred) : prim.getPrimitiveJavaObject(undeferred);
                retVal.add(obj);
            }
        }
        return retVal;
    }


    @Override
    public String getDisplayString(String[] args) {
        return "array_concat(" + String.join(", ", args) + " )";
    }


    @Override
    public ObjectInspector initialize(ObjectInspector[] args)
            throws UDFArgumentException {
        if (args.length < 2) {
            throw new UDFArgumentException(" Expecting at least two arguments ");
        }
        inputOiList = args;
        ObjectInspector first = args[0];
        ObjectInspector firstElem;
        if (first.getCategory() == Category.LIST) {
            firstElem = ((ListObjectInspector) first).getListElementObjectInspector();
        } else if (first.getCategory() == Category.PRIMITIVE) {
            firstElem = first;
        } else {
            throw new UDFArgumentException(" Expecting an array or primitive as argument ");
        }
        for (int i = 1; i < args.length; ++i) {
            ObjectInspector objectInspector = args[i];
            ObjectInspector elem;
            if (objectInspector.getCategory() == Category.LIST) {
                elem = ((ListObjectInspector) objectInspector).getListElementObjectInspector();
            } else if (objectInspector.getCategory() == Category.PRIMITIVE) {
                elem = objectInspector;
            } else {
                throw new UDFArgumentException(" Expecting an array or primitive as argument ");
            }
            if (!ObjectInspectorUtils.compareTypes(firstElem, elem)) {
                throw new UDFArgumentException(" Array types must match " + firstElem.getTypeName() + " != " + elem.getTypeName());
            }
        }

        retValInspector = ObjectInspectorFactory.getStandardListObjectInspector(firstElem);
        return retValInspector;
    }

}
