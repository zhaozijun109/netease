package com.netease.easyudf.udf.collect;

import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.StandardListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.JavaBooleanObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ArrayContainsAnyUDFTest {
    private ArrayContainsAnyUDF udf;

    @Before
    public void before() {
        udf = new ArrayContainsAnyUDF();
    }

    @Test
    public void evaluate() throws HiveException {
        ObjectInspector intOi = PrimitiveObjectInspectorFactory.javaIntObjectInspector;
        ObjectInspector listOi = ObjectInspectorFactory.getStandardListObjectInspector(intOi);
        JavaBooleanObjectInspector resultOi = (JavaBooleanObjectInspector) udf.initialize(new ObjectInspector[]{listOi, listOi});

        List<Integer> first = new ArrayList<>();
        first.add(1);

        List<Integer> second = new ArrayList<>();
        second.add(1);
        second.add(2);
        second.add(4);

        Object result = udf.evaluate(new GenericUDF.DeferredObject[]{new GenericUDF.DeferredJavaObject(first), new GenericUDF.DeferredJavaObject(second)});
        assertTrue(resultOi.get(result));
    }
}