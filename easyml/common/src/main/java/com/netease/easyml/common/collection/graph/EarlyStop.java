package com.netease.easyml.common.collection.graph;

import java.util.List;

/**
 * Created by linjiuning on 2018/12/21.
 */
@FunctionalInterface
public interface EarlyStop {
    boolean test(List<Vertex> vertices);
}
