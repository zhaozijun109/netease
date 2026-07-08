package com.netease.easyml.common.util;

import org.junit.Test;
import org.tensorflow.ConcreteFunction;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;
import org.tensorflow.proto.framework.SignatureDef;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linjiuning on 2021/2/7.
 */
public class TensorFlowUtilTest {
    private static final String PY_PATH = "/Users/linjiuning/workspace/git/netease/easyml_test/target/cross_domain/model";

    public Map<String, List<Object>> dummyInputs() {
        Map<String, List<Object>> inputs = new HashMap<>();
        inputs.put("age", Arrays.asList(
                1, 0
        ));
        inputs.put("gender", Arrays.asList(1, 0));
        return inputs;
    }

    @Test
    public void call() {
        SavedModelBundle bundle = TensorFlowUtil.load(PY_PATH);
        ConcreteFunction function = TensorFlowUtil.function(bundle);
        SignatureDef signatureDef = TensorFlowUtil.asSignatureDef(function.signature());
        Map<String, Tensor<?>> args = TensorFlowUtil.args(signatureDef, dummyInputs());
        Map<String, Tensor<?>> outputs = function.call(args);
        System.out.println(outputs);
        Map<String, List<Object>> result = TensorFlowUtil.call(function, dummyInputs());
        for (Map.Entry<String, List<Object>> entry : result.entrySet()) {
            System.out.println("KEY: " + entry.getKey());
            for (Object o : entry.getValue()) {
                System.out.println(StringUtil.join((float[]) o, ","));
            }
        }
    }
}