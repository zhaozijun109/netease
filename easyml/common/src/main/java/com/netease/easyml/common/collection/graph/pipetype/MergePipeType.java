package com.netease.easyml.common.collection.graph.pipetype;

import com.netease.easyml.common.collection.graph.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linjiuning on 2018/12/17.
 */
public class MergePipeType implements PipeType {
    static {
        Factory.addPipetype(Constant.MERGE, new MergePipeType());
    }

    @Override
    public Object apply(Query query, Object[] args, Object maybeGremlin, State state) {
        List<Vertex> vertices = state.getVertices();
        if (maybeGremlin.equals(false) && vertices == null)
            return Constant.PULL;

        if (vertices == null)
            state.setGremlin((Gremlin) maybeGremlin);

        // state initialization
        if (vertices == null || vertices.isEmpty()) {
            Map as = null;
            if (maybeGremlin instanceof Gremlin) {
                Gremlin gremlin = (Gremlin) maybeGremlin;
                State gstate = gremlin.getState();
                if (gstate == null)
                    gstate = new State();
                as = gstate.getAs();
            }
            if (as == null)
                as = new HashMap();
            vertices = new ArrayList<>();
            for (Object arg : args) {
                if (!as.containsKey(arg))
                    continue;
                Vertex vertex = (Vertex) as.get(arg);
                vertices.add(vertex);
            }
            state.setVertices(vertices);
        }

        vertices = state.getVertices();
        // done with this batch
        if (vertices.isEmpty())
            return Constant.PULL;

        Vertex vertex = vertices.remove(vertices.size() - 1);
        return Helper.makeGremlin(vertex, state.getGremlin().getState());
    }
}
