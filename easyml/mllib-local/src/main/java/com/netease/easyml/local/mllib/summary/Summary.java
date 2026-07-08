package com.netease.easyml.local.mllib.summary;

import java.util.List;

/**
 * Created by eddielin on 2019/2/18.
 */
public interface Summary {
    List<Integer> transform(List<List<String>> tokens);

    static String concat(List<String> sentence, List<Integer> ids) {
        ids.sort(Integer::compareTo);
        StringBuilder sb = new StringBuilder();
        for (int id : ids) {
            sb.append(sentence.get(id));
        }
        return sb.toString();
    }
}
