package com.netease.easyml.common.collection.graph.pipetype;

import com.netease.easyml.common.collection.graph.Vertex;

/**
 * Created by linjiuning on 2018/12/17.
 */
@FunctionalInterface
public interface Filter {
    boolean test(Vertex vertex, Object gremlin);
}
