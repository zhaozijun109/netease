package com.netease.easyml.common.collection.graph;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linjiuning on 2018/12/17.
 */
public class GraphTest {
    private Graph graph;

    @Before
    public void setUp() throws Exception {
        Vertex v1 = new Vertex();
        v1.put("name", "alice");

        Vertex v2 = new Vertex("10");
        v2.put("name", "bob");

        Map<String, Object> hobbie = new HashMap<>();
        hobbie.put("x", 3);
        v2.put("hobbies", new Object[]{"asdf", hobbie});

        Vertex v3 = new Vertex();
        v3.put("name", "tom");

        Edge e1 = new Edge("10", "1");
        e1.put("label", "knows");

        Edge e2 = new Edge("1", "2");

        Edge e3 = new Edge("2", "10");

        graph = Graph.create(Arrays.asList(v1, v2, v3), Arrays.asList(e1, e2, e3));
    }

    @Test
    public void jsonify() {
        String json = Helper.jsonify(graph);
        System.out.println(json);

        Graph newGraph = Helper.fromString(json);
        System.out.println(Helper.jsonify(newGraph));
    }

    @Test
    public void findVertexById() {
        System.out.println(graph.findVertexById("10"));
        System.out.println(graph.findVertexById("1"));
    }

    @Test
    public void findVertices() {
        Map<String, Object> query = new HashMap<>();
        query.put("name", "alice");
        List<Vertex> vertices = graph.findVertices("10", query, "2");
        for (Vertex vertex : vertices) {
            System.out.println("Vertex:");
            System.out.println(vertex);
            System.out.println("InEdges:");
            vertex.getInEdges().forEach(System.out::println);
            System.out.println("OutEdges:");
            vertex.getOutEdges().forEach(System.out::println);
            System.out.println();
        }
    }
}