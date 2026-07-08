package com.netease.easyml.local.mllib.processor.cleaner.impl;

import com.netease.easyml.local.mllib.processor.cleaner.ICleaner;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by eddielin on 2018/7/11.
 */
public class PipeLineCleaner implements ICleaner {
    Collection<ICleaner> cleaners;

    public PipeLineCleaner(ICleaner... cleaners) {
        this.cleaners = Stream.of(cleaners).collect(Collectors.toList());
    }

    public PipeLineCleaner(Collection<ICleaner> cleaners) {
        this.cleaners = cleaners;
    }

    @Override
    public String replace(String repl, String text) {
        return ICleaner.clean(cleaners, repl, text);
    }
}
