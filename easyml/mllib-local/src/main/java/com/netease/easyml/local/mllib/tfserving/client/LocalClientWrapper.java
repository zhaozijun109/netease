package com.netease.easyml.local.mllib.tfserving.client;


import com.google.protobuf.ByteString;
import com.netease.easyml.common.util.ArrayUtil;
import com.netease.easyml.common.util.TensorFlowUtil;
import com.netease.easyml.local.mllib.tfserving.config.ModelBaseConfig;
import lombok.extern.slf4j.Slf4j;
import org.tensorflow.ConcreteFunction;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.framework.TensorProto;
import org.tensorflow.proto.framework.ConfigProto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by linjiuning on 2021/10/14.
 */
@Slf4j
public class LocalClientWrapper {

    protected ModelBaseConfig config;

    private final ConcreteFunction function;

    public LocalClientWrapper(ModelBaseConfig config) {
        this.config = config;
        ConfigProto configProto = ConfigProto.newBuilder()
                .setInterOpParallelismThreads(config.getNumThreads())
                .setIntraOpParallelismThreads(config.getNumThreads())
                .build();
        SavedModelBundle bundle = SavedModelBundle.loader(config.getExportDir()).withTags(TensorFlowUtil.DEFAULT_TAGS)
                .withConfigProto(configProto).load();

        function = TensorFlowUtil.function(bundle, config.getSignatureName());
    }

    public Map<String, List<Object>> request(TensorProto proto) {
        Map<String, List<Object>> inputs = new HashMap<>();
        List<Object> bytes = proto.getStringValList()
                .stream().map(ByteString::toByteArray).collect(Collectors.toList());
        inputs.put(config.getTfInputLayer(), bytes);
        return TensorFlowUtil.call(function, inputs);
    }

    public List<Object> getScores(List<Object> outputs) {
        List<Object> scores = new ArrayList<>();
        for (Object o : outputs) {
            if (ArrayUtil.isArray(o) && ArrayUtil.size0(o) == 1) {
                scores.add(ArrayUtil.get(o, 0));
            } else {
                scores.add(o);
            }
        }
        return scores;
    }

    public List<Object> getScores(Map<String, List<Object>> outputs) {
        return getScores(outputs.get(config.getTfOutputLayer()));
    }

    public Map<String, List<Object>> getAllScores(Map<String, List<Object>> outputs) {
        Map<String, List<Object>> results = new HashMap<>();
        for (Map.Entry<String, List<Object>> entry : outputs.entrySet()) {
            List<Object> scores = getScores(entry.getValue());
            results.put(entry.getKey(), scores);
        }
        return results;
    }

}
