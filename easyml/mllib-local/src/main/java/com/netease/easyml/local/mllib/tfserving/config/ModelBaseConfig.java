package com.netease.easyml.local.mllib.tfserving.config;

import com.google.common.collect.Lists;
import com.netease.easyml.common.util.IOUtil;
import com.netease.easyml.common.util.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Slf4j
public class ModelBaseConfig {
    private String host;
    private Integer port;
    private String modelName;
    private String exportDir;
    private Integer numThreads = 0;
    private Integer batchSize = 100;
    private Integer connectionNum = 4;
    private Long timeOutMills = 10000L;
    private String signatureName;
    private String tfInputLayer = "examples";
    private String tfOutputLayer = "ctr_probabilities";
    private String exampleType = "example";
    private String tfOutputLayerKey = "";
    private String tfOutputLayerVal = "";
    private String compressionType = "";
    private String compressionSep = "";
    private String splitFeature = "";
    private List<FeatureConfig> intFeatures = Lists.newArrayList();
    private List<FeatureConfig> stringFeatures = Lists.newArrayList();
    private List<FeatureConfig> floatFeatures = Lists.newArrayList();
    private List<FeatureConfig> kvFeatures = Lists.newArrayList();
    private List<FeatureConfig> listStringFeatures = Lists.newArrayList();
    private List<FeatureConfig> listFloatFeatures = Lists.newArrayList();
    private List<FeatureConfig> nestedListStringFeatures = Lists.newArrayList();

    private boolean returnAll = false;

    public static ModelBaseConfig load(String configStr) throws IOException {
        try {
            if (IOUtil.exists(configStr)) {
                configStr = StringUtil.join(IOUtil.readLines(configStr), "\n");
            }
            Yaml yaml = new Yaml(new Constructor(ModelBaseConfig.class));
            ModelBaseConfig config = (ModelBaseConfig) yaml.load(configStr);
            return config;
        } catch (Exception e) {
            log.error("invalid model config str:{} , err msg: {}", configStr, e.getMessage());
            throw new IOException(e);
        }
    }

    @Data
    public static class FeatureConfig {
        private String name;
        private int index;
        private String normType;
        private String alias;
        private int dim = 0;
        private Boolean isShared = false;
        private Boolean isMixedShared = false;
        private List<String> normArg = new ArrayList<>();

        public String getFeatureNameOrAlias() {
            return (alias == null || "".equals(alias)) ? name : alias;
        }
    }

    public List<FeatureConfig> getAllConfigs() {
        List<FeatureConfig> allConfigs = new ArrayList<>();
        for (List<FeatureConfig> configs : Arrays.asList(stringFeatures, floatFeatures, kvFeatures, listStringFeatures, listFloatFeatures,
                nestedListStringFeatures, intFeatures)) {
            allConfigs.addAll(configs);
        }
        return allConfigs;
    }

    public List<String> getAllFeatureNames() {
        List<String> names = new ArrayList<>();
        for (FeatureConfig config : getAllConfigs()) {
            names.add(config.getName());
        }
        return names;
    }

    public boolean exampleTypeIsSequenceExample() {
        return "sequence_example".equals(this.exampleType);
    }

    public boolean exampleTypeIsUserOnceExample() {
        return "user_once_example".equals(this.exampleType);
    }

    public boolean exampleTypeIsMixedSharedExample() {
        return exampleTypeIsUserOnceExample() && !StringUtil.isEmpty(splitFeature);
    }
}