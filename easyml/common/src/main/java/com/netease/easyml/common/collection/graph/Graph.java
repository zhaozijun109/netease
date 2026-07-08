package com.netease.easyml.common.collection.graph;

import com.netease.easyml.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by linjiuning on 2018/12/17.
 */
public class Graph {
    private static final Logger log = LoggerFactory.getLogger(Graph.class);
    private List<Edge> edges = new ArrayList<>();
    private List<Vertex> vertices = new ArrayList<>();
    private Map<String, Vertex> vertexIndex = new HashMap<>();
    private AtomicInteger autoId = new AtomicInteger(1);

    private Graph() {
    }

    public Graph addVertex(Vertex vertex) {
        if (StringUtil.isEmpty(vertex.getId())) {
            Set<String> usedId = vertexIndex.keySet();
            String id;
            do {
                int genId = autoId.getAndIncrement();
                id = String.valueOf(genId);
            } while (usedId.contains(id));
            vertex.setId(id);
        } else if (findVertexById(vertex.getId()) != null) {
            log.error("A vertex with id " + vertex.getId() + " already exists");
            return this;
        }
        this.vertices.add(vertex);
        this.vertexIndex.put(vertex.getId(), vertex);
        vertex.setInEdges(new ArrayList<>());
        vertex.setOutEdges(new ArrayList<>());
        return this;
    }

    public Graph addEdge(Edge edge) {
        Vertex in = findVertexById(edge.getInId());
        Vertex out = findVertexById(edge.getOutId());

        if (in == null || out == null) {
            log.error("That edge's " + (edge.getIn() == null ? "out" : "in") + " vertex wasn't found");
            return this;
        }

        edge.setIn(in);
        edge.setOut(out);

        in.addInEdge(edge);
        out.addOutEdge(edge);
        this.edges.add(edge);
        return this;
    }

    public Graph addVertices(Vertex... vertices) {
        for (Vertex vertex : vertices)
            addVertex(vertex);
        return this;
    }

    public Graph addEdges(Edge... edges) {
        for (Edge edge : edges)
            addEdge(edge);
        return this;
    }

    public Vertex findVertexById(String id) {
        return vertexIndex.getOrDefault(id, null);
    }

    public List<Vertex> findVerticesByIds(String... ids) {
        List<Vertex> res = new ArrayList<>();
        for (String id : ids) {
            Vertex vertex = findVertexById(id);
            if (vertex != null)
                res.add(vertex);
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    public List<Vertex> findVertices(Object... args) {
        if (args.length == 0)
            return new ArrayList<>(this.vertices);
        else {
            List<Vertex> vertices = new ArrayList<>();
            for (Object arg : args) {
                if (arg instanceof Map) {
                    List<Vertex> res = this.searchVertices((Map<String, Object>) arg);
                    vertices.addAll(res);
                } else {
                    Vertex vertex = this.findVertexById((String) arg);
                    vertices.add(vertex);
                }
            }
            return vertices;
        }
    }

    public List<Vertex> searchVertices(Map<String, Object> filter) {
        return this.vertices.stream()
                .filter(it -> Helper.objectFilter(it, filter))
                .collect(Collectors.toList());
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public int getAutoId() {
        return autoId.get();
    }

    public void setAutoId(int autoId) {
        this.autoId = new AtomicInteger(autoId);
    }

    public Query v(Object... args){
        Query query = new Query(this);
        query.add(Constant.VERTEX, args);
        return query;
    }

    public static Graph create(List<Vertex> vertices, List<Edge> edges, int autoId) {
        Graph graph = new Graph();
        graph.setAutoId(autoId);
        vertices.forEach(graph::addVertex);
        edges.forEach(graph::addEdge);
        return graph;
    }

    public static Graph create(List<Vertex> vertices, List<Edge> edges) {
        return create(vertices, edges, 1);
    }
}
