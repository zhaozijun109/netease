package com.netease.easyudf.udf.collect;

import com.netease.easyml.common.util.CollectionUtil;
import com.netease.easyudf.udf.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.IntObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.StringObjectInspector;

@Slf4j
public class ArrayBinarySearchUDF extends GenericUDF {
    private IntObjectInspector retValInspector;
    private ListObjectInspector listInspector;
    private PrimitiveObjectInspector secondPrimitiveInspector;
    private StringObjectInspector stringObjectInspector;

    enum Method {
        LOWER,
        UPPER
    }

    @Override
    public Object evaluate(DeferredObject[] args) throws HiveException {
        int firstArrSize = listInspector.getListLength(args[0].get());
        double[] array = new double[firstArrSize];
        for (int i = 0; i < firstArrSize; ++i) {
            Object unInspected = listInspector.getListElement(args[0].get(), i);
            array[i] = Utils.toDouble(unInspected);
        }

        Double target = Utils.toDouble(secondPrimitiveInspector.getPrimitiveJavaObject(args[1].get()));
        int idx;
        if (stringObjectInspector != null && Method.UPPER.name().equalsIgnoreCase(stringObjectInspector.getPrimitiveJavaObject(args[2].get()))) {
            idx = CollectionUtil.upperBound(array, target);
        } else {
            idx = CollectionUtil.lowerBound(array, target);
        }
        return idx;
    }


    @Override
    public String getDisplayString(String[] args) {
        return "array_binary_search(" + String.join(", ", args) + " )";
    }


    @Override
    public ObjectInspector initialize(ObjectInspector[] args)
            throws UDFArgumentException {
        ObjectInspector first = args[0];
        if (first.getCategory() == ObjectInspector.Category.LIST) {
            listInspector = (ListObjectInspector) first;
        } else {
            throw new UDFArgumentException(" Expecting an array as first argument ");
        }

        secondPrimitiveInspector = (PrimitiveObjectInspector) args[1];

        if (args.length == 3) {
            stringObjectInspector = (StringObjectInspector) args[2];
        }
        retValInspector = PrimitiveObjectInspectorFactory.javaIntObjectInspector;
        return retValInspector;
    }
}