package com.netease.easyudf.udf.collect;


import brickhouse.udf.timeseries.NumericUtil;
import com.netease.easyudf.udf.util.Utils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.apache.hadoop.hive.ql.udf.generic.AbstractGenericUDAFResolver;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator;
import org.apache.hadoop.hive.serde2.objectinspector.*;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector.Category;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.StringObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;

import java.util.ArrayList;


@Description(name = "vector_union",
        value = "_FUNC_(x) - Aggregate adding vectors together "
)
@Deprecated
public class VectorUnionUDAF extends AbstractGenericUDAFResolver {

    enum Type {
        SUM,
        AVG,
        MIN,
        MAX
    }
    /// Snarfed from Hives CollectSet UDAF

    @Override
    public GenericUDAFEvaluator getEvaluator(TypeInfo[] parameters)
            throws SemanticException {
        // TODO Auto-generated method stub
        if (parameters[0].getCategory() == Category.LIST) {
            return new VectorArrayUDAFEvaluator();
        } else {
            throw new UDFArgumentTypeException(0, " vector_union aggregates arrays");
        }
    }

    public static class VectorArrayUDAFEvaluator extends GenericUDAFEvaluator {
        // For PARTIAL1 and COMPLETE: ObjectInspectors for original data, an array
        private ListObjectInspector inputOI;
        private StringObjectInspector typeObjectInspector;
        // For PARTIAL2 and FINAL: ObjectInspectors for partial aggregations (list
        //  ( sum of arrays, or arrays)
        private StandardListObjectInspector stdListOI;


        static class VectorArrayAggBuffer implements AggregationBuffer {
            ArrayList<Double> sumArray = new ArrayList<>();
            int count = 0;
            Type type = Type.SUM;
        }

        public ObjectInspector init(Mode m, ObjectInspector[] parameters)
                throws HiveException {
            super.init(m, parameters);
            inputOI = (ListObjectInspector) parameters[0];
            if (inputOI.getListElementObjectInspector().getCategory() != Category.PRIMITIVE
                    || !NumericUtil.isNumericCategory(
                    ((PrimitiveObjectInspector) inputOI.getListElementObjectInspector()).getPrimitiveCategory())) {
                throw new HiveException("Vector values must be numeric.");
            }
            if (parameters.length > 1) {
                typeObjectInspector = (StringObjectInspector) parameters[1];
            }
            /// always return the standard list of doubles
            stdListOI = ObjectInspectorFactory
                    .getStandardListObjectInspector(PrimitiveObjectInspectorFactory.javaDoubleObjectInspector);
            return stdListOI;
        }

        @Override
        public AggregationBuffer getNewAggregationBuffer() throws HiveException {
            AggregationBuffer buff = new VectorArrayAggBuffer();
            reset(buff);
            return buff;
        }

        @Override
        public void iterate(AggregationBuffer agg, Object[] parameters)
                throws HiveException {
            Object p = parameters[0];
            Type type = Type.SUM;
            if (typeObjectInspector != null) {
                type = Type.valueOf(typeObjectInspector.getPrimitiveJavaObject(parameters[1]).toUpperCase());
            }
            if (p != null) {
                VectorArrayAggBuffer myagg = (VectorArrayAggBuffer) agg;
                myagg.type = type;
                addVector(p, myagg, inputOI);
            }
        }

        @Override
        public void merge(AggregationBuffer agg, Object partial)
                throws HiveException {
            VectorArrayAggBuffer myagg = (VectorArrayAggBuffer) agg;
            addVector(partial, myagg, this.inputOI);
        }

        @Override
        public void reset(AggregationBuffer buff) throws HiveException {
            VectorArrayAggBuffer arrayBuff = (VectorArrayAggBuffer) buff;
            arrayBuff.sumArray = new ArrayList<>();
            arrayBuff.count = 0;
            arrayBuff.type = Type.SUM;
        }

        @Override
        public Object terminate(AggregationBuffer agg) throws HiveException {
            VectorArrayAggBuffer myagg = (VectorArrayAggBuffer) agg;
            ArrayList<Double> array = myagg.sumArray;
            if (myagg.type == Type.AVG) {
                array.replaceAll(i -> i / myagg.count);
            }
            return array;
        }

        private void addVector(Object listObj, VectorArrayAggBuffer myagg, ListObjectInspector inputOI) {
            int listLen = inputOI.getListLength(listObj);
            if (myagg.type == Type.AVG) {
                myagg.count += Utils.toInt(inputOI.getListElement(listObj, listLen - 1));
                listLen -= 1;
            } else {
                myagg.count += 1;
            }
            if (myagg.sumArray.isEmpty()) {
                for (int i = 0; i < listLen; i++) {
                    myagg.sumArray.add(0.0);
                }
            }

            for (int i = 0; i < listLen; ++i) {
                Object listElem = inputOI.getListElement(listObj, i);
                double listElemDbl = NumericUtil.getNumericValue(
                        (PrimitiveObjectInspector) inputOI.getListElementObjectInspector(), listElem);
                Double oldVal = myagg.sumArray.get(i);
                if (oldVal != null) {
                    double value;
                    switch (myagg.type) {
                        case MIN:
                            value = Math.min(oldVal, listElemDbl);
                            break;
                        case MAX:
                            value = Math.max(oldVal, listElemDbl);
                            break;
                        default:
                            value = oldVal + listElemDbl;
                    }
                    myagg.sumArray.set(i, value);
                } else {
                    myagg.sumArray.set(i, listElemDbl);
                }
            }
        }

        @Override
        public Object terminatePartial(AggregationBuffer agg) throws HiveException {
            VectorArrayAggBuffer myagg = (VectorArrayAggBuffer) agg;
            if (myagg.type == Type.AVG) {
                myagg.sumArray.add((double) myagg.count);
            }
            return myagg.sumArray;
        }
    }

}
