package com.netease.easyml.local.mllib.tfserving.wrapper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.netease.easyml.common.util.IOUtil;
import com.netease.easyml.common.util.SortUtil;
import com.netease.easyml.local.mllib.tfserving.config.ModelBaseConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by linjiuning on 2021/10/9.
 */
public class TFModelWrapperExamplesTest {
    String config = "/Users/linjiuning/workspace/git/netease/py_scripts/ai/fastrec/ranking_v10/online_fr/rk/v020/value_test_inc/config.yaml";
    String data = "../examples/toy_dataset/tfserving/debug.json";
    String output = "target/tfserving/result.txt";
    String outputAb = "target/tfserving/ab.csv";

    List<String> newTags = Lists.newArrayList();
    List<Integer> newTops = Lists.newArrayList();

    List<Integer> mapTops = Lists.newArrayList();

    private TFModelWrapperExamples client;

    @Before
    public void setUp() throws Exception {
        client = new TFModelWrapperExamples(ModelBaseConfig.load(config));

        newTags = Lists.newArrayList("排球少年");
        newTops = Lists.newArrayList(10, 30, 50);

        mapTops = Lists.newArrayList(10, 30, 50);
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

    public List<Pair<String, Double>> newIpRatio(List<Triple<String, List<String>, Float>> order, List<String> tags, List<Integer> top) {
        List<Pair<String, Double>> result = new ArrayList<>();
        for (Integer t : top) {
            List<Triple<String, List<String>, Float>> pairs = order.subList(0, t);
            List<Triple<String, List<String>, Float>> filter = pairs.stream().filter(it -> it.getMiddle().stream().anyMatch(tag -> tags.contains(tag))).collect(Collectors.toList());
            result.add(Pair.of("top" + t, filter.size() * 1.0 / t));
        }
        return result;
    }

    public List<Pair<String, Double>> meanAveragePrecision(List<Triple<String, List<String>, Float>> a, List<Triple<String, List<String>, Float>> b, List<Integer> top) {
        List<Pair<String, Double>> result = new ArrayList<>();
        for (Integer t : top) {
            List<Triple<String, List<String>, Float>> subA = a.subList(0, t);
            List<Triple<String, List<String>, Float>> subB = b.subList(0, t);
            Set<String> setA = subA.stream().map(it -> it.getLeft()).collect(Collectors.toSet());
            List<Triple<String, List<String>, Float>> filter = subB.stream().filter(it -> setA.contains(it.getLeft())).collect(Collectors.toList());
            result.add(Pair.of("top" + t, filter.size() * 1.0 / t));
        }
        return result;
    }

    public List<Triple<String, List<String>, Float>> predict(String feature, List<String> userKeys, List<String> itemKeys) throws Exception {
        List<Triple<String, List<String>, Float>> result = new ArrayList<>();
        JSONObject obj = JSON.parseObject(feature);

        JSONObject item = obj.getJSONObject("item");
        Map<String, Map<String, Object>> itemFea = new HashMap<>();
        Map<String, List<String>> iTags = new HashMap<>();
        for (Map.Entry<String, Object> entry : item.entrySet()) {
            iTags.put(entry.getKey(), ((JSONArray) ((Map<String, Object>) entry.getValue()).get("i_tags")).toJavaList(String.class));
        }
        for (Map.Entry<String, Object> entry : item.entrySet()) {
            Map<String, Object> value = (Map<String, Object>) entry.getValue();
            itemFea.put(entry.getKey(), value);
        }
        Map<String, Object> user = obj.getJSONObject("user");
        for (String k : userKeys) {
            if (!user.containsKey(k)) {
                continue;
            }
            if (user.get(k) instanceof String) {
                user.put(k, "");
            }
            if (user.get(k) instanceof Number) {
                user.put(k, 0.0);
            }
            if (user.get(k) instanceof JSONArray) {
                ((JSONArray) user.get(k)).clear();
            }
            if (user.get(k) instanceof JSONObject) {
                ((JSONObject) user.get(k)).clear();
            }
            if (Arrays.asList("u_light_items", "u_high_items").contains(k)) {
                String nk = k + "_tags";
                if (user.get(nk) instanceof JSONArray) {
                    ((JSONArray) user.get(nk)).clear();
                }
            }
        }

        for (Map.Entry<String, Object> entry : item.entrySet()) {
            Map<String, Object> value = (Map<String, Object>) entry.getValue();
            for (String k : itemKeys) {
                if (!value.containsKey(k)) {
                    continue;
                }
                if (value.get(k) instanceof String) {
                    value.put(k, "");
                }
                if (value.get(k) instanceof Number) {
                    value.put(k, 0.0);
                }
                if (value.get(k) instanceof JSONArray) {
                    ((JSONArray) value.get(k)).clear();
                }
                if (value.get(k) instanceof JSONObject) {
                    ((JSONObject) value.get(k)).clear();
                }
                if (Arrays.asList("c_u_long_sim_items").contains(k)) {
                    String nk = k + "_tags";
                    if (value.get(nk) instanceof JSONArray) {
                        ((JSONArray) value.get(nk)).clear();
                    }
                }
            }

            itemFea.put(entry.getKey(), value);
        }

        itemFea = (Map<String, Map<String, Object>>) fastJsonToJava(itemFea);
        user = (Map<String, Object>) fastJsonToJava(user);
        Map<String, Object> map;

        map = client.predict(itemFea, Optional.of(user));

        map = SortUtil.sort(map, (it1, it2) -> ((Float) it2.getValue()).compareTo((Float) it1.getValue()));
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String itemId = entry.getKey();
            result.add(Triple.of(itemId, iTags.get(itemId), (Float) entry.getValue()));
        }
        return result;
    }

