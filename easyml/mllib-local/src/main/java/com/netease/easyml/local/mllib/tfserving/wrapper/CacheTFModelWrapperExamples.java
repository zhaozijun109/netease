package com.netease.easyml.local.mllib.tfserving.wrapper;

import com.netease.easyml.common.util.ArrayUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by linjiuning on 2022/7/8.
 */
public class CacheTFModelWrapperExamples extends AbstractTFModelWrapperExamples {
    private static final String FLAG = "__flag";
    private static final String CACHE = "__cache";
    private static final String USER_ID = "user_id";
    private AbstractTFModelWrapperExamples client;
    private AbstractTFModelWrapperExamples userClient;
    private AbstractTFModelWrapperExamples itemClient;
    private AbstractTFModelWrapperExamples cacheClient;

    //TODO: cache manager
    private Map<String, Object> userCache = new HashMap<>();
    private Map<String, Object> itemCache = new HashMap<>();

    public CacheTFModelWrapperExamples(AbstractTFModelWrapperExamples userClient,
                                       AbstractTFModelWrapperExamples itemClient,
                                       AbstractTFModelWrapperExamples cacheClient) {
        super(cacheClient.config);
        this.userClient = userClient;
        this.itemClient = itemClient;
        this.cacheClient = cacheClient;
    }

    public CacheTFModelWrapperExamples(AbstractTFModelWrapperExamples client,
                                       AbstractTFModelWrapperExamples cacheClient) {
        super(cacheClient.config);
        this.client = client;
        this.client.config.setReturnAll(true);
        this.cacheClient = cacheClient;
    }

    @Override
    public Map<String, Object> predict(Map<String, Map<String, Object>> featureMap, Optional<Map<String, Object>> commonFeatures) throws Exception {
        if (client != null) {
            if (commonFeatures.isPresent()) {
                String userId = (String) commonFeatures.get().get(USER_ID);
                if (!userCache.containsKey(userId)) {
                    Map<String, Object> results = client.predict(featureMap, commonFeatures);
                    Map<String, Object> scores = new HashMap<>();
                    List<String> ids = (List<String>) results.get(IDS);
                    Object probs = results.get(config.getTfOutputLayer());
                    for (int i = 0; i < ids.size(); i++) {
                        scores.put(ids.get(i), ArrayUtil.get(probs, i));
                    }
                    userCache.put(userId, ArrayUtil.get(results.get(USER_ID), 0));
                    return scores;
                } else {
                    commonFeatures.get().put(USER_ID + FLAG, 1);
                    commonFeatures.get().put(USER_ID + CACHE, userCache.get(userId));
                }
            }
        } else {
            if (userClient != null && commonFeatures.isPresent()) {
                String key = userClient.config.getTfOutputLayer();
                userClient.config.setExampleType("example");
                String userId = (String) commonFeatures.get().get(key);
                if (!userCache.containsKey(userId)) {
                    Map<String, Object> userEmbedding = userClient.predict(Collections.singletonMap(userId, Collections.emptyMap()), commonFeatures);
                    userCache.putAll(userEmbedding);
                }
                commonFeatures.get().put(key + FLAG, 1);
                commonFeatures.get().put(key + CACHE, userCache.get(userId));
            }
            if (itemClient != null) {
                List<String> itemIds = featureMap.keySet().stream().filter(it -> !itemCache.containsKey(it)).collect(Collectors.toList());
                if (!itemIds.isEmpty()) {
                    Map<String, Map<String, Object>> subFeatureMap = new HashMap<>();
                    for (String itemId : itemIds) {
                        subFeatureMap.put(itemId, featureMap.get(itemId));
                    }
                    Map<String, Object> itemEmbedding = itemClient.predict(subFeatureMap, Optional.of(new HashMap<>()));
                    itemCache.putAll(itemEmbedding);
                }
                for (String itemId : featureMap.keySet()) {
                    String key = itemClient.config.getTfOutputLayer();
                    featureMap.get(itemId).put(key + FLAG, 1);
                    featureMap.get(itemId).put(key + CACHE, itemCache.get(itemId));
                }
            }
        }
        return cacheClient.predict(featureMap, commonFeatures);
    }
}
