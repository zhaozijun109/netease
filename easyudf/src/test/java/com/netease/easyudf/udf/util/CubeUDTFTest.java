package com.netease.easyudf.udf.util;

import com.alibaba.fastjson.JSON;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.StringObjectInspector;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

public class CubeUDTFTest {
    private CubeUDTF udtf;

    @Before
    public void before() {
        udtf = new CubeUDTF();
    }

    @Test
    public void evaluate() throws HiveException {
        StructObjectInspector inpOi = ObjectInspectorFactory.getStandardStructObjectInspector(
                Arrays.asList("a", "b"),
                Arrays.asList(PrimitiveObjectInspectorFactory.javaIntObjectInspector,
                        PrimitiveObjectInspectorFactory.javaStringObjectInspector));
        udtf.initialize(new ObjectInspector[]{inpOi});

        List input = Arrays.asList(1, Arrays.asList(null, "t"));

        List<Object> out = new ArrayList<>();
        udtf.setCollector(it -> out.add(JSON.toJSONString(it)));
        udtf.process(new Object[]{input});
        assertEquals(4, out.size());
    }

    @Test
    public void evaluateGroupingSet() throws HiveException {
        StructObjectInspector inpOi = ObjectInspectorFactory.getStandardStructObjectInspector(
                Arrays.asList("a", "b"),
                Arrays.asList(PrimitiveObjectInspectorFactory.javaIntObjectInspector,
                        PrimitiveObjectInspectorFactory.javaStringObjectInspector));
        StringObjectInspector groupingSetOi = PrimitiveObjectInspectorFactory.javaStringObjectInspector;
        udtf.initialize(new ObjectInspector[]{inpOi, groupingSetOi});

        List input = Arrays.asList(1, Arrays.asList(null, "t"));
        String groupingSet = "((), (a), (a,b))";

        List<Object> out = new ArrayList<>();
        udtf.setCollector(it -> out.add(JSON.toJSONString(it)));
        udtf.process(new Object[]{input, groupingSet});
        assertEquals(3, out.size());
    }

    private static final Pattern H5 = Pattern.compile("html/activities/([^/]+)/");
    private static final Pattern TAG = Pattern.compile("tag/([^/]+)$");

    @Test
    public void regex(){
        String url = "https://www.lofter.com/tag/代号鸢";
        Matcher m = TAG.matcher(url);
        boolean b = m.find();
        System.out.println(m.group(1));
    }
}