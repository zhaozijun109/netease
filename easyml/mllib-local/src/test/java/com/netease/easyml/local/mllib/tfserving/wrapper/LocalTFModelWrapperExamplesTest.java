package com.netease.easyml.local.mllib.tfserving.wrapper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netease.easyml.common.util.IOUtil;
import com.netease.easyml.common.util.SortUtil;
import com.netease.easyml.common.util.StringUtil;
import com.netease.easyml.local.mllib.tfserving.config.ModelBaseConfig;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by linjiuning on 2021/10/9.
 */
public class LocalTFModelWrapperExamplesTest {
    private static final String config = "/Users/linjiuning/workspace/git/netease/py_scripts/ai/fastrec/ranking_v6/online_fr/p002/base_inc/config.user.yaml";
    private static final String data = "../examples/toy_dataset/tfserving/feed_rec.json";
    private static final String output = "target/tfserving/result2.txt";
        private static final String exportDir = "/Users/linjiuning/workspace/git/netease/fastrec/tutorial/toy_dataset/estimator/rank/tmp/models/saved_models/1662109845";
//    private static final String exportDir = "/Users/linjiuning/workspace/git/netease/fastrec/tutorial/toy_dataset/estimator/rank/tmp/models/saved_models/1657276365";
    private LocalTFModelWrapperExamples client;

    @Before
    public void setUp() throws Exception {
        ModelBaseConfig modelBaseConfig = ModelBaseConfig.load(config);
        modelBaseConfig.setExportDir(exportDir);
        modelBaseConfig.setExampleType("user_once_example");
        client = new LocalTFModelWrapperExamples(modelBaseConfig);
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
        int repeat = 100;
        for (int i = 0; i < repeat; i++) {
            for (String line : IOUtil.readLines(data)) {
                JSONObject obj = JSON.parseObject(line);

                JSONObject item = obj.getJSONObject("item");
                Map<String, Map<String, Object>> itemFea = new HashMap<>();
                for (Map.Entry<String, Object> entry : item.entrySet()) {
                    Map<String, Object> value = (Map<String, Object>) entry.getValue();
//                value.put("o_alg", "SWING_ITEM");
                    value.put("i_sim_cluster", rng.nextInt(5));
                    itemFea.put(entry.getKey(), value);
                }
                Map<String, Object> user = obj.getJSONObject("user");
                itemFea = (Map<String, Map<String, Object>>) fastJsonToJava(itemFea);
                user = (Map<String, Object>) fastJsonToJava(user);
//                user.put("u_light_items", Arrays.asList("4cbe2bb7_1cd51e27a","320124aa_2b3e1774f","4d0cf51f_1cd4d247b","3147a373_1ccab3707","4c5e3623_1cc8435d8","4c59d657_1cd42130c","4cf444c5_1cd1d9742","4ca06c21_1cd21dbba","4cad5eaa_1cd31c5c0","4ca7722e_1cd235c79","4ca7722e_1cd235172","4c5bd990_1ccb1b798","321cf944_1cd3f2c4f","4cba4912_1cd483d2a","1ebe6a3a_1cd448129","1eaced29_1cd3f358b","4cba4912_1cd4582ad","30b0463f_1cd3c243a","1d8cb4de_1cd35b473"));
//                user.put("u_light_items_tags", Arrays.asList("杰罗姆,杰罗麦,哥谭","cameron monaghan","卡梅隆莫纳汉,哥谭,杰罗姆,杰罗麦","哥谭,丑鹅","哥谭,杰罗姆瓦勒斯卡,杰罗姆,卡梅隆莫纳汉,哥谭市,cameron monaghan,瓦勒斯卡,杰罗麦","marvel,洛基,蜘蛛侠,汤姆.赫兰德","what if…,漫威,洛基,雷神,小李子,彩蛋","感人,剧情,刘德华,影视","lofter,动漫","影视剪辑,好据推荐,精彩片段","泰坦尼克号,影视剪辑,精彩片段","排球少年,小排球,菅原孝支","鱿鱼游戏,yuliiatvieritina","鱿鱼游戏,姜晓,姜晓智英","成奇勋,鱿鱼游戏","黄俊昊,鱿鱼游戏,仁俊骨科","鱿鱼游戏,李政宰","procreate,鱿鱼游戏","凹凸世界,真赞,紫堂真,赞德"));
//                user.put("u_high_items", Arrays.asList("4cad7c72_1cd229014"));
//                user.put("u_high_items_tags", Arrays.asList("lofter,动漫"));
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
//                outLines.add(JSON.toJSONString(map));
            }
        }
        System.out.println(String.format("Cost time: %.3f", totalTime * 1.0 / size));
        IOUtil.mkParentDirs(output);
        IOUtil.writeLines(output, outLines);
    }
}