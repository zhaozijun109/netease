package com.netease.easyml.local.mllib.tfserving.wrapper;

import com.google.common.collect.Maps;
import com.netease.easyml.local.mllib.tfserving.client.LocalClientWrapper;
import com.netease.easyml.local.mllib.tfserving.config.ModelBaseConfig;
import lombok.extern.slf4j.Slf4j;
import org.tensorflow.framework.TensorProto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by linjiuning on 2021/10/14.
 */
@Slf4j
public class LocalTFModelWrapperExamples extends AbstractTFModelWrapperExamples {
    LocalClientWrapper clientWrapper;

    public LocalTFModelWrapperExamples(ModelBaseConfig config) {
        super(config);
        this.clientWrapper = new LocalClientWrapper(config);
    }

    public Map<String, Object> predict(Map<String, Map<String, Object>> featureMap, Optional<Map<String, Object>> commonFeatures) throws Exception {
        // 特征信息转List
        List<String> goodsId = new ArrayList<>();
        List<Map<String, Object>> featureList = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> goodFeatures : featureMap.entrySet()) {
            goodsId.add(goodFeatures.getKey());
            featureList.add(goodFeatures.getValue());
        }
        FeatureBuilderExamples featureBuilderExamples = new FeatureBuilderExamples(config, commonFeatures);
        // 预测结果map
        Map<String, Object> predictRes = Maps.newHashMap();
        List<Batch> batches = batch(goodsId, featureList);
        // 数据分片，提交任务
        for (Batch batch : batches) {
            List<String> subGoodsId = batch.subGoodsId;
            List<Map<String, Object>> subFeature = batch.subFeature;

            TensorProto tensor = featureBuilderExamples.buildFeatureProto(subFeature).build();
            Map<String, List<Object>> outputs = clientWrapper.request(tensor);
            if (config.isReturnAll()) {
                outputs = clientWrapper.getAllScores(outputs);
                updateResults(predictRes, subGoodsId, outputs);
            } else {
                List<Object> resList = clientWrapper.getScores(outputs);
                for (int j = 0; j < subGoodsId.size(); ++j) {
                    predictRes.put(subGoodsId.get(j), resList.get(j));
                }
            }
        }

        return predictRes;
    }
}
