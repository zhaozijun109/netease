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
import static org.junit.Assert.assertTrue;

public class ArrayDistinctUDFTest {

    private ArrayDistinctUDF udf;

    @Before
    public void before() {
        udf = new ArrayDistinctUDF();
    }

    @Test
    public void evaluate() throws HiveException {
        ObjectInspector intOi = PrimitiveObjectInspectorFactory.javaIntObjectInspector;
        ObjectInspector listOi = ObjectInspectorFactory.getStandardListObjectInspector(intOi);
        StandardListObjectInspector resultOi = (StandardListObjectInspector) udf.initialize(new ObjectInspector[]{listOi});

        List<Integer> one = new ArrayList<>();
        one.add(1);
        one.add(2);
        one.add(2);

        Object result = udf.evaluate(new GenericUDF.DeferredObject[]{new GenericUDF.DeferredJavaObject(one)});
        assertEquals(2, resultOi.getListLength(result));
        assertTrue(resultOi.getList(result).contains(1));
        assertTrue(resultOi.getList(result).contains(2));
    }

}