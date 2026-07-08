package com.netease.easyml.common.collection.graph.pipetype;

import com.netease.easyml.common.collection.graph.*;

/**
 * Created by linjiuning on 2018/12/17.
 */
public class PropertyPipeType implements PipeType {
    static {
        Factory.addPipetype(Constant.PROPERTY, new PropertyPipeType());
    }

    @Override
    public Object apply(Query query, Object[] args, Object maybeGremlin, State state) {
        if (maybeGremlin.equals(false)) return Constant.PULL;
        if (args == null || args.length == 0) return false;
        Gremlin gremlin = (Gremlin) maybeGremlin;
        Object result = gremlin.getVertex().getOrDefault(args[0], null);
        gremlin.setResult(result);
        // undefined or null properties kill the gremlin
        return result == null ? false : gremlin;
    }
}
