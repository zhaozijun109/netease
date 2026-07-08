package com.netease.easyudf.udf.collect;


import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.*;

import java.util.ArrayList;
import java.util.List;

public class ArrayZipUDF extends GenericUDF {
    private StandardStructObjectInspector structObjectInspector;
    private StandardListObjectInspector retValInspector;
    private ObjectInspector[] inputOiList;

    @Override
    public Object evaluate(DeferredObject[] args) throws HiveException {
        int length = ((ListObjectInspector) inputOiList[0]).getListLength(args[0].get());
        for (int i = 1; i < args.length; i++) {
            length = Math.min(length, ((ListObjectInspector) inputOiList[i]).getListLength(args[i].get()));
        }
        Object retVal = retValInspector.create(length);
        for (int i = 0; i < length; ++i) {
            Object struct = structObjectInspector.create();
            for (int j = 0; j < args.length; j++) {
                Object obj = ((ListObjectInspector) inputOiList[j]).getListElement(args[j].get(), i);
                structObjectInspector.setStructFieldData(struct, structObjectInspector.getStructFieldRef(String.valueOf(j)), obj);
                retValInspector.set(retVal, i, struct);
            }
        }
        return retVal;
    }


    @Override
    public String getDisplayString(String[] args) {
        return "array_zip(" + String.join(", ", args) + " )";
    }


    @Override
    public ObjectInspector initialize(ObjectInspector[] args)
            throws UDFArgumentException {
        if (args.length < 2) {
            throw new UDFArgumentException(" Expecting at least two arguments ");
        }
        inputOiList = args;

        List<String> structFieldNames = new ArrayList<>();
        List<ObjectInspector> structFieldObjectInspectors = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            structFieldNames.add(String.valueOf(i));
            structFieldObjectInspectors.add(((ListObjectInspector) args[i]).getListElementObjectInspector());
        }

        structObjectInspector = ObjectInspectorFactory.getStandardStructObjectInspector(structFieldNames, structFieldObjectInspectors);
        retValInspector = ObjectInspectorFactory.getStandardListObjectInspector(structObjectInspector);
        return retValInspector;
    }

}