    @Test
    public void ablationSingle() throws Exception {
        String features = IOUtil.readLines(data).get(0);
        List<String> userKeys = new ArrayList<>();
        List<String> itemKeys = new ArrayList<>();

        List<String> outLines = new ArrayList<>();
        outLines.add("type,name,metric," + newTops.stream().map(it -> "top" + it).collect(Collectors.joining(",")) + ",avg");

        JSONObject obj = JSON.parseObject(features);
        userKeys.addAll(obj.getJSONObject("user").keySet());
        userKeys.removeAll(Arrays.asList("u_light_items_tags", "u_high_items_tags", "o_hour"));
        JSONObject item = obj.getJSONObject("item");
        for (Object o : item.values()) {
            for (String k : ((Map<String, Object>) o).keySet()) {
                if (!itemKeys.contains(k)) {
                    itemKeys.add(k);
                }
            }
        }
        itemKeys.removeAll(Arrays.asList("c_u_long_sim_items_tags"));

        List<Triple<String, List<String>, Float>> truth = predict(features, Lists.newArrayList(), Lists.newArrayList());
        List<Pair<String, Double>> metric;
        String l;
        if (!newTags.isEmpty()) {
            metric = newIpRatio(truth, newTags, newTops);
            l = "all,all,new," + metric.stream().map(it -> it.getRight().toString()).collect(Collectors.joining(",")) + "," + metric.stream().mapToDouble(it -> it.getRight()).average().getAsDouble();
            outLines.add(l);
            System.out.println(l);
        }

        for (String k : userKeys) {
            List<Triple<String, List<String>, Float>> result = predict(features, Lists.newArrayList(k), Lists.newArrayList());
            if (!newTags.isEmpty()) {
                metric = newIpRatio(result, newTags, newTops);
                l = "user," + k + ",new," + metric.stream().map(it -> it.getRight().toString()).collect(Collectors.joining(",")) + "," + metric.stream().mapToDouble(it -> it.getRight()).average().getAsDouble();
                outLines.add(l);
                System.out.println(l);
            }
            if (!mapTops.isEmpty()) {
                metric = meanAveragePrecision(truth, result, mapTops);
                l = "user," + k + ",map," + metric.stream().map(it -> it.getRight().toString()).collect(Collectors.joining(",")) + "," + metric.stream().mapToDouble(it -> it.getRight()).average().getAsDouble();
                outLines.add(l);
                System.out.println(l);
            }
        }

        for (String k : itemKeys) {
            List<Triple<String, List<String>, Float>> result = predict(features, Lists.newArrayList(), Lists.newArrayList(k));
            if (!newTags.isEmpty()) {
                metric = newIpRatio(result, newTags, newTops);
                l = "item," + k + ",new," + metric.stream().map(it -> it.getRight().toString()).collect(Collectors.joining(",")) + "," + metric.stream().mapToDouble(it -> it.getRight()).average().getAsDouble();
                outLines.add(l);
                System.out.println(l);
            }
            if (!mapTops.isEmpty()) {
                metric = meanAveragePrecision(truth, result, mapTops);
                l = "item," + k + ",map," + metric.stream().map(it -> it.getRight().toString()).collect(Collectors.joining(",")) + "," + metric.stream().mapToDouble(it -> it.getRight()).average().getAsDouble();
                outLines.add(l);
                System.out.println(l);
            }
        }
        IOUtil.writeLines(outputAb, outLines);
    }


    @Test
    public void rank() throws Exception {
        String features = IOUtil.readLines(data).get(0);

        List<String> userKeys = Lists.newArrayList("user_id");
        List<String> itemKeys = Lists.newArrayList("c_itags_u_rt_tags", "c_itags_u_rt_itags");

        userKeys.removeAll(Arrays.asList("u_light_items_tags", "u_high_items_tags", "o_hour"));
        itemKeys.removeAll(Arrays.asList("c_u_long_sim_items_tags"));

        List<Triple<String, List<String>, Float>> result = predict(features, userKeys, itemKeys);
        if (!newTags.isEmpty()) {
            List<Pair<String, Double>> newIpRatio = newIpRatio(result, newTags, newTops);
            System.out.println(newTops.stream().map(it -> "top" + it).collect(Collectors.joining(",")) + ",avg");
            System.out.println(newIpRatio.stream().map(it -> it.getRight().toString()).collect(Collectors.joining(",")) + "," + newIpRatio.stream().mapToDouble(it -> it.getRight()).average().getAsDouble());
        }
        IOUtil.writeLines(output, result.stream().map(it -> it.getLeft() + "_!_" + StringUtils.join(it.getRight(), ",")).collect(Collectors.toList()));
    }
}