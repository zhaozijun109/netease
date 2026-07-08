package com.netease.easyudf.udf.collect;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.*;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector.Category;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector.PrimitiveCategory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class EmptyUDF extends GenericUDF {
    private InspectorHandle inspHandle;


    private interface InspectorHandle {
        boolean call(Object obj) throws IOException;
    }


    private class MapInspectorHandle implements InspectorHandle {
        private MapObjectInspector mapInspector;
        private StringObjectInspector keyObjectInspector;
        private InspectorHandle valueInspector;


        public MapInspectorHandle(MapObjectInspector mInsp) throws UDFArgumentException {
            mapInspector = mInsp;
            try {
                keyObjectInspector = (StringObjectInspector) mInsp.getMapKeyObjectInspector();
            } catch (ClassCastException castExc) {
                throw new UDFArgumentException("Only Maps with strings as keys can be converted to valid JSON");
            }
            valueInspector = GenerateInspectorHandle(mInsp.getMapValueObjectInspector());
        }

        @Override
        public boolean call(Object obj) throws IOException {
            if (obj == null) {
                return true;
            } else {
                Map map = mapInspector.getMap(obj);
                for (Map.Entry entry : (Iterable<Map.Entry>) map.entrySet()) {
                    if (!valueInspector.call(entry.getValue())) {
                        return false;
                    }
                }
                return true;
            }
        }

    }


    private class StructInspectorHandle implements InspectorHandle {
        private StructObjectInspector structInspector;
        private List<String> fieldNames;
        private List<InspectorHandle> fieldInspectorHandles;

        public StructInspectorHandle(StructObjectInspector insp) throws UDFArgumentException {
            structInspector = insp;
            List<? extends StructField> fieldList = insp.getAllStructFieldRefs();
            this.fieldNames = new ArrayList<>();
            this.fieldInspectorHandles = new ArrayList<>();
            for (StructField sf : fieldList) {
                fieldNames.add(sf.getFieldName());
                fieldInspectorHandles.add(GenerateInspectorHandle(sf.getFieldObjectInspector()));
            }
        }

        @Override
        public boolean call(Object obj) throws IOException {
            //// Interpret a struct as a map ...
            if (obj == null) {
                return true;
            } else {
                List structObjs = structInspector.getStructFieldsDataAsList(obj);
                for (int i = 0; i < fieldNames.size(); ++i) {
                    if (!fieldInspectorHandles.get(i).call(structObjs.get(i))) {
                        return false;
                    }
                }
                return true;
            }
        }

    }


    private class ArrayInspectorHandle implements InspectorHandle {
        private ListObjectInspector arrayInspector;
        private InspectorHandle valueInspector;


        public ArrayInspectorHandle(ListObjectInspector lInsp) throws UDFArgumentException {
            arrayInspector = lInsp;
            valueInspector = GenerateInspectorHandle(arrayInspector.getListElementObjectInspector());
        }

        @Override
        public boolean call(Object obj) throws IOException {
            if (obj == null) {
                return true;
            } else {
                List list = arrayInspector.getList(obj);
                for (Object listObj : list) {
                    if (!valueInspector.call(listObj)) {
                        return false;
                    }
                }
                return true;
            }
        }

    }

    private class StringInspectorHandle implements InspectorHandle {
        private StringObjectInspector strInspector;


        public StringInspectorHandle(StringObjectInspector insp) {
            strInspector = insp;
        }

        @Override
        public boolean call(Object obj) throws IOException {
            if (obj == null) {
                return true;
            } else {
                String str = strInspector.getPrimitiveJavaObject(obj);
                return "".equals(str);
            }
        }

    }

    private class IntInspectorHandle implements InspectorHandle {
        private IntObjectInspector intInspector;

        public IntInspectorHandle(IntObjectInspector insp) {
            intInspector = insp;
        }

        @Override
        public boolean call(Object obj) throws IOException {
            if (obj == null) return true;
            else {
                int num = intInspector.get(obj);
                return num == 0;
            }
        }
    }

    private class DoubleInspectorHandle implements InspectorHandle {
        private DoubleObjectInspector dblInspector;

        public DoubleInspectorHandle(DoubleObjectInspector insp) {
            dblInspector = insp;
        }

        @Override
        public boolean call(Object obj) throws IOException {
            if (obj == null) {
                return true;
            } else {
                double num = dblInspector.get(obj);
                return num == 0;
            }
        }
    }

    private class LongInspectorHandle implements InspectorHandle {
        private LongObjectInspector longInspector;

        public LongInspectorHandle(LongObjectInspector insp) {
            longInspector = insp;
        }

        @Override
        public boolean call(Object obj) throws IOException {
            if (obj == null) {
                return true;
            } else {
                long num = longInspector.get(obj);
                return num == 0;
            }
        }
    }

    private class ShortInspectorHandle implements InspectorHandle {
        private ShortObjectInspector shortInspector;

        public ShortInspectorHandle(ShortObjectInspector insp) {
            shortInspector = insp;
        }

        @Override
        public boolean call(Object obj) throws IOException {
            if (obj == null) {
                return true;
            } else {
                short num = shortInspector.get(obj);
                return num == 0;
            }
        }
    }


    private class ByteInspectorHandle implements InspectorHandle {
        private ByteObjectInspector byteInspector;

        public ByteInspectorHandle(ByteObjectInspector insp) {
            byteInspector = insp;
        }

        @Override
        public boolean call(Object obj) throws IOException {
            if (obj == null) {
                return true;
            } else {
                byte num = byteInspector.get(obj);
                return num == 0;
            }
        }
    }


    private class FloatInspectorHandle implements InspectorHandle {
        private FloatObjectInspector floatInspector;

        public FloatInspectorHandle(FloatObjectInspector insp) {
            floatInspector = insp;
        }

        @Override
        public boolean call(Object obj) throws IOException {
            if (obj == null) {
                return true;
            } else {
                float num = floatInspector.get(obj);
                return num == 0;
            }
        }
    }

    private class BooleanInspectorHandle implements InspectorHandle {
        private BooleanObjectInspector boolInspector;

        public BooleanInspectorHandle(BooleanObjectInspector insp) {
            boolInspector = insp;
        }

        @Override
        public boolean call(Object obj) throws IOException {
            return obj == null;
        }
    }

    private class BinaryInspectorHandle implements InspectorHandle {
        private BinaryObjectInspector binaryInspector;

        public BinaryInspectorHandle(BinaryObjectInspector insp) {
            binaryInspector = insp;
        }

        @Override
        public boolean call(Object obj) throws IOException {
            if (obj == null) {
                return true;
            } else {
                byte[] bytes = binaryInspector.getPrimitiveJavaObject(obj);
                return bytes.length > 0;
            }
        }
    }

    private class TimestampInspectorHandle implements InspectorHandle {
        private TimestampObjectInspector timestampInspector;

        public TimestampInspectorHandle(TimestampObjectInspector insp) {
            timestampInspector = insp;
        }

        @Override
        public boolean call(Object obj) throws IOException {
            return obj == null;
        }
    }


    private InspectorHandle GenerateInspectorHandle(ObjectInspector insp) throws UDFArgumentException {
        Category cat = insp.getCategory();
        if (cat == Category.MAP) {
            return new MapInspectorHandle((MapObjectInspector) insp);
        } else if (cat == Category.LIST) {
            return new ArrayInspectorHandle((ListObjectInspector) insp);
        } else if (cat == Category.STRUCT) {
            return new StructInspectorHandle((StructObjectInspector) insp);
        } else if (cat == Category.PRIMITIVE) {
            PrimitiveObjectInspector primInsp = (PrimitiveObjectInspector) insp;
            PrimitiveCategory primCat = primInsp.getPrimitiveCategory();
            if (primCat == PrimitiveCategory.STRING) {
                return new StringInspectorHandle((StringObjectInspector) primInsp);
            } else if (primCat == PrimitiveCategory.INT) {
                return new IntInspectorHandle((IntObjectInspector) primInsp);
            } else if (primCat == PrimitiveCategory.LONG) {
                return new LongInspectorHandle((LongObjectInspector) primInsp);
            } else if (primCat == PrimitiveCategory.SHORT) {
                return new ShortInspectorHandle((ShortObjectInspector) primInsp);
            } else if (primCat == PrimitiveCategory.BOOLEAN) {
                return new BooleanInspectorHandle((BooleanObjectInspector) primInsp);
            } else if (primCat == PrimitiveCategory.FLOAT) {
                return new FloatInspectorHandle((FloatObjectInspector) primInsp);
            } else if (primCat == PrimitiveCategory.DOUBLE) {
                return new DoubleInspectorHandle((DoubleObjectInspector) primInsp);
            } else if (primCat == PrimitiveCategory.BYTE) {
                return new ByteInspectorHandle((ByteObjectInspector) primInsp);
            } else if (primCat == PrimitiveCategory.BINARY) {
                return new BinaryInspectorHandle((BinaryObjectInspector) primInsp);
            } else if (primCat == PrimitiveCategory.TIMESTAMP) {
                return new TimestampInspectorHandle((TimestampObjectInspector) primInsp);
            }


        }
        /// Dunno ...
        throw new UDFArgumentException("Don't know how to handle object inspector " + insp);
    }


    @Override
    public Object evaluate(DeferredObject[] args) throws HiveException {
        try {
            return inspHandle.call(args[0].get());
        } catch (IOException io) {
            throw new HiveException(io);
        }

    }

    @Override
    public String getDisplayString(String[] args) {
        return "empty(" + args[0] + ")";
    }

    @Override
    public ObjectInspector initialize(ObjectInspector[] args) throws UDFArgumentException {
        if (args.length != 1) {
            throw new UDFArgumentException(" empty takes an object as an argument.");
        }
        ObjectInspector oi = args[0];
        inspHandle = GenerateInspectorHandle(oi);

        return PrimitiveObjectInspectorFactory.javaBooleanObjectInspector;
    }
}