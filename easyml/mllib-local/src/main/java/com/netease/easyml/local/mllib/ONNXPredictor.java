package com.netease.easyml.local.mllib;

import ai.onnxruntime.*;
import com.netease.easyml.common.collection.Params;
import com.netease.easyml.common.util.ArrayUtil;
import com.netease.easyml.common.util.CollectionUtil;
import com.netease.easyml.common.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.netease.easyml.common.util.ArrayUtil.shape;

/**
 * Created by linjiuning on 2020/7/31.
 */
public class ONNXPredictor implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(ONNXPredictor.class);
    private final OrtEnvironment env;
    private final OrtSession.SessionOptions opts;
    private final OrtSession session;
    private int batchSize;

    private ONNXPredictor(OrtEnvironment env, OrtSession.SessionOptions opts, OrtSession session, int batchSize) {
        this.env = env;
        this.opts = opts;
        this.session = session;
        this.batchSize = batchSize;
    }

    public OrtEnvironment getEnv() {
        return env;
    }

    public OrtSession.SessionOptions getOpts() {
        return opts;
    }

    public OrtSession getSession() {
        return session;
    }

    public ONNXPredictor setBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    private OnnxTensor createTensor(String name, Params param) throws OrtException {
        TensorInfo tensorInfo = (TensorInfo) session.getInputInfo().get(name).getInfo();
        long[] shape = tensorInfo.getShape();
        shape[0] = 1;
        int[] shape_ = new int[shape.length];
        for (int i = 0; i < shape.length; i++) {
            shape_[i] = (int) shape[i];
        }
        Class<?> clazz = tensorInfo.type.clazz;
        Object array = ArrayUtil.zeros(clazz, shape_);

        Object value = param.get(name);
        // handle [None, 1]
        if (ArrayUtil.dim(value) == shape.length - 2) {
            shape_ = shape(value);
            int[] expShape = new int[shape_.length + 1];
            System.arraycopy(shape_, 0, expShape, 0, shape_.length);
            expShape[expShape.length - 1] = 1;
            Object expValue = ArrayUtil.zeros(clazz, expShape);
            ArrayUtil.set(expValue, 0, value);
            ArrayUtil.set(array, 0, expValue);
        } else {
            ArrayUtil.set(array, 0, value);
        }
        return OnnxTensor.createTensor(env, array);
    }

    private OnnxTensor createTensor(String name, List<Params> params) throws OrtException {
        TensorInfo tensorInfo = (TensorInfo) session.getInputInfo().get(name).getInfo();
        long[] shape = tensorInfo.getShape();
        shape[0] = params.size();
        int[] shape_ = new int[shape.length];
        for (int i = 0; i < shape.length; i++) {
            shape_[i] = (int) shape[i];
        }
        Class<?> clazz = tensorInfo.type.clazz;
        Object array = ArrayUtil.zeros(clazz, shape_);
        for (int i = 0; i < params.size(); i++) {
            Params param = params.get(i);
            Object value = param.get(name);
            // handle [None, 1]
            if (ArrayUtil.dim(value) == shape.length - 2) {
                shape_ = shape(value);
                int[] expShape = new int[shape_.length + 1];
                System.arraycopy(shape_, 0, expShape, 0, shape_.length);
                expShape[expShape.length - 1] = 1;
                Object expValue = ArrayUtil.zeros(clazz, expShape);
                ArrayUtil.set(expValue, 0, value);
                ArrayUtil.set(array, i, expValue);
            } else {
                ArrayUtil.set(array, i, value);
            }
        }
        return OnnxTensor.createTensor(env, array);
    }

    public Params predict(Params param) {
        Params output = null;
        Map<String, OnnxTensor> inputs = new HashMap<>();
        OrtSession.Result result = null;
        try {
            for (String name : session.getInputNames()) {
                OnnxTensor tensor = createTensor(name, param);
                inputs.put(name, tensor);
            }
            result = session.run(inputs);

            output = new Params();
            for (String name : session.getOutputNames()) {
                Object value = result.get(name).get().getValue();
                Object o = ArrayUtil.get(value, 0);
                output.put(name, o);
            }
        } catch (OrtException e) {
            log.error("OrtException: " + e.getMessage());
        } finally {
            for (OnnxTensor value : inputs.values()) {
                if (value != null) {
                    value.close();
                }
            }
            if (result != null) {
                result.close();
            }
        }
        return output;
    }

    public List<Params> predictBatch(List<Params> params) {
        List<Params> outputs = new ArrayList<>(params.size());
        for (List<Params> batch : CollectionUtil.groupsOf(params, batchSize)) {
            Map<String, OnnxTensor> inputs = new HashMap<>();
            OrtSession.Result result = null;
            try {
                for (String name : session.getInputNames()) {
                    OnnxTensor tensor = createTensor(name, batch);
                    inputs.put(name, tensor);
                }
                result = session.run(inputs);

                for (int j = 0; j < batch.size(); j++) {
                    Params output = new Params();
                    for (String name : session.getOutputNames()) {
                        Object value = result.get(name).get().getValue();
                        Object o = ArrayUtil.get(value, j);
                        output.put(name, o);
                    }
                    outputs.add(output);
                }
            } catch (OrtException e) {
                log.error("OrtException: " + e.getMessage());
                for (int i = 0; i < batch.size(); i++) {
                    outputs.add(null);
                }
            } finally {
                for (OnnxTensor value : inputs.values()) {
                    if (value != null) {
                        value.close();
                    }
                }
                if (result != null) {
                    result.close();
                }
            }
        }
        return outputs;
    }

    @Override
    public void close() throws Exception {
        if (env != null) {
            env.close();
        }
        try {
            if (opts != null) {
                opts.close();
            }
        } catch (IllegalStateException ignored) {
        }
        try {
            if (session != null) {
                session.close();
            }
        } catch (IllegalStateException ignored) {
        }
    }

    public static ONNXPredictor of(String modelPath) {
        return builder().setModelPath(modelPath).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String modelPath;
        private int batchSize = 1;
        private OrtSession.SessionOptions opts;
        private OrtSession.SessionOptions.OptLevel optLevel = OrtSession.SessionOptions.OptLevel.BASIC_OPT;
        private int numThreads = 1;

        public Builder setModelPath(String modelPath) {
            this.modelPath = modelPath;
            return this;
        }

        public Builder setBatchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public Builder setOpts(OrtSession.SessionOptions opts) {
            this.opts = opts;
            return this;
        }

        public Builder setOptLevel(OrtSession.SessionOptions.OptLevel optLevel) {
            this.optLevel = optLevel;
            return this;
        }

        public Builder setNumThreads(int numThreads) {
            this.numThreads = numThreads;
            return this;
        }

        public ONNXPredictor build() {
            try {
                modelPath = IOUtil.mayCopyHdfsToLocal(modelPath);
                OrtEnvironment env = OrtEnvironment.getEnvironment();
                if (opts == null) {
                    opts = new OrtSession.SessionOptions();
                    opts.setOptimizationLevel(optLevel);
                    opts.setIntraOpNumThreads(numThreads);
                    opts.setInterOpNumThreads(numThreads);
                }
                OrtSession session = env.createSession(modelPath, opts);
                return new ONNXPredictor(env, opts, session, batchSize);
            } catch (OrtException e) {
                log.error("OrtException: " + e.getMessage());
            }
            return null;
        }
    }
}
