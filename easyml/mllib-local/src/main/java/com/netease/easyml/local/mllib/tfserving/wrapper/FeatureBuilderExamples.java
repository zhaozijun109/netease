package com.netease.easyml.local.mllib.tfserving.wrapper;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.netease.easyml.common.util.IOUtil;
import com.netease.easyml.local.mllib.tfserving.config.ModelBaseConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.tensorflow.example.Example;
import org.tensorflow.example.Feature;
import org.tensorflow.example.Features;
import org.tensorflow.example.FloatList;
import org.tensorflow.framework.DataType;
import org.tensorflow.framework.TensorProto;
import org.tensorflow.framework.TensorShapeProto;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

/**
 * @author hejiecheng
 * @Date 2020-04-13
 */
@Slf4j
public class FeatureBuilderExamples extends FeatureBuilder {

    private final Optional<Map<String, Object>> commonFeatures;
    private ModelBaseConfig config;
    private Gson gson = new Gson();

    public FeatureBuilderExamples(ModelBaseConfig config, Optional<Map<String, Object>> commonFeatures) {
        this.config = config;
        this.commonFeatures = config.exampleTypeIsUserOnceExample() ? commonFeatures : commonFeatures.filter(x -> !x.isEmpty());
    }

    public void applyGzip(TensorProto.Builder builder) {
        for (int i = 0; i < builder.getStringValList().size(); i++) {
            ByteString bytes = builder.getStringVal(i);
            byte[] nbytes = IOUtil.gzip(bytes.toByteArray());
            builder.setStringVal(i, ByteString.copyFrom(nbytes));
        }
    }

    public void applyFullGzip(TensorProto.Builder builder) {
        int nElements = builder.getStringValList().size();
        if (nElements < 1) {
            return;
        }

        byte[] sep = config.getCompressionSep().getBytes(StandardCharsets.UTF_8);
        int length = (nElements - 1) * sep.length;
        for (ByteString bytes : builder.getStringValList()) {
            length += bytes.size();
        }

        byte[] mergeBytes = new byte[length];
        int j = 0;
        for (int i = 0; i < nElements; i++) {
            ByteString bytes = builder.getStringVal(i);
            if (i > 0) {
                for (byte b : sep) {
                    mergeBytes[j++] = b;
                }
            }
            for (byte b : bytes) {
                mergeBytes[j++] = b;
            }
        }
        builder.clearStringVal();
        byte[] nbytes = IOUtil.gzip(mergeBytes);
        builder.addStringVal(ByteString.copyFrom(nbytes));
        TensorShapeProto.Dim dim = TensorShapeProto.Dim.newBuilder().setSize(1).build();
        builder.setTensorShape(TensorShapeProto.newBuilder().addDim(dim).build());
    }

