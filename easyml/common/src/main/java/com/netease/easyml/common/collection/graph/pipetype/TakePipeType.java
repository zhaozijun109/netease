package com.netease.easyml.common.collection.graph.pipetype;

import com.netease.easyml.common.collection.graph.Constant;
import com.netease.easyml.common.collection.graph.Factory;
import com.netease.easyml.common.collection.graph.Query;
import com.netease.easyml.common.collection.graph.State;

/**
 * Created by linjiuning on 2018/12/17.
 */
public class TakePipeType implements PipeType {
    static {
        Factory.addPipetype(Constant.TAKE, new TakePipeType());
    }

    @Override
    public Object apply(Query query, Object[] args, Object maybeGremlin, State state) {
        Integer taken = state.getTaken();
        if (taken == null)
            taken = 0;
        if (taken.equals((args == null || args.length == 0) ? -1 : args[0])) {
            taken = 0;
            state.setTaken(taken);
            return Constant.DONE;
        }

        if (maybeGremlin.equals(false)) return Constant.PULL;
        state.setTaken(taken + 1);
        return maybeGremlin;
    }
}
