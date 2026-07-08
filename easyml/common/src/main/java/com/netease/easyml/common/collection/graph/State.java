package com.netease.easyml.common.collection.graph;

import java.util.List;
import java.util.Map;

/**
 * Created by linjiuning on 2018/12/18.
 */
public class State extends Attribute {
    private Gremlin gremlin;
    private List<Vertex> vertices;
    private List<Vertex> traversal;
    private Integer taken;
    private Map<Object, Vertex> as;

    public Gremlin getGremlin() {
        return gremlin;
    }

    public void setGremlin(Gremlin gremlin) {
        this.gremlin = gremlin;
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public void setVertices(List<Vertex> vertices) {
        this.vertices = vertices;
    }

    public List<Vertex> getTraversal() {
        return traversal;
    }

    public void setTraversal(List<Vertex> traversal) {
        this.traversal = traversal;
    }

    public Integer getTaken() {
        return taken;
    }

    public void setTaken(Integer taken) {
        this.taken = taken;
    }

    public Map<Object, Vertex> getAs() {
        return as;
    }

    public void setAs(Map<Object, Vertex> as) {
        this.as = as;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
