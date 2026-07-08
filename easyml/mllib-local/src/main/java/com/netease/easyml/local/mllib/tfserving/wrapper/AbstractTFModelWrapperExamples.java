package com.netease.easyml.local.mllib.tfserving.wrapper;

import com.netease.easyml.common.util.StringUtil;
import com.netease.easyml.local.mllib.tfserving.config.ModelBaseConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Created by linjiuning on 2022/7/6.
 */
@Slf4j
public abstract class AbstractTFModelWrapperExamples {
    protected static final String IDS = "__ids";

    @Getter
    protected final ModelBaseConfig config;

    protected AbstractTFModelWrapperExamples(ModelBaseConfig config) {
        this.config = config;
    }

    protected List<Batch> simpleSplit(List<String> goodsId, List<Map<String, Object>> featureList) {
        List<Batch> batches = new ArrayList<>();
        double bs = (double) goodsId.size() / (double) config.getBatchSize();
        int batchNum = (int) Math.ceil(bs);
        // 数据分片，提交任务
        for (int i = 0; i < batchNum; ++i) {
            int startIndex = i * config.getBatchSize();
            int endIndex = (i + 1) * config.getBatchSize();
            if (endIndex > goodsId.size()) {
                endIndex = goodsId.size();
            }
            List<String> subGoodsId = goodsId.subList(startIndex, endIndex);
            List<Map<String, Object>> subFeature = featureList.subList(startIndex, endIndex);
            batches.add(new Batch(subGoodsId, subFeature));
        }
        return batches;
    }

    protected List<Batch> groupSplit(List<String> goodsId, List<Map<String, Object>> featureList) {
        Integer batchSize = config.getBatchSize();
        String groupByFeature = config.getSplitFeature();
        Map<Object, List<Integer>> groupIndices = new HashMap<>();
        for (int i = 0; i < featureList.size(); i++) {
            Map<String, Object> feature = featureList.get(i);
            Object group = feature.getOrDefault(groupByFeature, "");
            if (!groupIndices.containsKey(group)) {
                groupIndices.put(group, new ArrayList<>());
            }
            groupIndices.get(group).add(i);
        }

        List<Batch> batches = new ArrayList<>();
        List<String> subGoodsId = new ArrayList<>();
        List<Map<String, Object>> subFeatureList = new ArrayList<>();
        for (List<Integer> indices : groupIndices.values()) {
            List<String> groupGoodsId = new ArrayList<>();
            List<Map<String, Object>> groupFeatureList = new ArrayList<>();
            for (Integer index : indices) {
                groupGoodsId.add(goodsId.get(index));
                groupFeatureList.add(featureList.get(index));
            }
            List<Batch> groupBatches = simpleSplit(groupGoodsId, groupFeatureList);
            Batch last = groupBatches.remove(groupBatches.size() - 1);
            batches.addAll(groupBatches);
            if (last.size() < batchSize * 0.6) {
                subGoodsId.addAll(last.subGoodsId);
                subFeatureList.addAll(last.subFeature);
            } else {
                batches.add(last);
            }

        }
        List<Batch> subBatches = simpleSplit(subGoodsId, subFeatureList);
        batches.addAll(subBatches);
        return batches;
    }

    protected List<Batch> batch(List<String> goodsId, List<Map<String, Object>> featureList) {
        List<Batch> batches;
        String groupByFeature = config.getSplitFeature();
        if (StringUtil.isEmpty(groupByFeature)) {
            batches = simpleSplit(goodsId, featureList);
        } else {
            batches = groupSplit(goodsId, featureList);
        }
        return batches;
    }


    public abstract Map<String, Object> predict(Map<String, Map<String, Object>> featureMap, Optional<Map<String, Object>> commonFeatures) throws Exception;

    public static void updateResults(Map<String, Object> results, List<String> ids, Map<String, List<Object>> batchResults) {
        if (!results.containsKey(IDS)) {
            results.put(IDS, new ArrayList<>());
        }
        ((List<Object>) results.get(IDS)).addAll(ids);
        for (Map.Entry<String, List<Object>> entry : batchResults.entrySet()) {
            String key = entry.getKey();
            if (!results.containsKey(key)) {
                results.put(key, new ArrayList<>());
            }
            ((List<Object>) results.get(key)).addAll(entry.getValue());
        }
    }

    @AllArgsConstructor
    public static class Batch {
        public List<String> subGoodsId;
        public List<Map<String, Object>> subFeature;

        public int size() {
            return subGoodsId == null ? 0 : subGoodsId.size();
        }
    }
}
