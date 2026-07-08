package com.netease.easyml.common.collection.graph;

import com.netease.easyml.common.collection.graph.pipetype.SimpleTraversal;
import com.netease.easyml.common.util.JacksonUtil;
import com.netease.easyml.common.util.Lazy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by linjiuning on 2018/12/17.
 */
public class Helper {
    private static final Logger log = LoggerFactory.getLogger(Helper.class);
    private static final Lazy<Vertex> START = new Lazy<>(() -> new Vertex(Constant.START));
    private static final Lazy<Vertex> END = new Lazy<>(() -> new Vertex(Constant.END));

    public static boolean objectFilter(Map thing, Map filter) {
        for (Object key : filter.keySet())
            if (!thing.containsKey(key) || !Objects.deepEquals(thing.get(key), filter.get(key)))
                return false;

        return true;
    }

    public static String jsonify(Graph graph) {
        return jsonify(graph, false);
    }

    public static String jsonify(Graph graph, boolean pretty) {
        Map<String, Object> bean = new HashMap<>();
        List<String> vertexStr = graph.getVertices().stream().map(Vertex::toString).collect(Collectors.toList());
        bean.put(Constant.VERTEX, vertexStr);
        List<String> edgeStr = graph.getEdges().stream().map(Edge::toString).collect(Collectors.toList());
        bean.put(Constant.EDGE, edgeStr);
        bean.put(Constant.AUTO_ID, graph.getAutoId());
        String json = JacksonUtil.beanToJson(bean, pretty);
        return json == null ? "" : json;
    }

    @SuppressWarnings("unchecked")
    public static Graph fromString(String json) {
        Map<String, Object> bean = JacksonUtil.jsonToBean(json, Map.class);
        List<Vertex> vertices = new ArrayList<>();
        for (String attrStr : (List<String>) bean.get(Constant.VERTEX)) {
            Map attr = JacksonUtil.jsonToBean(attrStr, Map.class);
            Vertex vertex = new Vertex();
            attr.forEach(vertex::put);
            vertices.add(vertex);
        }

        List<Edge> edges = new ArrayList<>();
        for (String attrStr : (List<String>) bean.get(Constant.EDGE)) {
            Map attr = JacksonUtil.jsonToBean(attrStr, Map.class);
            Edge edge = new Edge();
            attr.forEach(edge::put);
            edges.add(edge);
        }
        int autoId = (int) bean.get(Constant.AUTO_ID);
        return Graph.create(vertices, edges, autoId);
    }

    public static Vertex startVertex() {
        return START.getOrCompute();
    }

    public static boolean isStartVertex(Vertex vertex) {
        return vertex != null && Objects.equals(vertex.getId(), Constant.START);
    }

    public static Vertex endVertex() {
        return END.getOrCompute();
    }

    public static boolean isEndVertex(Vertex vertex) {
        return vertex != null && Objects.equals(vertex.getId(), Constant.END);
    }

    public static boolean isStartOrEndVertex(Vertex vertex) {
        return isStartVertex(vertex) || isEndVertex(vertex);
    }

    public static boolean isStartOrEndVertex(String vertex) {
        return Objects.equals(vertex, Constant.START) || Objects.equals(vertex, Constant.END);
    }

    public static Gremlin makeGremlin(Vertex vertex, State state) {
        Gremlin gremlin = new Gremlin();
        gremlin.setVertex(vertex);
        gremlin.setState(state != null ? state : new State());
        return gremlin;
    }

    public static Gremlin gotoVertex(Gremlin gremlin, Vertex vertex) {
        return makeGremlin(vertex, gremlin.getState());
    }

    @SuppressWarnings("unchecked")
    public static List<Edge> filterEdges(List<Edge> edges, Object filter) {
        return edges.stream().filter((edge) -> {
            // if there's no filter, everything is valid
            if (filter == null)
                return true;

            // if the filter is a string, the label must match
            if (filter instanceof String)
                return edge.getLabel().equals(filter);

            // if the filter is an array, the label must be in it
            if (filter instanceof Object[]) {
                boolean empty = true;
                for (Object obj : (Object[]) filter) {
                    empty = false;
                    if (obj.equals(edge.getLabel()))
                        return true;
                }
                return empty;
            }

            if (filter instanceof Predicate)
                return ((Predicate) filter).test(edge);

            // try the filter as an object
            return objectFilter(edge, (Map) filter);
        }).collect(Collectors.toList());
    }

    public static List<Vertex> bfs(Graph graph, String id, Object filter, SimpleTraversal.Direction direction) {
        return bfs(graph, id, filter, direction, null);
    }

    public static List<Vertex> bfs(Vertex vertex, Object filter, SimpleTraversal.Direction direction) {
        return bfs(vertex, filter, direction, null);
    }

