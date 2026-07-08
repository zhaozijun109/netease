package com.netease.easyml.common.util;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public final class Lazy<T> {
    private volatile T value;
    private Supplier<T> supplier;

    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T getOrCompute() {
        final T result = value; // Just one volatile read
        return result == null ? maybeCompute(supplier) : result;
    }

    private synchronized T maybeCompute(Supplier<T> supplier) {
        if (value == null) {
            value = requireNonNull(supplier.get());
        }
        return value;
    }
}
