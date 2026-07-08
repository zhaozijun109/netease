package com.netease.easyudf.udf.collect;


import brickhouse.udf.timeseries.NumericUtil;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.*;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector.Category;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.StringObjectInspector;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Normalize a Vector
 */
@Description(
        name = "vector_normalize",
        value = " Normalize a Vector"
)
public class VectorNormalizeUDF extends GenericUDF {
    private static final Logger LOG = Logger.getLogger(VectorNormalizeUDF.class);
    private ListObjectInspector listInspector;
    private MapObjectInspector mapInspector;
    private PrimitiveObjectInspector valueInspector;

    private StringObjectInspector typeObjectInspector;

    private StandardListObjectInspector retListInspector;
    private StandardMapObjectInspector retMapInspector;

    enum Type {
        L1,
        L2
    }

    public Object evaluateList(Object listObj, Type type) {
        int listLength = listInspector.getListLength(listObj);
        Object retList = retListInspector.create(listLength);
        double tot = 0.0;
        for (int i = 0; i < listLength; ++i) {
            Object listVal = this.listInspector.getListElement(listObj, i);
            double listDbl = NumericUtil.getNumericValue(valueInspector, listVal);
            if (type == Type.L2) {
                tot += listDbl * listDbl;
            } else {
                tot += listDbl;
            }
        }
        if (type == Type.L2) {
            tot = Math.sqrt(tot);
        }
        for (int i = 0; i < listLength; ++i) {
            Object listVal = this.listInspector.getListElement(listObj, i);
            double listDbl = NumericUtil.getNumericValue(valueInspector, listVal);
            retListInspector.set(retList, i, NumericUtil.castToPrimitiveNumeric(listDbl / tot,
                    ((PrimitiveObjectInspector) retListInspector.getListElementObjectInspector()).getPrimitiveCategory()));
        }

        return retList;
    }

    public Object evaluateMap(Object uninspMapObj, Type type) {
        Object retMap = retMapInspector.create();
        Map map = mapInspector.getMap(uninspMapObj);
        double tot = 0.0;
        for (Object mapKey : map.keySet()) {
            Object mapValObj = map.get(mapKey);
            double mapValDbl = NumericUtil.getNumericValue(valueInspector, mapValObj);
            if (type == Type.L2) {
                tot += mapValDbl * mapValDbl;
            } else {
                tot += mapValDbl;
            }
        }
        if (type == Type.L2) {
            tot = Math.sqrt(tot);
        }
        for (Object mapKey : map.keySet()) {
            Object mapValObj = map.get(mapKey);
            double mapValDbl = NumericUtil.getNumericValue(valueInspector, mapValObj);
            double newVal = mapValDbl / tot;

            Object stdKey = ObjectInspectorUtils.copyToStandardJavaObject(mapKey,
                    mapInspector.getMapKeyObjectInspector());
            Object stdVal = NumericUtil.castToPrimitiveNumeric(newVal,
                    ((PrimitiveObjectInspector) retMapInspector.getMapValueObjectInspector()).getPrimitiveCategory());
            retMapInspector.put(retMap, stdKey, stdVal);

        }
        return retMap;
    }


    @Override
    public Object evaluate(DeferredObject[] arg0) throws HiveException {
        Type type = Type.L2;
        if (typeObjectInspector != null) {
            type = Type.valueOf(typeObjectInspector.getPrimitiveJavaObject(arg0[1].get()).toUpperCase());
        }
        if (listInspector != null) {
            return evaluateList(arg0[0].get(), type);
        } else {
            return evaluateMap(arg0[0].get(), type);
        }
    }

    @Override
    public String getDisplayString(String[] arg0) {
        return "vector_normalize";
    }


    private void usage(String message) throws UDFArgumentException {
        LOG.error("vector_normalize: Normalize a vector : " + message);
        throw new UDFArgumentException("vector_normalize: Normalize a vector : " + message);
    }


    @Override
    public ObjectInspector initialize(ObjectInspector[] arg0)
            throws UDFArgumentException {
        if (arg0[0].getCategory() == Category.MAP) {
            this.mapInspector = (MapObjectInspector) arg0[0];

            if (mapInspector.getMapKeyObjectInspector().getCategory() != Category.PRIMITIVE)
                usage("Vector map key must be a primitive");

            if (mapInspector.getMapValueObjectInspector().getCategory() != Category.PRIMITIVE)
                usage("Vector map value must be a primitive");

            this.valueInspector = (PrimitiveObjectInspector) mapInspector.getMapValueObjectInspector();
        } else if (arg0[0].getCategory() == Category.LIST) {
            this.listInspector = (ListObjectInspector) arg0[0];

            if (listInspector.getListElementObjectInspector().getCategory() != Category.PRIMITIVE)
                usage("Vector array value must be a primitive");

            this.valueInspector = (PrimitiveObjectInspector) listInspector.getListElementObjectInspector();
        } else {
            usage("First argument must be an array or map");
        }

        if (arg0.length > 1) {
            typeObjectInspector = (StringObjectInspector) arg0[1];
        }

        if (!NumericUtil.isNumericCategory(valueInspector.getPrimitiveCategory())) {
            usage(" Vector values must be numeric");
        }

        if (listInspector != null) {
            retListInspector = ObjectInspectorFactory.getStandardListObjectInspector(PrimitiveObjectInspectorFactory.javaDoubleObjectInspector);
            return retListInspector;
        } else {
            retMapInspector = ObjectInspectorFactory.getStandardMapObjectInspector(
                    ObjectInspectorUtils.getStandardObjectInspector(mapInspector.getMapKeyObjectInspector(),
                            ObjectInspectorUtils.ObjectInspectorCopyOption.JAVA),
                    ObjectInspectorUtils.getStandardObjectInspector(PrimitiveObjectInspectorFactory.javaDoubleObjectInspector,
                            ObjectInspectorUtils.ObjectInspectorCopyOption.JAVA));
            return retMapInspector;
        }
    }
}
