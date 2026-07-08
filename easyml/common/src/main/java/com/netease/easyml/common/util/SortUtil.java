package com.netease.easyml.common.util;

import java.util.*;

/**
 * Created by linjiuning on 2020/07/21.
 */
public class SortUtil {
    /**
     * sort map by value ascending
     */
    public static <K, V extends Comparable<V>> Map<K, V> sortByValueAsc(Map<K, V> map) {
        return sort(map, Comparator.comparing(Map.Entry::getValue));
    }

    /**
     * sort map by value descending
     */
    public static <K, V extends Comparable<V>> Map<K, V> sortByValueDesc(Map<K, V> map) {
        return sort(map, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
    }

    public static <K, V> Map<K, V> sort(Map<K, V> map, Comparator<Map.Entry<K, V>> comparator) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        list.sort(comparator);

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
