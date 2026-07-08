package com.netease.easyml.local.mllib.processor.cleaner;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by eddielin on 2018/6/2.
 */
@FunctionalInterface
public interface ICleaner {
    String replace(String repl, String text);

    default List<String> replace(String repl, List<String> texts) {
        return texts.stream().map(it -> replace(repl, it)).filter(it -> !it.trim().isEmpty()).collect(Collectors.toList());
    }

    static String clean(Collection<ICleaner> cleaners, String repl, String text) {
        String pText = text;
        for (ICleaner cleaner : cleaners)
            pText = cleaner.replace(repl, pText);
        return pText;
    }
}
