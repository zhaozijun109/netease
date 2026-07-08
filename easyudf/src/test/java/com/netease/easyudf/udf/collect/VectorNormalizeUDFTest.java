package com.netease.easyudf.udf.collect;

import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.time.DayOfWeek;
import java.time.LocalDate;

import java.util.List;

public class VectorNormalizeUDFTest {
    private VectorNormalizeUDF udf;

    @Before
    public void before() {
        udf = new VectorNormalizeUDF();
    }

    @Test
    public void evaluate() throws HiveException {
        ObjectInspector listOi = ObjectInspectorFactory.getStandardListObjectInspector(PrimitiveObjectInspectorFactory.javaIntObjectInspector);
        udf.initialize(new ObjectInspector[]{listOi, PrimitiveObjectInspectorFactory.javaStringObjectInspector});
        List a = Arrays.asList(1, 2, 2, 3);
        String type = "l1";
        Object result = udf.evaluate(new GenericUDF.DeferredObject[]{new GenericUDF.DeferredJavaObject(a), new GenericUDF.DeferredJavaObject(type)});
        System.out.println(result);
    }

    @Test
    public void name() {

// Get today's date
        LocalDate today = LocalDate.now();

// Get the previous Monday date
        LocalDate prevMonday = today.with(DayOfWeek.MONDAY).minusDays(14);
        System.out.println(prevMonday.toString());
    }
}