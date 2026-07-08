package com.netease.easyml.common.collection.graph.pipetype;

import com.netease.easyml.common.collection.graph.*;

import java.util.List;

/**
 * Created by linjiuning on 2018/12/17.
 */
public class VertexPipeType implements PipeType {
    static {
        PipeType pipeType = new VertexPipeType();
        Factory.addPipetype(Constant.VERTEX, pipeType);
        Factory.addPipetype(Constant.SHORT_VERTEX, pipeType);
    }

    @Override
    public Object apply(Query query, Object[] args, Object maybeGremlin, State state) {
        if (state.getVertices() == null) {
            List<Vertex> vertices = query.getGraph().findVertices(args);
            state.setVertices(vertices);
        }

        List<Vertex> vertices = state.getVertices();
        if (vertices.isEmpty())
            return Constant.DONE;

        Vertex vertex = vertices.remove(vertices.size() - 1);
        return Helper.makeGremlin(vertex, maybeGremlin.equals(false) ? new State() : ((Gremlin) maybeGremlin).getState());
    }
}
