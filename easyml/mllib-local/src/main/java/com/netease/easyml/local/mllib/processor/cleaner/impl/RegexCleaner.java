package com.netease.easyml.local.mllib.processor.cleaner.impl;

import com.netease.easyml.local.mllib.processor.cleaner.ICleaner;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by eddielin on 2018/6/4.
 */
public class RegexCleaner implements ICleaner {
    private int flag = Pattern.CASE_INSENSITIVE;
    private Pattern pattern;

    public RegexCleaner(String... patterns) {
        String p = Stream.of(patterns).collect(Collectors.joining("|"));
        pattern = Pattern.compile(p, flag);
    }

    public RegexCleaner(Collection<String> patterns) {
        String p = patterns.stream().collect(Collectors.joining("|"));
        pattern = Pattern.compile(p);
    }

    @Override
    public String replace(String repl, String text) {
        Matcher m = pattern.matcher(text);
        return m.replaceAll(repl);
    }
}
