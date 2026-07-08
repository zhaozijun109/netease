package com.netease.easyml.common.util;

import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tensorflow.ConcreteFunction;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Signature;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.*;
import org.tensorflow.proto.example.Example;
import org.tensorflow.proto.example.SequenceExample;
import org.tensorflow.proto.framework.*;
import org.tensorflow.types.*;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.netease.easyml.common.util.CollectionUtil.map;


/**
 * Created by linjiuning on 2021/2/6.
 */
public class TensorFlowUtil {
    private static final Logger log = LoggerFactory.getLogger(TensorFlowUtil.class);

    public static final String DEFAULT_TAGS = SavedModelBundle.DEFAULT_TAG;
    public static final String DEFAULT_FUNCTION = Signature.DEFAULT_KEY;

    public static final String DATATYPE_PREFIX = "DT_";

    private static final Map<String, ObjectSetter> OBJECT_SETTERS = map(
            TFloat32.NAME, new ObjectSetter() {
                @Override
                public void setObject(NdArray<?> ndArray, Object object, long[] coordinates) {
                    object = ConvertUtil.toFloat(object);
                    ((FloatNdArray) ndArray).setObject((Float) object, coordinates);
                }

                @Override
                public NdArray<?> asNdArray(Shape shape) {
                    return NdArrays.ofFloats(shape);
                }

                @Override
                public Tensor<?> asTensor(NdArray<?> ndArray) {
                    return TFloat32.tensorOf((FloatNdArray) ndArray);
                }

                @Override
                public Class<?> componentType() {
                    return float.class;
                }
            },
            TFloat64.NAME, new ObjectSetter() {
                @Override
                public void setObject(NdArray<?> ndArray, Object object, long[] coordinates) {
                    object = ConvertUtil.toDouble(object);
                    ((DoubleNdArray) ndArray).setObject((Double) object, coordinates);
                }

                @Override
                public NdArray<?> asNdArray(Shape shape) {
                    return NdArrays.ofDoubles(shape);
                }

                @Override
                public Tensor<?> asTensor(NdArray<?> ndArray) {
                    return TFloat64.tensorOf((DoubleNdArray) ndArray);
                }

                @Override
                public Class<?> componentType() {
                    return double.class;
                }
            },
            TInt32.NAME, new ObjectSetter() {
                @Override
                public void setObject(NdArray<?> ndArray, Object object, long[] coordinates) {
                    object = ConvertUtil.toInt(object);
                    ((IntNdArray) ndArray).setObject((Integer) object, coordinates);
                }

                @Override
                public NdArray<?> asNdArray(Shape shape) {
                    return NdArrays.ofInts(shape);
                }

                @Override
                public Tensor<?> asTensor(NdArray<?> ndArray) {
                    return TInt32.tensorOf((IntNdArray) ndArray);
                }

                @Override
                public Class<?> componentType() {
                    return int.class;
                }
            },
            TInt64.NAME, new ObjectSetter() {
                @Override
                public void setObject(NdArray<?> ndArray, Object object, long[] coordinates) {
                    object = ConvertUtil.toLong(object);
                    ((LongNdArray) ndArray).setObject((Long) object, coordinates);
                }

                @Override
                public NdArray<?> asNdArray(Shape shape) {
                    return NdArrays.ofLongs(shape);
                }

                @Override
                public Tensor<?> asTensor(NdArray<?> ndArray) {
                    return TInt64.tensorOf((LongNdArray) ndArray);
                }

                @Override
                public Class<?> componentType() {
                    return long.class;
                }
            },
//            TString.NAME, new ObjectSetter() {
//                @Override
//                public void setObject(NdArray<?> ndArray, Object object, long[] coordinates) {
//                    object = ConvertUtil.toString(object);
//                    ((TString) ndArray).setObject((String) object, coordinates);
//                }
//
//                @Override
//                public NdArray<?> asNdArray(Shape shape) {
//                    return NdArrays.ofObjects(String.class, shape);
//                }
//
//                @Override
//                public Tensor<?> asTensor(NdArray<?> ndArray) {
//                    return TString.tensorOf((TString) ndArray);
//                }
//
//                @Override
//                public Class<?> componentType() {
//                    return String.class;
//                }
//            }
            TString.NAME, new ObjectSetter() {
                @Override
                public void setObject(NdArray<?> ndArray, Object object, long[] coordinates) {
                    byte[] bytes;
                    if (object instanceof Example) {
                        bytes = ((Example) object).toByteArray();
                    } else if (object instanceof SequenceExample) {
                        bytes = ((SequenceExample) object).toByteArray();
                    } else if (object instanceof String) {
                        bytes = ((String) object).getBytes(StandardCharsets.UTF_8);
                    } else {
                        bytes = (byte[]) object;
                    }
                    ((NdArray<byte[]>) ndArray).setObject(bytes, coordinates);
                }

                @Override
                public NdArray<?> asNdArray(Shape shape) {
                    return NdArrays.ofObjects(byte[].class, shape);
                }

                @Override
                public Tensor<?> asTensor(NdArray<?> ndArray) {
                    return TString.tensorOfBytes((NdArray<byte[]>) ndArray);
                }

                @Override
                public Class<?> componentType() {
                    return String.class;
                }
            }
    );

