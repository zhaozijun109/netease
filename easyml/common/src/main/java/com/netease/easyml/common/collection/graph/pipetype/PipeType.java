package com.netease.easyml.common.collection.graph.pipetype;

import com.netease.easyml.common.collection.graph.Query;
import com.netease.easyml.common.collection.graph.State;

/**
 * Created by linjiuning on 2018/12/17.
 */
@FunctionalInterface
public interface PipeType {
    Object apply(Query query, Object[] args, Object maybeGremlin, State state);
}