    public TensorProto.Builder buildFeatureProto(Collection<Map<String, Object>> featureList) {
        int batchNum = featureList.size();
        if (config.exampleTypeIsUserOnceExample()) {
            batchNum += 1;
        }

        TensorProto.Builder builder = TensorProto.newBuilder()
                .setDtype(DataType.DT_STRING);
        Optional<Features.Builder> commonFeaturesTF = commonFeatures.map(x -> buildFeature(x, f -> f.getIsShared()));
        if (config.exampleTypeIsUserOnceExample()) {
            if (commonFeaturesTF.isPresent()) {
                Features.Builder featuresTF = commonFeaturesTF.get();
                ByteString inputStr = Example.newBuilder().setFeatures(featuresTF).build().toByteString();
                builder.addStringVal(inputStr);
            }
        }
        if (config.exampleTypeIsMixedSharedExample()) {
            boolean mixed = config.getAllConfigs().stream().anyMatch(it -> it.getIsMixedShared());
            List<Integer> repeat = new ArrayList<>();
            List<Features.Builder> builders = new ArrayList<>();
            if (mixed) {
                String groupByFeature = config.getSplitFeature();
                Object last = null;
                int cnt = 0;
                for (Map<String, Object> features : featureList) {
                    Object group = features.getOrDefault(groupByFeature, "");
                    if (Objects.equals(last, group)) {
                        cnt += 1;
                    } else {
                        if (cnt > 0) {
                            repeat.add(cnt);
                        }
                        Features.Builder featuresTF = buildFeature(features, x -> !x.getIsShared() && x.getIsMixedShared());
                        builders.add(featuresTF);
                        cnt = 1;
                    }
                    last = group;
                }
                if (cnt > 0) {
                    repeat.add(cnt);
                }
            } else {
                repeat.add(featureList.size());
                Features.Builder featuresTF = buildFeature(Collections.emptyMap(), x -> !x.getIsShared() && x.getIsMixedShared());
                builders.add(featuresTF);
            }
            builder.addStringVal(ByteString.copyFromUtf8(StringUtils.join(repeat, ",")));
            for (Features.Builder featuresTF : builders) {
                ByteString inputStr = Example.newBuilder().setFeatures(featuresTF).build().toByteString();
                builder.addStringVal(inputStr);
            }
            batchNum += builders.size() + 1;
        }
        for (Map<String, Object> features : featureList) {
            Features.Builder featuresTF = buildFeature(features, x -> !x.getIsShared() && !x.getIsMixedShared());
            if (commonFeaturesTF.isPresent() && !config.exampleTypeIsUserOnceExample()) {
                featuresTF = featuresTF.mergeFrom(commonFeaturesTF.get().build());
            }
            ByteString inputStr = Example.newBuilder().setFeatures(featuresTF).build().toByteString();
            builder.addStringVal(inputStr);
        }

        TensorShapeProto.Dim dim = TensorShapeProto.Dim.newBuilder().setSize(batchNum).build();
        builder.setTensorShape(TensorShapeProto.newBuilder().addDim(dim).build());
        if ("GZIP".equals(config.getCompressionType())) {
            if (StringUtils.isEmpty(config.getCompressionSep())) {
                applyGzip(builder);
            } else {
                applyFullGzip(builder);
            }
        }
        return builder;
    }

