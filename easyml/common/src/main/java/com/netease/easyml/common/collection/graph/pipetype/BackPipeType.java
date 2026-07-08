package com.netease.easyml.common.collection.graph.pipetype;

import com.netease.easyml.common.collection.graph.*;

/**
 * Created by eddielin on 2018/12/17.
 */
public class BackPipeType implements PipeType {
    static {
        Factory.addPipetype(Constant.BACK, new BackPipeType());
    }

    @Override
    public Object apply(Query query, Object[] args, Object maybeGremlin, State state) {
        if (maybeGremlin.equals(false) || args == null || args.length == 0) return Constant.PULL;
        Gremlin gremlin = (Gremlin) maybeGremlin;
        State gstate = gremlin.getState();
        if(gstate == null || gstate.getAs() == null || !gstate.getAs().containsKey(args[0])) return Constant.PULL;
        return Helper.gotoVertex(gremlin, gstate.getAs().get(args[0]));
    }
}
