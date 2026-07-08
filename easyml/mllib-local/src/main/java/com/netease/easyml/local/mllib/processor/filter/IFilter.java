package com.netease.easyml.local.mllib.processor.filter;

import java.util.Collection;

/**
 * Created by eddielin on 2018/6/2.
 */
public interface IFilter {
    /**
     * Check whether text matches the given condition
     *
     * @param text
     * @return matched
     */
    boolean filter(String text);

    @Deprecated
    boolean filter(Collection<String> tokens);

    /**
     * Check whether any filter matched
     *
     * @param filters
     * @param text
     * @return Any matched
     */
    static boolean filter(Collection<IFilter> filters, String text) {
        for (IFilter filter : filters) {
            if (filter.filter(text))
                return true;
        }
        return false;
    }

    @Deprecated
    static boolean filter(Collection<IFilter> filters, Collection<String> tokens) {
        for (IFilter filter : filters) {
            if (filter.filter(tokens))
                return true;
        }
        return false;
    }
}
