package com.netease.wm.udf.bitmap;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorUtils;

import java.io.IOException;

@Description(name = "bitmap_build", value = "_FUNC_ a - build bitmap value from a long value")
public class BitmapBuildUDF extends GenericUDF {
    private PrimitiveObjectInspector inputOI;

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        ObjectInspector input0 = arguments[0];
        if (!(input0 instanceof PrimitiveObjectInspector)) {
            throw new UDFArgumentException("argument must be a primitive");
        }
        inputOI = (PrimitiveObjectInspector)input0;
        return PrimitiveObjectInspectorFactory.javaByteArrayObjectInspector;
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        BitmapValue bitmapValue = new BitmapValue();
        long value = PrimitiveObjectInspectorUtils.getLong(arguments[0].get(), inputOI);
        bitmapValue.add(value);
        try {
            return BitmapValueUtil.serializeToBytes(bitmapValue);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDisplayString(String[] children) {
        return "Usage: bitmap_build(value)";
    }
}
