package com.netease.wm.udf.bitmap;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.BinaryObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;

import java.io.IOException;

@Description(name = "bitmap_xor", value = "a _FUNC_ b - Compute the symmetric"
        + " union of two or more input bitmaps, return the new bitmap")
public class BitmapXorUDF extends GenericUDF {
    private transient BinaryObjectInspector inputOI0;
    private transient BinaryObjectInspector inputOI1;

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {

        ObjectInspector input0 = arguments[0];
        ObjectInspector input1 = arguments[1];
        if (!(input0 instanceof BinaryObjectInspector) || !(input1 instanceof BinaryObjectInspector)) {
            throw new UDFArgumentException("first and second argument must be a binary");
        }

        this.inputOI0 = (BinaryObjectInspector) input0;
        this.inputOI1 = (BinaryObjectInspector) input1;

        return PrimitiveObjectInspectorFactory.javaByteArrayObjectInspector;
    }

    @Override
    public Object evaluate(DeferredObject[]  args) throws HiveException {
        if (args[0] == null || args[1] == null) {
            return null;
        }
        byte[] inputBytes0 = this.inputOI0.getPrimitiveJavaObject(args[0].get());
        byte[] inputBytes1 = this.inputOI1.getPrimitiveJavaObject(args[1].get());

        if (inputBytes0 == null || inputBytes1 == null) {
            return null;
        }

        try {
            BitmapValue bitmapValue0 = BitmapValueUtil.deserializeToBitmap(inputBytes0);
            BitmapValue bitmapValue1 = BitmapValueUtil.deserializeToBitmap(inputBytes1);
            bitmapValue0.xor(bitmapValue1);
            return BitmapValueUtil.serializeToBytes(bitmapValue0);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    @Override
    public String getDisplayString(String[] children) {
        return "Usage: bitmap_xor(bitmap1,bitmap2)";
    }
}
