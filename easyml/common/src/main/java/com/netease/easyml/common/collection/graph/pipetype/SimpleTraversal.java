package com.netease.easyml.common.collection.graph.pipetype;

import com.netease.easyml.common.collection.graph.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by linjiuning on 2018/12/17.
 */
public class SimpleTraversal implements PipeType {
    static {
        Factory.addPipetype(Constant.IN, new SimpleTraversal(Direction.IN));
        Factory.addPipetype(Constant.OUT, new SimpleTraversal(Direction.OUT));
        Factory.addPipetype(Constant.BOTH, new SimpleTraversal(Direction.BOTH));
    }

    public enum Direction {
        IN { // children

            @Override
            public List<Vertex> getVertices(Vertex vertex, Object filter) {
                List<Edge> edges = Helper.filterEdges(vertex.getInEdges(), filter);
                return edges.stream().map(Edge::getOut).collect(Collectors.toList());
            }
        },
        OUT { // parents

            @Override
            public List<Vertex> getVertices(Vertex vertex, Object filter) {
                List<Edge> edges = Helper.filterEdges(vertex.getOutEdges(), filter);
                return edges.stream().map(Edge::getIn).collect(Collectors.toList());
            }
        },
        BOTH {
            @Override
            public List<Vertex> getVertices(Vertex vertex, Object filter) {
                List<Edge> edges = new ArrayList<>();
                edges.addAll(vertex.getInEdges());
                edges.addAll(vertex.getOutEdges());
                edges = Helper.filterEdges(edges, filter);
                List<Vertex> vertices = new ArrayList<>();
                for (Edge edge : edges) {
                    vertices.add(edge.getIn());
                    vertices.add(edge.getOut());
                }
                return vertices;
            }
        };

        public abstract List<Vertex> getVertices(Vertex vertex, Object filter);
    }

    private Direction direction;

    public SimpleTraversal(Direction direction) {
        this.direction = direction;
    }

    @Override
    public Object apply(Query query, Object[] args, Object maybeGremlin, State state) {
        // query initialization
        if (maybeGremlin.equals(false) && (state.getTraversal() == null || state.getTraversal().isEmpty())) {
            return Constant.PULL;
        }

        // state initialization
        if (state.getTraversal() == null || state.getTraversal().isEmpty()) {
            Gremlin gremlin = (Gremlin) maybeGremlin;
            state.setGremlin(gremlin);
            Vertex vertex = gremlin.getVertex();
            // get edges that match our query
            List<Vertex> vertices = direction.getVertices(vertex, args);
            state.setTraversal(vertices);
        }

        List<Vertex> vertices = state.getTraversal();
        // all done
        if (vertices.isEmpty())
            return Constant.PULL;

        // use up an edge
        Vertex vertex = vertices.remove(vertices.size() - 1);
        return Helper.gotoVertex(state.getGremlin(), vertex);
    }
}
