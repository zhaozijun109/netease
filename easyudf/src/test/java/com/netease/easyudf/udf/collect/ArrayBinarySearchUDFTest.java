package com.netease.easyudf.udf.collect;

import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class ArrayBinarySearchUDFTest {
    private ArrayBinarySearchUDF udf;

    @Before
    public void before() {
        udf = new ArrayBinarySearchUDF();
    }

    @Test
    public void evaluate() throws HiveException {
        ObjectInspector listOi = ObjectInspectorFactory.getStandardListObjectInspector(PrimitiveObjectInspectorFactory.javaIntObjectInspector);
        PrimitiveObjectInspector resultOi = (PrimitiveObjectInspector) udf.initialize(new ObjectInspector[]{listOi, PrimitiveObjectInspectorFactory.javaIntObjectInspector, PrimitiveObjectInspectorFactory.javaStringObjectInspector});
        List a = Arrays.asList(1, 2, 2, 3);
        Integer b = 2;
        String method = "upper";
        Object result = udf.evaluate(new GenericUDF.DeferredObject[]{new GenericUDF.DeferredJavaObject(a), new GenericUDF.DeferredJavaObject(b), new GenericUDF.DeferredJavaObject(method)});
        assertEquals(resultOi.getPrimitiveJavaObject(result), 3);
    }
}