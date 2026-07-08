package com.netease.easyudf.udf.collect;

import com.netease.easyudf.udf.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.*;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.StringObjectInspector;

import java.util.List;

@Slf4j
public class ArrayElementOpUDF extends GenericUDF {
    private StandardListObjectInspector retValInspector;
    private ListObjectInspector listInspector;
    private PrimitiveObjectInspector secondPrimitiveInspector;
    private StringObjectInspector stringObjectInspector;

    enum Method {
        ADD,
        SUB,
        MUL,
        DIV
    }

    @Override
    public Object evaluate(DeferredObject[] args) throws HiveException {
        int firstArrSize = listInspector.getListLength(args[0].get());
        PrimitiveObjectInspector.PrimitiveCategory category = ((PrimitiveObjectInspector) listInspector.getListElementObjectInspector()).getPrimitiveCategory();
        Object target = secondPrimitiveInspector.getPrimitiveJavaObject(args[1].get());
        switch (category) {
            case INT:
                target = Utils.toInt(target);
                break;
            case LONG:
                target = Utils.toLong(target);
                break;
            case FLOAT:
                target = Utils.toFloat(target);
                break;
            default:
                target = Utils.toDouble(target);
        }
        Method method = Method.ADD;
        if (stringObjectInspector != null) {
            method = Method.valueOf(stringObjectInspector.getPrimitiveJavaObject(args[2].get()).toUpperCase());
        }
        List retVal = (List) retValInspector.create(0);
        switch (method) {
            case SUB:
                for (int i = 0; i < firstArrSize; ++i) {
                    Object unInspected = listInspector.getListElement(args[0].get(), i);
                    switch (category) {
                        case INT:
                            unInspected = Utils.toInt(unInspected) - (Integer) target;
                            break;
                        case LONG:
                            unInspected = Utils.toLong(unInspected) - (Long) target;
                            break;
                        case FLOAT:
                            unInspected = Utils.toFloat(unInspected) - (Float) target;
                            break;
                        default:
                            unInspected = Utils.toDouble(unInspected) - (Double) target;
                            break;
                    }
                    retVal.add(unInspected);
                }
                break;
            case MUL:
                for (int i = 0; i < firstArrSize; ++i) {
                    Object unInspected = listInspector.getListElement(args[0].get(), i);
                    switch (category) {
                        case INT:
                            unInspected = Utils.toInt(unInspected) * (Integer) target;
                            break;
                        case LONG:
                            unInspected = Utils.toLong(unInspected) * (Long) target;
                            break;
                        case FLOAT:
                            unInspected = Utils.toFloat(unInspected) * (Float) target;
                            break;
                        default:
                            unInspected = Utils.toDouble(unInspected) * (Double) target;
                            break;
                    }
                    retVal.add(unInspected);
                }
                break;
            case DIV:
                for (int i = 0; i < firstArrSize; ++i) {
                    Object unInspected = listInspector.getListElement(args[0].get(), i);
                    switch (category) {
                        case INT:
                            unInspected = Utils.toInt(unInspected) / (Integer) target;
                            break;
                        case LONG:
                            unInspected = Utils.toLong(unInspected) / (Long) target;
                            break;
                        case FLOAT:
                            unInspected = Utils.toFloat(unInspected) / (Float) target;
                            break;
                        default:
                            unInspected = Utils.toDouble(unInspected) / (Double) target;
                            break;
                    }
                    retVal.add(unInspected);
                }
                break;
            default:
                for (int i = 0; i < firstArrSize; ++i) {
                    Object unInspected = listInspector.getListElement(args[0].get(), i);
                    switch (category) {
                        case INT:
                            unInspected = Utils.toInt(unInspected) + (Integer) target;
                            break;
                        case LONG:
                            unInspected = Utils.toLong(unInspected) + (Long) target;
                            break;
                        case FLOAT:
                            unInspected = Utils.toFloat(unInspected) + (Float) target;
                            break;
                        default:
                            unInspected = Utils.toDouble(unInspected) + (Double) target;
                            break;
                    }
                    retVal.add(unInspected);
                }
        }
        return retVal;
    }


    @Override
    public String getDisplayString(String[] args) {
        return "array_element_op(" + String.join(", ", args) + " )";
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
        retValInspector = ObjectInspectorFactory.getStandardListObjectInspector(listInspector.getListElementObjectInspector());
        return retValInspector;
    }
}