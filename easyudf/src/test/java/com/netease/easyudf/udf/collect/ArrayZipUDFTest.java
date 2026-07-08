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

public class ArrayZipUDFTest {
    private ArrayZipUDF udf;

    @Before
    public void before() {
        udf = new ArrayZipUDF();
    }

    @Test
    public void evaluate() throws HiveException {
        ObjectInspector listOi = ObjectInspectorFactory.getStandardListObjectInspector(PrimitiveObjectInspectorFactory.javaIntObjectInspector);
        StandardListObjectInspector resultOi = (StandardListObjectInspector) udf.initialize(new ObjectInspector[]{listOi, listOi});
        List a = Arrays.asList(1, 2, 3);
        List b = Arrays.asList(3, 4);
        Object result = udf.evaluate(new GenericUDF.DeferredObject[]{new GenericUDF.DeferredJavaObject(a), new GenericUDF.DeferredJavaObject(b)});
        assertEquals(2, resultOi.getListLength(result));
    }
}