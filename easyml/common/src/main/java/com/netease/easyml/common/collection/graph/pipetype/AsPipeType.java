package com.netease.easyml.common.collection.graph.pipetype;

import com.netease.easyml.common.collection.graph.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by eddielin on 2018/12/17.
 */
public class AsPipeType implements PipeType {
    static {
        Factory.addPipetype(Constant.AS, new AsPipeType());
    }

    @Override
    public Object apply(Query query, Object[] args, Object maybeGremlin, State state) {
        if (maybeGremlin.equals(false)) return Constant.PULL;
        // ignore any way
        if (args == null || args.length == 0) return maybeGremlin;
        Gremlin gremlin = (Gremlin) maybeGremlin;
        State gstate = gremlin.getState();
        if (gstate == null) {
            gstate = new State();
            gremlin.setState(gstate);
        }
        Map<Object, Vertex> as = gstate.getAs();
        if (as == null) {
            as = new HashMap<>();
            gstate.setAs(as);
        }
        // set label to the current vertex
        as.put(args[0], gremlin.getVertex());
        return gremlin;
    }
}
