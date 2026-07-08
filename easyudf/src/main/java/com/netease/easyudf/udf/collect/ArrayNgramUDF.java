package com.netease.easyudf.udf.collect;


import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.StandardListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.IntObjectInspector;

import java.util.List;

public class ArrayNgramUDF extends GenericUDF {
    private StandardListObjectInspector inputOiList;

    private StandardListObjectInspector retValInspector;
    private IntObjectInspector minInspector;
    private IntObjectInspector maxInspector;

    @Override
    public Object evaluate(DeferredObject[] args) throws HiveException {
        List retVal = (List) retValInspector.create(0);
        Object data = args[0].get();
        int length = inputOiList.getListLength(data);
        int min = minInspector.get(args[1].get());
        int max = maxInspector.get(args[2].get());
        for (int i = min; i <= max; ++i) {
            for (int j = 0; j <= length - i; j++) {
                List o = (List) inputOiList.create(0);
                for (int k = j; k < j + i; k++) {
                    Object obj = inputOiList.getListElement(data, k);
                    o.add(obj);
                }
                retVal.add(o);
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
        if (args.length < 3) {
            throw new UDFArgumentException(" Expecting at least three arguments ");
        }
        inputOiList = (StandardListObjectInspector) args[0];
        minInspector = (IntObjectInspector) args[1];
        maxInspector = (IntObjectInspector) args[2];

        retValInspector = ObjectInspectorFactory.getStandardListObjectInspector(ObjectInspectorFactory.getStandardListObjectInspector(inputOiList.getListElementObjectInspector()));
        return retValInspector;
    }

}