    private Features.Builder buildFeature(Map<String, Object> featureMap, Function<ModelBaseConfig.FeatureConfig, Boolean> filter) {
        Features.Builder builder = Features.newBuilder();

        for (ModelBaseConfig.FeatureConfig featureConfig : config.getIntFeatures()) {
            if (filter.apply(featureConfig)) {
                Optional<Number> featureValue = Optional.empty();
                try {
                    if (featureMap.get(featureConfig.getName()) instanceof String) {
                        featureValue = Optional.of(Integer.parseInt((String) featureMap.get(featureConfig.getName())));
                    } else {
                        featureValue = Optional.ofNullable((Number) featureMap.get(featureConfig.getName()));
                    }
                    builder.putFeature(featureConfig.getFeatureNameOrAlias(), toIntFeature(featureValue).build());
                } catch (Exception ex) {
                    log.error("serialize int feature :{},value:{} failed", featureConfig.getName(), gson.toJson(featureValue), ex);
                    throw ex;
                }
            }

        }

        for (ModelBaseConfig.FeatureConfig featureConfig : config.getFloatFeatures()) {
            if (filter.apply(featureConfig)) {
                Optional<Number> featureValue = Optional.empty();
                try {
                    if (featureMap.get(featureConfig.getName()) instanceof String) {
                        // TODO：不同模型中，存在相同特征名，但类型不同的特征；注：尽量保持相同特征名只有一种特征类型
                        featureValue = Optional.of(Float.parseFloat((String) featureMap.get(featureConfig.getName())));
                    } else {
                        featureValue = Optional.ofNullable((Number) featureMap.get(featureConfig.getName()));
                    }
                    builder.putFeature(featureConfig.getFeatureNameOrAlias(), toFloatFeature(featureValue).build());
                } catch (Exception ex) {
                    log.error("serialize float feature :{},value:{} failed", featureConfig.getName(), gson.toJson(featureValue), ex);
                    throw ex;
                }
            }

        }

        for (ModelBaseConfig.FeatureConfig featureConfig : config.getListStringFeatures()) {
            log.debug("serialize list stirng feature name:{}", featureConfig.getName());
            if (filter.apply(featureConfig)) {
                Optional<List<String>> featureValue = Optional.ofNullable((List<String>) featureMap.get(featureConfig.getName()));
                try {
                    builder.putFeature(featureConfig.getFeatureNameOrAlias(), toListStringFeature(featureValue).build());
                } catch (Exception ex) {
                    log.error("serialize liststring feature :{},value:{} failed", featureConfig.getName(), gson.toJson(featureValue), ex);
                    throw ex;
                }
            }
        }

        for (ModelBaseConfig.FeatureConfig featureConfig : config.getNestedListStringFeatures()) {
            log.debug("serialize list stirng feature name:{}", featureConfig.getName());
            if (filter.apply(featureConfig)) {
                Optional<List<String>> featureValue = Optional.ofNullable((List<String>) featureMap.get(featureConfig.getName()));
                try {
                    builder.putFeature(featureConfig.getFeatureNameOrAlias(), toListStringFeature(featureValue).build());
                } catch (Exception ex) {
                    log.error("serialize liststring feature :{},value:{} failed", featureConfig.getName(), gson.toJson(featureValue), ex);
                    throw ex;
                }
            }
        }

        for (ModelBaseConfig.FeatureConfig featureConfig : config.getListFloatFeatures()) {
            log.debug("serialize list float feature name:{}", featureConfig.getName());
            if (filter.apply(featureConfig)) {
                if (featureConfig.getDim() > 0) {
                    Optional<List<Number>> featureValue = Optional.ofNullable((List<Number>) featureMap.get(featureConfig.getName()));
                    try {
                        builder.putFeature(featureConfig.getFeatureNameOrAlias(), toListFloatFeature(featureValue, featureConfig).build());
                    } catch (Exception ex) {
                        log.error("serialize list float feature :{},value:{} failed", featureConfig.getName(), gson.toJson(featureValue), ex);
                        throw ex;
                    }
                } else {
                    FloatList.Builder floatList = FloatList.newBuilder();
                    float[] array = (float[]) featureMap.get(featureConfig.getName());
                    if (array != null) {
                        for (float v : array) {
                            floatList.addValue(v);
                        }
                        builder.putFeature(featureConfig.getFeatureNameOrAlias(), Feature.newBuilder().setFloatList(floatList).build());
                    }
                }
            }
        }

        for (ModelBaseConfig.FeatureConfig featureConfig : config.getKvFeatures()) {
            log.debug("serialize kv feature name:{}", featureConfig.getName());
            if (filter.apply(featureConfig)) {
                Optional<Map<String, Double>> featureValue = Optional.ofNullable((Map<String, Double>) featureMap.get(featureConfig.getName()));
                try {
                    Feature.Builder[] kvBuilders = toKVFeature(featureValue);

                    builder.putFeature(featureConfig.getFeatureNameOrAlias() + ID_SUFIX, kvBuilders[0].build());
                    builder.putFeature(featureConfig.getFeatureNameOrAlias() + VALUE_SUFIX, kvBuilders[1].build());
                } catch (Exception ex) {
                    log.error("serialize kv feature :{},value:{} failed", featureConfig.getName(), gson.toJson(featureValue), ex);
                    throw ex;
                }
            }
        }

        for (ModelBaseConfig.FeatureConfig featureConfig : config.getStringFeatures()) {
            log.debug("serialize string feature name:{}", featureConfig.getName());
            if (filter.apply(featureConfig)) {
                Optional<String> featureValue = Optional.ofNullable((String) featureMap.get(featureConfig.getName()));
                try {
                    builder.putFeature(featureConfig.getFeatureNameOrAlias(), toStringFeature(featureValue).build());
                } catch (Exception ex) {
                    log.error("serialize string feature :{},value:{} failed", featureConfig.getName(), gson.toJson(featureValue), ex);
                    throw ex;
                }
            }
        }
        return builder;
    }

}
