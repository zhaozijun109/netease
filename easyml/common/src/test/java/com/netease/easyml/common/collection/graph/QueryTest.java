package com.netease.easyml.common.collection.graph;

import com.netease.easyml.common.collection.graph.pipetype.Filter;
import com.netease.easyml.common.collection.graph.pipetype.SimpleTraversal;
import com.netease.easyml.common.util.StringUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linjiuning on 2018/12/17.
 */
public class QueryTest {
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

        Edge e1 = new Edge("10", "1");
        e1.put("label", "knows");

        graph = Graph.create(Arrays.asList(v1, v2), Arrays.asList(e1));

        Vertex v3 = new Vertex("charlie");
        v3.put("name", "charlie");

        Vertex v4 = new Vertex("30");
        v4.put("name", "delta");

        Edge e2 = new Edge("30", "10", "parent");

        Edge e3 = new Edge("charlie", "10", "knows");

        Vertex v5 = new Vertex("3");
        v5.put("name", "tom");

        Edge e4 = new Edge("3", "30", "knows");

        Edge e5 = new Edge("3", "1", "knows");

        graph.addVertex(v3);
        graph.addVertex(v4);
        graph.addVertex(v5);
        graph.addEdges(e2, e3, e4, e5);
    }

    @Test
    public void run() throws Exception {
        List<Object> result = graph.v("1")
                .add(Constant.OUT, "knows")
                .add(Constant.OUT).run();
        result.forEach(System.out::println);
    }

    @Test
    public void take() throws Exception {
        Query q = graph.v("1")
                .add(Constant.PARENTS, "knows")
                .add(Constant.PARENTS)
                .add(Constant.TAKE, 1);
        List<Object> result = q.run();
        System.out.println(StringUtil.join(result, ","));

        result = q.run();
        System.out.println(StringUtil.join(result, ","));

        result = q.run();
        System.out.println(StringUtil.join(result, ","));
    }

    @Test
    public void property() throws Exception {
        List<Object> result = graph.v("1")
                .add(Constant.OUT, "knows")
                .add(Constant.OUT)
                .add(Constant.PROPERTY, "name")
                .run();
        result.forEach(System.out::println);
    }

    @Test
    public void back() throws Exception {
        List<Object> result = graph.v("1")
                .add(Constant.OUT, "knows")
                .add(Constant.AS, "v2")
                .add(Constant.OUT)
                .add(Constant.BACK, "v2")
                .add(Constant.PROPERTY, "name")
                .run();
        result.forEach(System.out::println);
    }

    @Test
    public void unique() throws Exception {
        List<Object> result = graph.v("1")
                .add(Constant.OUT, "knows")
                .add(Constant.AS, "v2")
                .add(Constant.OUT)
                .add(Constant.BACK, "v2")
                .add(Constant.UNIQUE)
                .add(Constant.PROPERTY, "name")
                .run();
        result.forEach(System.out::println);
    }

    @Test
    public void merge() throws Exception {
        List<Object> result = graph.v("1")
                .add(Constant.OUT, "knows")
                .add(Constant.AS, "v2")
                .add(Constant.OUT)
                .add(Constant.AS, "vo")
                .add(Constant.MERGE, "v2", "vo")
                .add(Constant.UNIQUE)
                .run();
        result.forEach(System.out::println);
    }

    @Test
    public void except() throws Exception {
        List<Object> result = graph.v("1")
                .add(Constant.OUT, "knows")
                .add(Constant.AS, "v2")
                .add(Constant.OUT)
                .add(Constant.AS, "vo")
                .add(Constant.MERGE, "v2", "vo")
                .add(Constant.EXCEPT, "v2")
                .run();
        result.forEach(System.out::println);
    }

    @Test
    public void filter() throws Exception {
        Map<String, String> cond = new HashMap<>();
        cond.put("name", "delta");
        List<Object> result = graph.v("1")
                .add(Constant.OUT, "knows")
                .add(Constant.AS, "v2")
                .add(Constant.OUT)
                .add(Constant.AS, "vo")
                .add(Constant.MERGE, "v2", "vo")
                .add(Constant.UNIQUE)
                .add(Constant.FILTER, (Filter) (vertex, gremlin) -> vertex.getOrDefault("name", "").equals("delta"))
//                .add(Constant.FILTER, cond)
                .run();
        result.forEach(System.out::println);
    }

    @Test
    public void bfs() {
        graph.findVertexById("1").getId();
        List<Vertex> vertices = Helper.bfs(graph, "1", null, SimpleTraversal.Direction.OUT, (vtx) -> vtx.size() >= 4);
        vertices.forEach(System.out::println);
    }

    @Test
    public void dfs() {
        List<Vertex> vertices = Helper.dfs(graph, "1", null, SimpleTraversal.Direction.OUT, (vtx) -> vtx.size() >= 4);
        vertices.forEach(System.out::println);
    }
}