    public interface ObjectSetter {

        void setObject(NdArray<?> ndArray, Object object, long[] coordinates);

        NdArray<?> asNdArray(Shape shape);

        Tensor<?> asTensor(NdArray<?> ndArray);

        Class<?> componentType();

        default Object asJava(Tensor<?> tensor) {
            long[] shapes = tensor.shape().asArray();
            Class<?> clazz = componentType();
            return ArrayUtil.zeros(clazz, shapes);
        }
    }

    public static RunOptions sillyRunOptions() {
        return RunOptions.newBuilder()
                .setTraceLevel(RunOptions.TraceLevel.FULL_TRACE)
                .build();
    }

    public static ConfigProto sillyConfigProto() {
        return ConfigProto.newBuilder()
                .setInterOpParallelismThreads(1)
                .setIntraOpParallelismThreads(1)
                .build();
    }

    public static SavedModelBundle load(String exportDir) {
        return SavedModelBundle.loader(exportDir)
                .withTags(DEFAULT_TAGS)
                .withRunOptions(sillyRunOptions())
                .withConfigProto(sillyConfigProto())
                .load();
    }

    public static ConcreteFunction function(SavedModelBundle bundle) {
        return function(bundle, DEFAULT_FUNCTION);
    }

    public static ConcreteFunction function(SavedModelBundle bundle, String signatureKey) {
        return bundle.function(signatureKey);
    }

