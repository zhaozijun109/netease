package com.netease.easyudf.udf.math;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.apache.hadoop.hive.ql.udf.generic.AbstractGenericUDAFResolver;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator;
import org.apache.hadoop.hive.ql.util.JavaDataModel;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorUtils;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.util.StringUtils;

public class BitXorUDAF extends AbstractGenericUDAFResolver {

    static final Log LOG = LogFactory.getLog(BitXorUDAF.class.getName());

    enum Method {
        AND,
        OR,
        XOR
    }

    @Override
    public GenericUDAFEvaluator getEvaluator(TypeInfo[] parameters)
            throws SemanticException {

        if (parameters[0].getCategory() != ObjectInspector.Category.PRIMITIVE) {
            throw new UDFArgumentTypeException(0,
                    "Only primitive type arguments are accepted but "
                            + parameters[0].getTypeName() + " is passed.");
        }

        return new GenericUDAFBitOpLong();
    }

    /**
     * GenericUDAFSumLong.
     */
    public static class GenericUDAFBitOpLong extends GenericUDAFEvaluator {
        private PrimitiveObjectInspector inputOI;
        private LongWritable result;

        @Override
        public ObjectInspector init(Mode m, ObjectInspector[] parameters) throws HiveException {
            super.init(m, parameters);
            result = new LongWritable(0);
            inputOI = (PrimitiveObjectInspector) parameters[0];
            return PrimitiveObjectInspectorFactory.writableLongObjectInspector;
        }

        /**
         * class for storing double sum value.
         */
        @AggregationType(estimable = true)
        static class LongAgg extends AbstractAggregationBuffer {
            boolean empty;
            long agg;

            @Override
            public int estimate() {
                return JavaDataModel.PRIMITIVES1 + JavaDataModel.PRIMITIVES2;
            }
        }

        @Override
        public AggregationBuffer getNewAggregationBuffer() throws HiveException {
            LongAgg result = new LongAgg();
            reset(result);
            return result;
        }

        @Override
        public void reset(AggregationBuffer agg) throws HiveException {
            LongAgg myagg = (LongAgg) agg;
            myagg.empty = true;
            myagg.agg = 0;
        }

        private boolean warned = false;
        private Method method = Method.XOR;

        @Override
        public void iterate(AggregationBuffer agg, Object[] parameters) throws HiveException {
            try {
                merge(agg, parameters[0]);
            } catch (NumberFormatException e) {
                if (!warned) {
                    warned = true;
                    LOG.warn(getClass().getSimpleName() + " "
                            + StringUtils.stringifyException(e));
                }
            }
        }

        @Override
        public Object terminatePartial(AggregationBuffer agg) throws HiveException {
            return terminate(agg);
        }

        @Override
        public void merge(AggregationBuffer agg, Object partial) throws HiveException {
            if (partial != null) {
                LongAgg myagg = (LongAgg) agg;
                long value = PrimitiveObjectInspectorUtils.getLong(partial, inputOI);
                if (myagg.empty) {
                    myagg.agg = value;
                } else {
                    switch (method) {
                        case OR:
                            myagg.agg = myagg.agg | value;
                            break;
                        case XOR:
                            myagg.agg = myagg.agg ^ value;
                            break;
                        default:
                            myagg.agg = myagg.agg & value;
                    }
                }
                myagg.empty = false;
            }
        }

        @Override
        public Object terminate(AggregationBuffer agg) throws HiveException {
            LongAgg myagg = (LongAgg) agg;
            if (myagg.empty) {
                return null;
            }
            result.set(myagg.agg);
            return result;
        }
    }

}
