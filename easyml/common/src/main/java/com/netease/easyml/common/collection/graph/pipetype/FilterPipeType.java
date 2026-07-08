package com.netease.easyml.common.collection.graph.pipetype;

import com.netease.easyml.common.collection.graph.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by linjiuning on 2018/12/17.
 */
public class FilterPipeType implements PipeType {
    private static final Logger log = LoggerFactory.getLogger(FilterPipeType.class);

    static {
        Factory.addPipetype(Constant.FILTER, new FilterPipeType());
    }

    @Override
    public Object apply(Query query, Object[] args, Object maybeGremlin, State state) {
        if (maybeGremlin.equals(false)) return Constant.PULL;
        if (args == null || args.length == 0)
            return maybeGremlin;
        Gremlin gremlin = (Gremlin) maybeGremlin;

        Vertex vertex = gremlin.getVertex();
        // filter by object
        if (args[0] instanceof Map)
            return Helper.objectFilter(vertex, (Map) args[0])
                    ? gremlin : Constant.PULL;

        if (!(args[0] instanceof Filter)) {
            log.error("Filter arg is not a function: " + args[0]);
            // keep things moving
            return gremlin;
        }
        Filter filter = (Filter) args[0];
        // gremlin fails filter function
        if (!filter.test(vertex, gremlin)) return Constant.PULL;
        return gremlin;
    }
}
