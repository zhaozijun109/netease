package com.netease.easyudf.udf.collect;

import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.StandardListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ArrayOffsetUDFTest {
    private ArrayOffsetUDF udf;

    @Before
    public void before() {
        udf = new ArrayOffsetUDF();
    }

    @Test
    public void evaluate() throws HiveException {
        ObjectInspector listOi = ObjectInspectorFactory.getStandardListObjectInspector(PrimitiveObjectInspectorFactory.javaIntObjectInspector);
        StandardListObjectInspector resultOi = (StandardListObjectInspector) udf.initialize(new ObjectInspector[]{listOi, PrimitiveObjectInspectorFactory.javaBooleanObjectInspector});
        List input = Arrays.asList(1, 2, 3);
        Object result = udf.evaluate(new GenericUDF.DeferredObject[]{new GenericUDF.DeferredJavaObject(input), new GenericUDF.DeferredJavaObject(true)});
        assertEquals(3, resultOi.getListLength(result));
    }
}