    public static List<Vertex> bfs(Graph graph, String id, Object filter, SimpleTraversal.Direction direction, EarlyStop earlyStop) {
        Vertex vertex = graph.findVertexById(id);
        return bfs(vertex, filter, direction, earlyStop);
    }

    /**
     * bf search
     *
     * @param vertex:    start vertex
     * @param filter:    edge filter, can be string, map, function
     * @param direction: search direction
     * @param earlyStop: function, control early stop
     * @return vertices, don't contain start vertex
     */
    public static List<Vertex> bfs(Vertex vertex, Object filter, SimpleTraversal.Direction direction, EarlyStop earlyStop) {
        List<Vertex> vertices = new ArrayList<>();
        Set<String> uniq = new HashSet<>();
        if (vertex != null) {
            Queue<Vertex> queue = new LinkedList<>();
            queue.offer(vertex);
            while (!queue.isEmpty()) {
                if (earlyStop != null && earlyStop.test(vertices))
                    break;
                Vertex vtx = queue.poll();
                if (vtx != vertex)
                    vertices.add(vtx);
                uniq.add(vtx.getId());
                List<Vertex> nearVtx = direction.getVertices(vtx, filter);
                for (Vertex v : nearVtx) {
                    if (v != null && !uniq.contains(v.getId()))
                        queue.offer(v);
                }
            }
        }
        return vertices;
    }

    public static List<Vertex> dfs(Graph graph, String id, Object filter, SimpleTraversal.Direction direction) {
        return dfs(graph, id, filter, direction, null);
    }

    public static List<Vertex> dfs(Vertex vertex, Object filter, SimpleTraversal.Direction direction) {
        return dfs(vertex, filter, direction, null);
    }

    public static List<Vertex> dfs(Graph graph, String id, Object filter, SimpleTraversal.Direction direction, EarlyStop earlyStop) {
        Vertex vertex = graph.findVertexById(id);
        return dfs(vertex, filter, direction, earlyStop);
    }

    /**
     * df search
     *
     * @param vertex:    start vertex
     * @param filter:    edge filter, can be string, map, function
     * @param direction: search direction
     * @param earlyStop: function, control early stop
     * @return vertices, don't contain start vertex
     */
    public static List<Vertex> dfs(Vertex vertex, Object filter, SimpleTraversal.Direction direction, EarlyStop earlyStop) {
        List<Vertex> vertices = new ArrayList<>();
        Set<String> uniq = new HashSet<>();
        if (vertex != null) {
            Stack<Vertex> stack = new Stack<>();
            stack.push(vertex);
            while (!stack.isEmpty()) {
                if (earlyStop != null && earlyStop.test(vertices))
                    break;
                Vertex vtx = stack.pop();
                if (vtx != vertex)
                    vertices.add(vtx);
                uniq.add(vtx.getId());
                List<Vertex> nearVtx = direction.getVertices(vtx, filter);
                for (Vertex v : nearVtx) {
                    if (v != null && !uniq.contains(v.getId()))
                        stack.push(v);
                }
            }
        }
        return vertices;
    }

    public static List<Vertex> topological(Graph graph, String id, Object filter, SimpleTraversal.Direction direction) {
        return topological(graph, id, filter, direction, null);
    }

    public static List<Vertex> topological(Vertex vertex, Object filter, SimpleTraversal.Direction direction) {
        return topological(vertex, filter, direction, null);
    }

    public static List<Vertex> topological(Graph graph, String id, Object filter, SimpleTraversal.Direction direction, EarlyStop earlyStop) {
        Vertex vertex = graph.findVertexById(id);
        return topological(vertex, filter, direction, earlyStop);
    }

    public static List<Vertex> topological(Vertex vertex, Object filter, SimpleTraversal.Direction direction, EarlyStop earlyStop) {
        Set<String> visited = new HashSet<>();
        List<Vertex> vertices = new ArrayList<>();

        topological(vertex, filter, direction, earlyStop, visited, vertices);
        return vertices;
    }

    private static boolean topological(Vertex vertex, Object filter, SimpleTraversal.Direction direction,
                                       EarlyStop earlyStop, Set<String> visited, List<Vertex> vertices) {
        if (vertex == null || visited.contains(vertex.getId()))
            return true;

        if (earlyStop != null && earlyStop.test(vertices))
            return false;

        visited.add(vertex.getId());
        List<Vertex> nearVtx = direction.getVertices(vertex, filter);
        boolean noStop = true;
        for (Vertex v : nearVtx) {
            noStop = topological(v, filter, direction, earlyStop, visited, vertices);
            if (!noStop)
                break;
        }
        vertices.add(0, vertex);
        return noStop;
    }
}
