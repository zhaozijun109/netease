package com.netease.easyml.local.mllib.processor.filter.impl;


import com.netease.easyml.local.mllib.processor.filter.IFilter;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by eddielin on 2018/6/8.
 */
public class PipeLineFilter implements IFilter {
    private Collection<IFilter> filters;

    public PipeLineFilter(IFilter... filters) {
        this.filters = Stream.of(filters).collect(Collectors.toList());
    }

    public PipeLineFilter(Collection<IFilter> filters) {
        this.filters = filters;
    }

    @Override
    public boolean filter(String text) {
        return IFilter.filter(filters, text);
    }

    @Override
    public boolean filter(Collection<String> tokens) {
        return IFilter.filter(filters, tokens);
    }
}