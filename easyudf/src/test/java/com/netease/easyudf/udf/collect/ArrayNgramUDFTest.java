package com.netease.easyudf.udf.collect;

import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.StandardListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ArrayNgramUDFTest {
    private ArrayNgramUDF udf;

    @Before
    public void before() {
        udf = new ArrayNgramUDF();
    }

    @Test
    public void evaluate() throws HiveException {
        ObjectInspector intOi = PrimitiveObjectInspectorFactory.javaIntObjectInspector;
        ObjectInspector listOi = ObjectInspectorFactory.getStandardListObjectInspector(intOi);
        StandardListObjectInspector resultOi = (StandardListObjectInspector) udf.initialize(new ObjectInspector[]{listOi, intOi, intOi});

        Integer min = 1;
        Integer max = 3;

        List<Integer> second = new ArrayList<>();
        second.add(1);
        second.add(2);
        second.add(3);
        second.add(4);

        Object result = udf.evaluate(new GenericUDF.DeferredObject[]{new GenericUDF.DeferredJavaObject(second), new GenericUDF.DeferredJavaObject(min), new GenericUDF.DeferredJavaObject(max)});
        assertEquals(9, resultOi.getListLength(result));
    }
}