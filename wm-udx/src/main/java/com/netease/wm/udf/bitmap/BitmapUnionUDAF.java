package com.netease.wm.udf.bitmap;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.apache.hadoop.hive.ql.udf.generic.AbstractGenericUDAFResolver;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.BinaryObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;

import java.io.IOException;

/**
 * bitmap_union.
 *
 */
@Description(name = "bitmap_union", value = "_FUNC_(expr) - Calculate the grouped bitmap"
        + " union , Returns an doris bitmap representation of a column.")
public class BitmapUnionUDAF extends AbstractGenericUDAFResolver {

    @Override
    public GenericUDAFEvaluator getEvaluator(TypeInfo[] parameters)
            throws SemanticException {
        if (parameters.length != 1) {
            throw new UDFArgumentTypeException(parameters.length - 1,
                    "Exactly one argument is expected.");
        }
        return new GenericEvaluate();
    }

    //The UDAF evaluator assumes that all rows it's evaluating have
    //the same (desired) value.
    public static class GenericEvaluate extends GenericUDAFEvaluator {

        private transient BinaryObjectInspector inputOI;
        private transient BinaryObjectInspector internalMergeOI;

        @Override
        public ObjectInspector init(Mode m, ObjectInspector[] parameters)
                throws HiveException {
            super.init(m, parameters);
            // init output object inspectors
            // The output of a partial aggregation is a binary
            if (m == Mode.PARTIAL1) {
                this.inputOI = (BinaryObjectInspector) parameters[0];
            } else {
                this.internalMergeOI = (BinaryObjectInspector) parameters[0];
            }
            return PrimitiveObjectInspectorFactory.javaByteArrayObjectInspector;
        }

        /** class for storing the current partial result aggregation */
        @AggregationType(estimable = true)
        static class BitmapAgg extends AbstractAggregationBuffer {
            BitmapValue bitmap;
        }

        @Override
        public void reset(AggregationBuffer agg) throws HiveException {
            ((BitmapAgg) agg).bitmap = new BitmapValue();
        }

        @Override
        public AggregationBuffer getNewAggregationBuffer() throws HiveException {
            BitmapAgg result = new BitmapAgg();
            reset(result);
            return result;
        }

        @Override
        public void iterate(AggregationBuffer agg, Object[] parameters) throws HiveException {
            assert (parameters.length == 1);
            Object p = parameters[0];
            if (p != null) {
                BitmapAgg myagg = (BitmapAgg) agg;
                byte[] partialResult = this.inputOI.getPrimitiveJavaObject(parameters[0]);
                try {
                    myagg.bitmap.or(BitmapValueUtil.deserializeToBitmap(partialResult));
                } catch (IOException ioException) {
                    throw new HiveException(ioException);
                }
            }
        }

        @Override
        public Object terminate(AggregationBuffer agg) {
            BitmapAgg myagg = (BitmapAgg) agg;
            try {
                return BitmapValueUtil.serializeToBytes(myagg.bitmap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void merge(AggregationBuffer agg, Object partial) {
            BitmapAgg myagg = (BitmapAgg) agg;
            byte[] partialResult = this.internalMergeOI.getPrimitiveJavaObject(partial);
            try {
                myagg.bitmap.or(BitmapValueUtil.deserializeToBitmap(partialResult));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Object terminatePartial(AggregationBuffer agg) {
            return terminate(agg);
        }
    }
}
