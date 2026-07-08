package com.netease.easyml.ndarray.util;

import ai.djl.engine.Engine;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import com.netease.easyml.common.util.ArrayUtil;
import com.netease.easyml.common.util.IOUtil;
import com.netease.easyml.common.util.SparkUtil;
import org.apache.spark.SparkConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by linjiuning on 2020/7/26.
 */
public class NDArrayUtil {
    private static final Logger log = LoggerFactory.getLogger(NDArrayUtil.class);

    public static final String DJL_ENGINE_MXNET = "MXNet";
    public static final String DJL_ENGINE_PYTORCH = "PyTorch";

    public static final String DJL_ENGINE_CACHE_DIR_KEY = "ENGINE_CACHE_DIR";
    public static final String DJL_ENGINE_CACHE_DIR = "ai.djl.engine_cache_dir";
    public static final String DJL_DEFAULT_ENGINE = "ai.djl.default_engine";

    private static NDManager MANGER = null;

    public static Class<?> javaClass(DataType type) {
        switch (type) {
            case UINT8:
            case INT32:
                return int.class;
            case INT8:
                return byte.class;
            case INT64:
                return long.class;
            case BOOLEAN:
                return boolean.class;
            case FLOAT16:
                throw new UnsupportedOperationException("Not implemented");
            case FLOAT32:
                return float.class;
            case FLOAT64:
                return double.class;
            case UNKNOWN:
                return Object.class;
        }
        throw new UnsupportedOperationException("Not implemented");
    }

    public static DataType dataType(Class<?> type) {
        if (int.class.equals(type)) {
            return DataType.INT32;
        } else if (byte.class.equals(type)) {
            return DataType.INT8;
        } else if (long.class.equals(type)) {
            return DataType.INT64;
        } else if (boolean.class.equals(type)) {
            return DataType.BOOLEAN;
        } else if (float.class.equals(type)) {
            return DataType.FLOAT32;
        } else if (double.class.equals(type)) {
            return DataType.FLOAT64;
        }
        throw new UnsupportedOperationException("Not implemented");
    }

    public static Object toJavaArray(NDArray array) {
        DataType dtype = array.getDataType();
        Object nativeArray = null;
        switch (dtype) {
            case UINT8:
                nativeArray = array.toUint8Array();
                break;
            case INT8:
                nativeArray = array.toByteArray();
                break;
            case INT32:
                nativeArray = array.toIntArray();
                break;
            case INT64:
                nativeArray = array.toLongArray();
                break;
            case BOOLEAN:
                nativeArray = array.toBooleanArray();
                break;
            case FLOAT32:
                nativeArray = array.toFloatArray();
                break;
            case FLOAT64:
                nativeArray = array.toDoubleArray();
                break;
            case UNKNOWN:
                throw new UnsupportedOperationException("Not implemented");
        }
        return nativeArray;
    }

    public static Object toJavaNDArray(NDArray array) {
        DataType dtype = array.getDataType();
        long[] shape = array.getShape().getShape();
        Class<?> typeClass = javaClass(dtype);
        Object nativeArray = ArrayUtil.zeros(typeClass, shape);
        ByteBuffer buffer = array.toByteBuffer();

        switch (dtype) {
            case UINT8:
                ArrayUtil.map(nativeArray, (ignore) -> buffer.get() & 0xff);
                break;
            case INT8:
                ArrayUtil.map(nativeArray, (ignore) -> buffer.get());
                break;
            case INT32:
                ArrayUtil.map(nativeArray, (ignore) -> buffer.getInt());
                break;
            case INT64:
                ArrayUtil.map(nativeArray, (ignore) -> buffer.getLong());
                break;
            case BOOLEAN:
                ArrayUtil.map(nativeArray, (ignore) -> buffer.get() != 0);
                break;
            case FLOAT32:
                ArrayUtil.map(nativeArray, (ignore) -> buffer.getFloat());
                break;
            case FLOAT64:
                ArrayUtil.map(nativeArray, (ignore) -> buffer.getDouble());
                break;
            case UNKNOWN:
                throw new UnsupportedOperationException("Not implemented");
        }
        return nativeArray;
    }

    public static NDArray toNDArray(Object array) {
        NDManager ndManager = getNDManager();
        DataType dtype = dataType(ArrayUtil.componentType(array));
        long[] shape_ = ArrayUtil.shapeAsLong(array);
        Shape shape = new Shape(shape_);
        return toNDArray(ndManager, array, shape, dtype);
    }

    public static NDArray toNDArray(NDManager manager, Object array, Shape shape, DataType dataType) {
        array = ArrayUtil.flatten(array);

        NDArray ndarray = null;
        switch (dataType) {
            case INT8:
                ndarray = manager.create((byte[]) array, shape);
                break;
            case INT32:
                ndarray = manager.create((int[]) array, shape);
                break;
            case INT64:
                ndarray = manager.create((long[]) array, shape);
                break;
            case BOOLEAN:
                ndarray = manager.create((boolean[]) array, shape);
                break;
            case FLOAT32:
                ndarray = manager.create((float[]) array, shape);
                break;
            case FLOAT64:
                ndarray = manager.create((double[]) array, shape);
                break;
            case UNKNOWN:
                throw new UnsupportedOperationException("Not implemented");
        }
        return ndarray;
    }

    public synchronized static void setUp(SparkConf conf) {
        if (MANGER == null) {
            try {
                String cacheDir = "";
                if (conf.contains(DJL_ENGINE_CACHE_DIR)) {
                    cacheDir = conf.get(DJL_ENGINE_CACHE_DIR);
                } else if (!SparkUtil.isLocalMaster(conf)) {
                    File tempPath = IOUtil.createTemporaryDirectory();
                    tempPath.deleteOnExit();
                    cacheDir = tempPath.getCanonicalPath();
                }
                if (!cacheDir.isEmpty()) {
                    log.info("set cache dir =" + cacheDir);
                    System.setProperty(DJL_ENGINE_CACHE_DIR_KEY, cacheDir);
                }
                if (conf.contains(DJL_DEFAULT_ENGINE)) {
                    System.setProperty(DJL_DEFAULT_ENGINE, conf.get(DJL_DEFAULT_ENGINE));
                }
            } catch (IOException ignored) {
            }
        }
    }

    public static NDManager getNDManager() {
        if (MANGER == null) {
            synchronized (NDArrayUtil.class) {
                if (MANGER == null) {
                    MANGER = Engine.getInstance().newBaseManager();
                }
            }
        }
        return MANGER;
    }

    public synchronized static void close() {
        if (MANGER != null) {
            MANGER.close();
        }
    }
}
