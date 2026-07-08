package com.netease.wm.udf.bitmap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.BinaryObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;

import java.io.IOException;

@Description(name = "bitmap_to_array", value = "_FUNC_ a - extract all element in bitmap")
public class BitmapToArrayUDF extends GenericUDF {

    private transient BinaryObjectInspector inputOI;

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {

        ObjectInspector input = arguments[0];
        if (!(input instanceof BinaryObjectInspector)) {
            throw new UDFArgumentException("first argument must be a binary");
        }

        this.inputOI = (BinaryObjectInspector) input;

        return ObjectInspectorFactory.getStandardListObjectInspector(PrimitiveObjectInspectorFactory.javaLongObjectInspector);
    }

    @Override
    public Object evaluate(DeferredObject[]  args) throws HiveException {
        if (args[0] == null) {
            return null;
        }

        byte[] inputBytes = this.inputOI.getPrimitiveJavaObject(args[0].get());

        if (inputBytes == null) {
            return null;
        }

        try {
            BitmapValue bitmapValue = BitmapValueUtil.deserializeToBitmap(inputBytes);
            return ArrayUtils.toObject(bitmapValue.toArray());
        } catch (IOException ioException) {
            throw new HiveException(ioException);
        }
    }

    @Override
    public String getDisplayString(String[] children) {
        return "Usage: bitmap_to_array(bitmap)";
    }
}
