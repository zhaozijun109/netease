package com.netease.easyml.common.collection.graph.pipetype;

import com.netease.easyml.common.collection.graph.*;

/**
 * Created by linjiuning on 2018/12/17.
 */
public class UniquePipeType implements PipeType {
    static {
        Factory.addPipetype(Constant.UNIQUE, new UniquePipeType());
    }

    @Override
    public Object apply(Query query, Object[] args, Object maybeGremlin, State state) {
        if (maybeGremlin.equals(false)) return Constant.PULL;
        Gremlin gremlin = (Gremlin) maybeGremlin;
        Vertex vertex = gremlin.getVertex();
        // we've seen this gremlin, so get another instead
        if (state.containsKey(vertex.getId()))
            return Constant.PULL;
        state.put(vertex.getId(), true);
        return gremlin;
    }
}
