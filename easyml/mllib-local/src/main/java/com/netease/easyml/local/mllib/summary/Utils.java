package com.netease.easyml.local.mllib.summary;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eddielin on 2019/2/19.
 */
public class Utils {
    public static List<Integer> getSentenceLength(List<List<String>> tokens, boolean charLevel) {
        List<Integer> sentLen = new ArrayList<>();
        for (List<String> words : tokens) {
            int slen = 0;
            if (charLevel) {
                for (String wd : words)
                    slen += wd.length();
            } else
                slen = words.size();
            sentLen.add(slen);
        }
        return sentLen;
    }
}
