package com.netease.easyudf.udf.collect;

import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class ArrayElementOpUDFTest {
    private ArrayElementOpUDF udf;

    @Before
    public void before() {
        udf = new ArrayElementOpUDF();
    }

    @Test
    public void evaluate() throws HiveException {
        ObjectInspector listOi = ObjectInspectorFactory.getStandardListObjectInspector(PrimitiveObjectInspectorFactory.javaIntObjectInspector);
        udf.initialize(new ObjectInspector[]{listOi, PrimitiveObjectInspectorFactory.javaIntObjectInspector, PrimitiveObjectInspectorFactory.javaStringObjectInspector});
        List a = Arrays.asList(1, 2, 2, 3);
        Integer b = 2;
        String method = "div";
        Object result = udf.evaluate(new GenericUDF.DeferredObject[]{new GenericUDF.DeferredJavaObject(a), new GenericUDF.DeferredJavaObject(b), new GenericUDF.DeferredJavaObject(method)});
        System.out.println(result);
    }
}