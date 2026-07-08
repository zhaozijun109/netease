package com.netease.easyml.common.collection.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linjiuning on 2018/12/17.
 */
public class Vertex extends Attribute {
    private List<Edge> inEdges; // 出节点
    private List<Edge> outEdges; // 入节点

    public Vertex() {
    }

    public Vertex(String id) {
        setId(id);
    }

    public List<Edge> getInEdges() {
        return inEdges;
    }

    public void setInEdges(List<Edge> inEdges) {
        this.inEdges = inEdges;
    }

    public List<Edge> getOutEdges() {
        return outEdges;
    }

    public void setOutEdges(List<Edge> outEdges) {
        this.outEdges = outEdges;
    }

    public Vertex addInEdge(Edge edge) {
        this.inEdges.add(edge);
        return this;
    }

    public Vertex addOutEdge(Edge edge) {
        this.outEdges.add(edge);
        return this;
    }

    public List<Edge> getEdges() {
        List<Edge> edges = new ArrayList<>();
        if (inEdges != null)
            edges.addAll(inEdges);
        if (outEdges != null)
            edges.addAll(outEdges);
        return edges;
    }

    public void setId(String id) {
        putUnique(Constant.ID, id);
    }

    public String getId() {
        return (String) get(Constant.ID);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
