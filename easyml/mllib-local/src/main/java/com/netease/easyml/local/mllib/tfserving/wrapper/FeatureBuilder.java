package com.netease.easyml.local.mllib.tfserving.wrapper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;
import com.netease.easyml.local.mllib.tfserving.config.ModelBaseConfig;
import org.tensorflow.example.BytesList;
import org.tensorflow.example.Feature;
import org.tensorflow.example.FloatList;
import org.tensorflow.example.Int64List;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author hejiecheng
 * @Date 2020-04-13
 */
public class FeatureBuilder {

    protected static final String ID_SUFIX = "_ids";
    protected static final String VALUE_SUFIX = "_values";
    protected static final String SESSION_SIZE_KEY = "__session_size_";

    protected Feature.Builder[] toKVFeature(Optional<Map<String, Double>> val) {
        BytesList.Builder idFeatureBuilder = BytesList.newBuilder();
        FloatList.Builder valueFeatureBuilder = FloatList.newBuilder();
        Map<String, Double> mapVal = val.orElse(Maps.newHashMap());
        mapVal.entrySet().stream()
                .filter(x -> x.getKey() != null)
                .filter(x -> x.getValue() != null)
                .forEach(x -> {
                    idFeatureBuilder.addValue(ByteString.copyFromUtf8(x.getKey()));
                    valueFeatureBuilder.addValue(x.getValue().floatValue());
                });
        return new Feature.Builder[]{Feature.newBuilder().setBytesList(idFeatureBuilder), Feature.newBuilder().setFloatList(valueFeatureBuilder)};
    }

    protected Feature.Builder toListStringFeature(Optional<List<String>> val) {
        List<String> listVal = val.orElse(Lists.newArrayList());
        if (listVal.isEmpty()) {
            listVal = Arrays.asList("");
        }
        BytesList.Builder byteList = BytesList.newBuilder();
        listVal.forEach(v -> byteList.addValue(ByteString.copyFromUtf8(v)));
        return Feature.newBuilder().setBytesList(byteList);
    }

    protected Feature.Builder toListFloatFeature(Optional<List<Number>> val, ModelBaseConfig.FeatureConfig config) {
        Stream<Number> listVal = val.filter(x -> !x.isEmpty()).map(l -> l.stream()).orElse(IntStream.range(0, config.getDim()).mapToObj(x -> 0.0));
        FloatList.Builder floatList = FloatList.newBuilder();
        listVal.forEach(v -> floatList.addValue(v.floatValue()));
        return Feature.newBuilder().setFloatList(floatList);
    }

    protected Feature.Builder toStringFeature(Optional<String> val) {
        String strVal = val.orElse("");
        return Feature.newBuilder().setBytesList(BytesList.newBuilder().addValue(ByteString.copyFromUtf8(strVal)));
    }

    protected Feature.Builder toFloatFeature(Optional<Number> val) {
        Double doubleVal = val.map(x -> x.doubleValue()).orElse(0.0);
        //TODO: norm support
        return Feature.newBuilder().setFloatList(FloatList.newBuilder().addValue(doubleVal.floatValue()));
    }

    protected Feature.Builder toIntFeature(Optional<Number> val) {
        Integer intVal = val.orElse(0).intValue();
        return Feature.newBuilder().setInt64List(Int64List.newBuilder().addValue(intVal));
    }

}
