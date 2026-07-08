package com.netease.easyml.common.util;

import com.tencent.miaobinlp.backend.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.netease.easyml.common.util.CollectionUtil.map;


/**
 * Created by linjiuning on 2021/2/18.
 */
public class TorchUtil {
    private static final Logger log = LoggerFactory.getLogger(TorchUtil.class);

    private static final Map<String, ObjectSetter> OBJECT_SETTERS = map(
            "FLOAT", new ObjectSetter() {
                @Override
                public Object value(Object object) {
                    return ConvertUtil.toFloat(object);
                }

                @Override
                public Class<?> componentType() {
                    return float.class;
                }
            },
            "DOUBLE", new ObjectSetter() {
                @Override
                public Object value(Object object) {
                    return ConvertUtil.toDouble(object);
                }

                @Override
                public Class<?> componentType() {
                    return double.class;
                }
            },
            "INT", new ObjectSetter() {
                @Override
                public Object value(Object object) {
                    return ConvertUtil.toInt(object);
                }

                @Override
                public Class<?> componentType() {
                    return int.class;
                }
            },
            "LONG", new ObjectSetter() {
                @Override
                public Object value(Object object) {
                    return ConvertUtil.toLong(object);
                }

                @Override
                public Class<?> componentType() {
                    return long.class;
                }
            }
    );

    public interface ObjectSetter {

        Object value(Object object);

        Class<?> componentType();

        default Object asNdArray(long[] shapes) {
            Class<?> clazz = componentType();
            return ArrayUtil.zeros(clazz, shapes);
        }
    }

    public static ObjectSetter getObjectSetter(String dataType) {
        String name = dataType.toUpperCase();
        return OBJECT_SETTERS.get(name);
    }

    public static void set(ObjectSetter setter, Object tensor, Object array) {
        int dim = ArrayUtil.dim(tensor);
        if (dim == 0) {
            return;
        }
        if (dim == 1) {
            int size = ArrayUtil.size0(array);
            for (int i = 0; i < size; i++) {
                Object value = setter.value(ArrayUtil.get(array, i));
                Array.set(tensor, i, value);
            }
        } else {
            int size = ArrayUtil.size0(tensor);
            for (int i = 0; i < size; i++) {
                set(setter, ArrayUtil.get(tensor, i), ArrayUtil.get(array, i));
            }
        }
    }

    public static Object[] args(String[] dtypes, List<List<Object>> inputs) {
        int numKeys = inputs.size();
        String[] dtypes_ = new String[numKeys];
        List<long[]> shapes = new ArrayList<>();
        Object[] tensors = new Object[numKeys];

        for (int i = 0; i < numKeys; i++) {
            List<Object> list = inputs.get(i);
            if (i >= dtypes.length || dtypes[i] == null) {
                Class<?> componentType = ArrayUtil.componentType(list);
                String dtype = componentType.getSimpleName().toUpperCase();
                dtypes_[i] = dtype;
            } else {
                dtypes_[i] = dtypes[i].toUpperCase();
            }
            shapes.add(null);
            for (Object o : list) {
                if (o == null) {
                    continue;
                }
                long[] oShape = ArrayUtil.shapeAsLong(o);
                if (shapes.get(i) == null) {
                    shapes.set(i, oShape);
                } else {
                    long[] pShape = shapes.get(i);
                    if (pShape.length != oShape.length) {
                        throw new IllegalArgumentException(String.format("Dimension of key %s must be equal, but get %d and %d instead", i, pShape.length, oShape.length));
                    }
                    for (int j = 0; j < pShape.length; j++) {
                        pShape[j] = Math.max(pShape[j], oShape[j]);
                    }
                }
            }
        }

        // initialize tensor
        for (int i = 0; i < numKeys; i++) {
            List<Object> list = inputs.get(i);
            int dim0 = list.size();
            long[] dataDims = shapes.get(i);
            long[] defDims = new long[dataDims.length + 1];
            defDims[0] = dim0;
            System.arraycopy(dataDims, 0, defDims, 1, dataDims.length);
            String dataType = dtypes_[i];
            ObjectSetter setter = getObjectSetter(dataType);

            Object ndArray = setter.asNdArray(defDims);
            set(setter, ndArray, list);
            tensors[i] = ndArray;
        }
        return tensors;
    }

    public static List<List<Object>> call(Graph graph, List<List<Object>> inputs) {
        return call(graph, inputs, new String[0]);
    }

    public static List<List<Object>> call(Graph graph, List<List<Object>> inputs, String[] dtypes) {
        Object[] args = args(dtypes, inputs);
        List<List<Object>> outputs = new ArrayList<>();
        Object[] outputTensors = graph.forward(args);
        for (Object array : outputTensors) {
            List<Object> lists = new ArrayList<>();
            for (int i = 0; i < ArrayUtil.size0(array); i++) {
                lists.add(ArrayUtil.get(array, i));
            }
            outputs.add(lists);
        }
        return outputs;
    }
}
