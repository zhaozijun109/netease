package com.netease.easyml.local.mllib.tfserving.wrapper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netease.easyml.common.util.IOUtil;
import com.netease.easyml.common.util.SortUtil;
import com.netease.easyml.common.util.StringUtil;
import com.netease.easyml.local.mllib.tfserving.config.ModelBaseConfig;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by linjiuning on 2022/7/8.
 */
public class CacheTFModelWrapperExamplesTest {
    private static final String config = "/Users/linjiuning/workspace/git/netease/py_scripts/ai/fastrec/ranking_v6/online_fr/p002/base_inc/config.yaml";
    private static final String userConfig = "/Users/linjiuning/workspace/git/netease/py_scripts/ai/fastrec/ranking_v6/online_fr/p002/base_inc/config.user.yaml";
    private static final String itemConfig = "/Users/linjiuning/workspace/git/netease/py_scripts/ai/fastrec/ranking_v6/online_fr/p002/base_inc/config.item.yaml";
//    private static final String cacheConfig = "/Users/linjiuning/workspace/git/netease/py_scripts/ai/fastrec/ranking_v6/online_fr/v017_p2/value_ext_cache_v2/config.cache.yaml";

    private static final String data = "../examples/toy_dataset/tfserving/feed_rec.json";
    private static final String output = "target/tfserving/result1.txt";
    private static final String exportDir = "/Users/linjiuning/workspace/git/netease/fastrec/tutorial/toy_dataset/estimator/rank/tmp/models/saved_models/1662115963";

    private CacheTFModelWrapperExamples client;

    public ModelBaseConfig load(String path) throws IOException {
        ModelBaseConfig modelBaseConfig = ModelBaseConfig.load(path);
        modelBaseConfig.setExportDir(exportDir);
        modelBaseConfig.setExampleType("user_once_example");
        return modelBaseConfig;
    }

    @Before
    public void setUp() throws Exception {
        AbstractTFModelWrapperExamples cacheClient = null;
        AbstractTFModelWrapperExamples userClient = null;
        AbstractTFModelWrapperExamples itemClient = null;
        AbstractTFModelWrapperExamples client = null;

//        client = new LocalTFModelWrapperExamples(load(config));
//        userClient = new LocalTFModelWrapperExamples(load(userConfig));
//        itemClient = new LocalTFModelWrapperExamples(load(itemConfig));
//        cacheClient = new LocalTFModelWrapperExamples(load(cacheConfig));

        client = new TFModelWrapperExamples(load(config));
        userClient = new TFModelWrapperExamples(load(userConfig));
        itemClient = new TFModelWrapperExamples(load(itemConfig));
//        cacheClient = new TFModelWrapperExamples(load(cacheConfig));

        this.client = new CacheTFModelWrapperExamples(userClient, itemClient, client);
//        this.client = new CacheTFModelWrapperExamples(client, cacheClient);
    }


    public static Object fastJsonToJava(Object obj) {
        if (obj instanceof Map) {
            Map map = (Map) obj;
            Map nMap = new HashMap();
            for (Object k : map.keySet()) {
                nMap.put(k, fastJsonToJava(map.get(k)));
            }
            return nMap;
        } else if (obj instanceof BigDecimal) {
            return ((BigDecimal) obj).doubleValue();
        }
        return obj;
    }

    @Test
    public void predict() throws Exception {
        List<String> outLines = new ArrayList<>();
        Random rng = new Random();
        long totalTime = 0;
        int size = 0;
        int repeat = 1;
        for (int i = 0; i < repeat; i++) {
            for (String line : IOUtil.readLines(data)) {
                JSONObject obj = JSON.parseObject(line);

                JSONObject item = obj.getJSONObject("item");
                Map<String, Map<String, Object>> itemFea = new HashMap<>();
                for (Map.Entry<String, Object> entry : item.entrySet()) {
                    Map<String, Object> value = (Map<String, Object>) entry.getValue();
                    value.put("i_sim_cluster", rng.nextInt(5));
                    itemFea.put(entry.getKey(), value);
                }
                Map<String, Object> user = obj.getJSONObject("user");
                itemFea = (Map<String, Map<String, Object>>) fastJsonToJava(itemFea);
//                itemFea = Collections.singletonMap("3128c79d_2b58c1815", itemFea.get("3128c79d_2b58c1815"));
                user = (Map<String, Object>) fastJsonToJava(user);
                Map<String, Object> map;
                if (i >= 10) {
                    long startTime = System.currentTimeMillis();
                    map = client.predict(itemFea, Optional.of(user));
                    long endTime = System.currentTimeMillis();
                    totalTime += endTime - startTime;
                    size += map.size();
                } else {
                    map = client.predict(itemFea, Optional.of(user));
                }
                map = SortUtil.sort(map, (it1, it2) -> ((Float) it2.getValue()).compareTo((Float) it1.getValue()));
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    String itemId = entry.getKey();
                    outLines.add(StringUtil.join(Arrays.asList(itemId, entry.getValue().toString(), JSON.toJSONString(itemFea.get(itemId))), "_!_"));
                }
            }
        }
        System.out.println(String.format("Cost time: %.3f", totalTime * 1.0 / size));
        IOUtil.mkParentDirs(output);
        IOUtil.writeLines(output, outLines);
    }
}