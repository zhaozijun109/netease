package com.netease.easyml.common.collection.graph.pipetype;

import com.netease.easyml.common.collection.graph.*;

import java.util.Map;
import java.util.Objects;

/**
 * Created by linjiuning on 2018/12/17.
 */
public class ExceptPipeType implements PipeType {
    static {
        Factory.addPipetype(Constant.EXCEPT, new ExceptPipeType());
    }

    @Override
    public Object apply(Query query, Object[] args, Object maybeGremlin, State state) {
        if (maybeGremlin.equals(false)) return Constant.PULL;
        // ignore any way
        if (args == null || args.length == 0) return maybeGremlin;
        Gremlin gremlin = (Gremlin) maybeGremlin;
        State gstate = gremlin.getState();
        if (gstate == null || gstate.getAs() == null || !gstate.getAs().containsKey(args[0])) return gremlin;
        Map as = gstate.getAs();
        if (as.containsKey(args[0]) && Objects.equals(gremlin.getVertex(), as.get(args[0])))
            return Constant.PULL;
        return gremlin;
    }
}
