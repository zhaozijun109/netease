package com.netease.easyml.common.collection.graph;

/**
 * Created by linjiuning on 2018/12/17.
 */
public class Edge extends Attribute {
    private Vertex in;
    private Vertex out;

    public Edge() {
    }

    public Edge(String inId, String outId) {
        setInId(inId);
        setOutId(outId);
    }

    public Edge(String inId, String outId, String label) {
        this(inId, outId);
        setLabel(label);
    }

    public Vertex getIn() {
        return in;
    }

    public void setIn(Vertex in) {
        this.in = in;
    }

    public Vertex getOut() {
        return out;
    }

    public void setOut(Vertex out) {
        this.out = out;
    }

    public void setInId(String inId) {
        putUnique(Constant.IN_ID, inId);
    }

    public void setOutId(String outId) {
        putUnique(Constant.OUT_ID, outId);
    }

    public String getInId() {
        return (String) get(Constant.IN_ID);
    }

    public String getOutId() {
        return (String) get(Constant.OUT_ID);
    }

    public void setLabel(String label) {
        putUnique(Constant.LABEL, label);
    }

    public String getLabel() {
        return (String) get(Constant.LABEL);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
