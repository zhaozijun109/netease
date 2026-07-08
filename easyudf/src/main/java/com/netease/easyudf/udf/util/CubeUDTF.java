package com.netease.easyudf.udf.util;

import com.netease.easyml.common.util.ArrayUtil;
import com.netease.easyml.common.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDTF;
import org.apache.hadoop.hive.serde2.objectinspector.*;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.StringObjectInspector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CubeUDTF extends GenericUDTF {
    private static final Pattern PT = Pattern.compile("\\([^()]*\\)");
    private transient StructObjectInspector structInspector;
    private transient StringObjectInspector stringInspector;

    private Object[] forwardListObj;

    @Override
    public StructObjectInspector initialize(ObjectInspector[] argOIs) throws UDFArgumentException {
        this.structInspector = (StructObjectInspector) argOIs[0];
        List<? extends StructField> structFields = structInspector.getAllStructFieldRefs();

        if (argOIs.length > 1) {
            stringInspector = (StringObjectInspector) argOIs[1];
        }

        List<String> structFieldNames = new ArrayList<>();
        List<ObjectInspector> structFieldObjectInspectors = new ArrayList<>();
        for (StructField structField : structFields) {
            structFieldNames.add(structField.getFieldName());
            ObjectInspector inspector = structField.getFieldObjectInspector();
            switch (inspector.getCategory()) {
                case PRIMITIVE:
                    break;
                case LIST:
                    inspector = ((ListObjectInspector) inspector).getListElementObjectInspector();
                    break;
                default:
                    throw new UDFArgumentException(inspector.getCategory().toString());
            }
            structFieldObjectInspectors.add(inspector);
        }

        return ObjectInspectorFactory.getStandardStructObjectInspector(structFieldNames, structFieldObjectInspectors);
    }

    public void process(List<Object> fieldsData, int i) throws HiveException {
        if (i >= forwardListObj.length) {
            forward(forwardListObj);
            return;
        }
        Object o = fieldsData.get(i);
        process(fieldsData, i + 1);
        if (o != null) {
            if (ArrayUtil.isNDArray(o)) {
                for (int j = 0; j < ArrayUtil.size0(o); j++) {
                    Object v = ArrayUtil.get(o, j);
                    if (v != null) {
                        forwardListObj[i] = v;
                        process(fieldsData, i + 1);
                        forwardListObj[i] = null;
                    }
                }
            } else {
                forwardListObj[i] = o;
                process(fieldsData, i + 1);
                forwardListObj[i] = null;
            }
        }
    }

    public void process(List<Object> fieldsData, int k, List<Integer> indices) throws HiveException {
        if (k >= indices.size()) {
            forward(forwardListObj);
            return;
        }
        int i = indices.get(k);
        Object o = fieldsData.get(i);
        if (o != null) {
            if (ArrayUtil.isNDArray(o)) {
                for (int j = 0; j < ArrayUtil.size0(o); j++) {
                    Object v = ArrayUtil.get(o, j);
                    if (v != null) {
                        forwardListObj[i] = v;
                        process(fieldsData, k + 1, indices);
                        forwardListObj[i] = null;
                    }
                }
            } else {
                forwardListObj[i] = o;
                process(fieldsData, k + 1, indices);
                forwardListObj[i] = null;
            }
        }
    }

    public void process(List<Object> fieldsData, String groupingSet) throws HiveException {
        Matcher m = PT.matcher(groupingSet);
        List<String> fields = structInspector.getAllStructFieldRefs().stream().map(StructField::getFieldName).collect(Collectors.toList());
        while (m.find()) {
            String set = StringUtil.strip(m.group(0), "[()]");
            List<Integer> indices;
            if (set.isEmpty()) {
                indices = Collections.emptyList();
            } else {
                indices = Arrays.stream(set.split(",")).map(StringUtils::trim).map(fields::indexOf).sorted().collect(Collectors.toList());
            }
            process(fieldsData, 0, indices);
        }
    }

    @Override
    public void process(Object[] args) throws HiveException {
        List<Object> fieldsData = structInspector.getStructFieldsDataAsList(args[0]);
        forwardListObj = new Object[fieldsData.size()];
        if (stringInspector != null) {
            process(fieldsData, stringInspector.getPrimitiveJavaObject(args[1]));
        } else {
            process(fieldsData, 0);
        }
    }

    @Override
    public void close() throws HiveException {

    }
}
