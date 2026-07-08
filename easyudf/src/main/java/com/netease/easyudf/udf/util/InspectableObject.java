package com.netease.easyudf.udf.util;

import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorUtils;

public class InspectableObject implements Comparable {
    public Object o;
    public ObjectInspector oi;

    public InspectableObject(Object o, ObjectInspector oi) {
        this.o = o;
        this.oi = oi;
    }

    @Override
    public int hashCode() {
        return ObjectInspectorUtils.hashCode(o, oi);
    }

    @Override
    public int compareTo(Object arg0) {
        InspectableObject otherInsp = (InspectableObject) arg0;
        return ObjectInspectorUtils.compare(o, oi, otherInsp.o, otherInsp.oi);
    }

    @Override
    public boolean equals(Object other) {
        return compareTo(other) == 0;
    }

}