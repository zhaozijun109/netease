package com.netease.easyml.local.mllib.tfserving.client;

import com.netease.easyml.common.util.ArrayUtil;
import com.netease.easyml.local.mllib.tfserving.config.ModelBaseConfig;
import lombok.extern.slf4j.Slf4j;
import org.tensorflow.framework.TensorProto;
import org.tensorflow.framework.TensorShapeProto;
import tensorflow.serving.Model;
import tensorflow.serving.Predict;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.netease.easyml.common.util.CollectionUtil.map;

/**
 * @author hejiecheng
 * @Date 2020-04-13
 */
@Slf4j
public class ClientWrapper {

    protected ModelBaseConfig config;

    public ClientWrapper(ModelBaseConfig config) {
        this.config = config;
    }

    public List<Object> getScores(TensorProto proto) {
        List<Integer> dims = proto.getTensorShape().getDimList().stream()
                .map(it -> (int) it.getSize()).collect(Collectors.toList());
        while (!dims.isEmpty() && dims.get(dims.size() - 1) == 1) {
            dims.remove(dims.size() - 1);
        }
        int[] shape = new int[dims.size()];
        for (int i = 0; i < dims.size(); i++) {
            shape[i] = dims.get(i);
        }
        //TODO: based on dtype
        List<Float> values = proto.getFloatValList();

        Object obj = ArrayUtil.reshape1D(values, shape);
        List<Object> result = new ArrayList<>();
        for (int i = 0; i < ArrayUtil.size0(obj); i++) {
            result.add(ArrayUtil.get(obj, i));
        }
        return result;
    }

    public List<Object> getScores(Predict.PredictResponse response) {
        List<Object> scores = getScores(response.getOutputsMap().get(config.getTfOutputLayer()));
        return scores;
    }

    public Map<String, List<Object>> getAllScores(Predict.PredictResponse response) {
        Map<String, List<Object>> allScores = new HashMap<>();
        for (Map.Entry<String, TensorProto> entry : response.getOutputsMap().entrySet()) {
            List<Object> scores = getScores(entry.getValue());
            allScores.put(entry.getKey(), scores);
        }
        return allScores;
    }

    protected Predict.PredictRequest prepareRequest(TensorProto proto) {
        return Predict.PredictRequest.newBuilder()
                .setModelSpec(Model.ModelSpec.newBuilder()
                        .setName(config.getModelName())
                        .setSignatureName(config.getSignatureName()))
                .putAllInputs(map(config.getTfInputLayer(), proto))
                .build();
    }

}
