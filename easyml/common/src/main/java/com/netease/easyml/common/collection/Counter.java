package com.netease.easyml.common.collection;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by linjiuning on 2018/6/4.
 */
public class Counter<K> {
    public static <K> Counter<K> counter(Collection<K> keys) {
        return new Counter<K>().addAll(keys);
    }

    private Map<K, Integer> counter;

    public Counter() {
        this(false);
    }

    public Counter(boolean order) {
        if (order)
            counter = new LinkedHashMap<>();
        else
            counter = new HashMap<>();
    }

    public Counter<K> add(K key) {
        counter.put(key, counter.getOrDefault(key, 0) + 1);
        return this;
    }

    public Integer get(Object key) {
        return counter.getOrDefault(key, 0);
    }

    public Set<Map.Entry<K, Integer>> entrySet() {
        return counter.entrySet();
    }

    public Set<K> keySet() {
        return counter.keySet();
    }

    public void clear() {
        counter.clear();
    }

    public boolean isEmpty() {
        return counter.isEmpty();
    }

    public boolean containsKey(Object key) {
        return counter.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return counter.containsValue(value);
    }

    public Counter<K> addAll(Collection<K> keys) {
        keys.forEach(this::add);
        return this;
    }

    public List<Tuple<K, Integer>> mostCommon() {
        return counter.entrySet().stream().sorted((c1, c2) -> -c1.getValue().compareTo(c2.getValue())).map(it -> Tuple.tuple(it.getKey(), it.getValue())).collect(Collectors.toList());
    }

    public List<Tuple<K, Integer>> mostCommon(int topk) {
        List<Tuple<K, Integer>> sorted = mostCommon();
        if (topk >= sorted.size())
            return sorted;
        else
            return sorted.subList(0, topk);
    }

    public Map<K, Integer> getCounter() {
        return counter;
    }
}
