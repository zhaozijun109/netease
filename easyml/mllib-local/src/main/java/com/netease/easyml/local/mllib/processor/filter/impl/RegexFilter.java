package com.netease.easyml.local.mllib.processor.filter.impl;

import com.netease.easyml.local.mllib.processor.filter.IFilter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by eddielin on 2018/6/8.
 */
@Slf4j
public class RegexFilter implements IFilter {
    private int flag = Pattern.CASE_INSENSITIVE;
    @Getter
    private Pattern pattern;

    public RegexFilter(String p) {
        pattern = Pattern.compile(p, flag);
    }

    public RegexFilter(String... patterns) {
        String p = String.join("|", patterns);
        pattern = Pattern.compile(p, flag);
    }

    public RegexFilter(Collection<String> patterns) {
        String p = String.join("|", patterns);
        pattern = Pattern.compile(p, flag);
    }

    @Override
    public boolean filter(String text) {
        Matcher m = pattern.matcher(text);
        if (m.find()) {
//            log.debug(m.group());
            return true;
        }
        return false;
    }

    @Override
    public boolean filter(Collection<String> tokens) {
        return tokens.stream().allMatch(it -> {
            Matcher m = pattern.matcher(it);
            return m.matches();
        });
    }
}