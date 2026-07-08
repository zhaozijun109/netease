package com.netease.lofter.tango.impl.entity;

import lombok.Getter;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Getter
public class PageDO<T> implements Serializable {

    private static final long serialVersionUID = 690219237762728679L;

    private final int total;

    private final List<T> list;

    public PageDO(int total, List<T> list) {
        this.total = total;
        this.list = list;
    }

    public static <T> PageDO<T> empty() {
        return new PageDO<>(0, Collections.emptyList());
    }
}