    public static SignatureDef asSignatureDef(Signature signature) {
        try {
            Method method = signature.getClass().getDeclaredMethod("asSignatureDef");
            method.setAccessible(true);
            return (SignatureDef) method.invoke(signature);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public static Map<String, org.tensorflow.DataType<?>> getOutputDataTypes(SignatureDef signatureDef) {
        Map<String, TensorInfo> outputInfos = signatureDef.getOutputsMap();
        Map<String, org.tensorflow.DataType<?>> dataTypes = new HashMap<>();
        for (Map.Entry<String, TensorInfo> entry : outputInfos.entrySet()) {
            String key = entry.getKey();
            org.tensorflow.DataType<?> dtype = getDataType(entry.getValue());
            dataTypes.put(key, dtype);
        }
        return dataTypes;
    }

    public static Map<String, org.tensorflow.DataType<?>> getOutputDataTypes(ConcreteFunction function) {
        SignatureDef signatureDef = asSignatureDef(function.signature());
        return getOutputDataTypes(signatureDef);
    }

    public static Map<String, Shape> getOutputShapes(SignatureDef signatureDef) {
        Map<String, TensorInfo> outputInfos = signatureDef.getOutputsMap();
        Map<String, Shape> dataTypes = new HashMap<>();
        for (Map.Entry<String, TensorInfo> entry : outputInfos.entrySet()) {
            String key = entry.getKey();
            long[] shape = getShape(entry.getValue());
            dataTypes.put(key, Shape.of(shape));
        }
        return dataTypes;
    }

    public static Map<String, Shape> getOutputShapes(ConcreteFunction function) {
        SignatureDef signatureDef = asSignatureDef(function.signature());
        return getOutputShapes(signatureDef);
    }

    public static long[] getShape(TensorInfo tensorInfo) {
        TensorShapeProto shape = tensorInfo.getTensorShape();
        List<TensorShapeProto.Dim> dimList = shape.getDimList();
        long[] dims = new long[dimList.size()];
        for (int i = 0; i < dimList.size(); i++) {
            dims[i] = dimList.get(i).getSize();
        }
        return dims;
    }

    public static org.tensorflow.DataType<?> getDataType(TensorInfo tensorInfo) {
        DataType dtype = tensorInfo.getDtype();
        String name = dtype.name();
        if (name.startsWith(DATATYPE_PREFIX)) {
            name = name.substring(DATATYPE_PREFIX.length());
        }
        return org.tensorflow.DataType.of(name);
    }

    public static ObjectSetter getObjectSetter(org.tensorflow.DataType<?> dataType) {
        String name = dataType.name();
        return OBJECT_SETTERS.get(name);
    }

    public static void set(ObjectSetter setter, NdArray<?> ndArray, Object array) {
        set(setter, ndArray, array, new long[0]);
    }

    public static void set(ObjectSetter setter, NdArray<?> ndArray, Object array, long[] coordinates) {
        if (ndArray.shape().numDimensions() == coordinates.length) {
            setter.setObject(ndArray, array, coordinates);
        } else {
            int size = ArrayUtil.size0(array);
            long[] newCoor = new long[coordinates.length + 1];
            System.arraycopy(coordinates, 0, newCoor, 0, coordinates.length);
            for (int i = 0; i < size; i++) {
                newCoor[newCoor.length - 1] = i;
                set(setter, ndArray, ArrayUtil.get(array, i), newCoor);
            }
        }
    }

    public static Object set(ObjectSetter setter, Tensor<?> tensor) {
        Object array = setter.asJava(tensor);
        NdArray<?> ndArray = (NdArray<?>) tensor.data();
        set(array, ndArray, new long[0]);
        return array;
    }

    public static void set(Object array, NdArray<?> ndArray) {
        set(array, ndArray, new long[0]);
    }

    public static void set(Object array, NdArray<?> ndArray, long[] coordinates) {
        int dim = ArrayUtil.dim(array);
        if (dim == 0) {
            return;
        }
        if (dim == 1) {
            int size = ArrayUtil.size0(array);
            long[] newCoor = new long[coordinates.length + 1];
            System.arraycopy(coordinates, 0, newCoor, 0, coordinates.length);
            for (int i = 0; i < size; i++) {
                newCoor[newCoor.length - 1] = i;
                Array.set(array, i, ndArray.getObject(newCoor));
            }
        } else {
            int size = ArrayUtil.size0(array);
            long[] newCoor = new long[coordinates.length + 1];
            System.arraycopy(coordinates, 0, newCoor, 0, coordinates.length);
            for (int i = 0; i < size; i++) {
                newCoor[newCoor.length - 1] = i;
                set(ArrayUtil.get(array, i), ndArray, newCoor);
            }
        }
    }

    public static Map<String, Tensor<?>> args(SignatureDef signatureDef, Map<String, List<Object>> inputs) {
        Map<String, TensorInfo> inputsMap = signatureDef.getInputsMap();
        Map<String, long[]> shapes = new HashMap<>();
        Map<String, Tensor<?>> tensorMap = new HashMap<>();

        // get data shapes
        for (String key : inputsMap.keySet()) {
            if (!inputs.containsKey(key)) {
                log.warn(String.format("Input args missing key %s", key));
                continue;
            }
            List<Object> list = inputs.get(key);
            for (Object o : list) {
                if (o == null) {
                    continue;
                }
                long[] oShape = ArrayUtil.shapeAsLong(o);
                if (!shapes.containsKey(key)) {
                    shapes.put(key, oShape);
                } else {
                    long[] pShape = shapes.get(key);
                    if (pShape.length != oShape.length) {
                        throw new IllegalArgumentException(String.format("Dimension of key %s must be equal, but get %d and %d instead", key, pShape.length, oShape.length));
                    }
                    for (int i = 0; i < pShape.length; i++) {
                        pShape[i] = Math.max(pShape[i], oShape[i]);
                    }
                }
            }
        }

        // initialize tensor
        for (String key : inputsMap.keySet()) {
            if (!inputs.containsKey(key)) {
                continue;
            }
            int dim0 = inputs.get(key).size();
            TensorInfo tensorInfo = inputsMap.get(key);
            long[] defDims = getShape(tensorInfo);
            long[] dataDims = shapes.get(key);
            defDims[0] = dim0;
            for (int i = 0; i < dataDims.length; i++) {
                if (i + 1 < defDims.length && defDims[i + 1] == -1) {
                    defDims[i + 1] = dataDims[i];
                }
            }
            org.tensorflow.DataType<?> dataType = getDataType(tensorInfo);
            ObjectSetter setter = getObjectSetter(dataType);
            Shape shape = Shape.of(defDims);
            NdArray<?> ndArray = setter.asNdArray(shape);
            set(setter, ndArray, inputs.get(key));
            Tensor<?> tensor = setter.asTensor(ndArray);
            tensorMap.put(key, tensor);
        }

        return tensorMap;
    }

    public static Map<String, List<Object>> call(ConcreteFunction function, Map<String, List<Object>> inputs) {
        SignatureDef signatureDef = asSignatureDef(function.signature());
        Map<String, Tensor<?>> args = args(signatureDef, inputs);
        Map<String, Tensor<?>> outputTensors = null;
        try {
            Map<String, List<Object>> outputs = new HashMap<>();
            outputTensors = function.call(args);
            for (Map.Entry<String, Tensor<?>> entry : outputTensors.entrySet()) {
                Tensor<?> tensor = entry.getValue();
                ObjectSetter setter = getObjectSetter(tensor.dataType());
                Object array = set(setter, tensor);
                List<Object> lists = new ArrayList<>();
                for (int i = 0; i < ArrayUtil.size0(array); i++) {
                    lists.add(ArrayUtil.get(array, i));
                }
                outputs.put(entry.getKey(), lists);
            }
            return outputs;
        } finally {
            for (Tensor<?> value : args.values()) {
                if (value != null) {
                    value.close();
                }
            }
            if (outputTensors != null) {
                for (Tensor<?> value : outputTensors.values()) {
                    value.close();
                }
            }
        }
    }

    public static TensorProto gzipTensorProto(TensorProto proto) {
        TensorProto.Builder builder = TensorProto.newBuilder()
                .setTensorShape(proto.getTensorShape())
                .setDtype(proto.getDtype());
        for (ByteString bytes : proto.getStringValList()) {
            byte[] nbytes = IOUtil.gzip(bytes.toByteArray());
            builder.addStringVal(ByteString.copyFrom(nbytes));
        }
        return builder.build();
    }
}
