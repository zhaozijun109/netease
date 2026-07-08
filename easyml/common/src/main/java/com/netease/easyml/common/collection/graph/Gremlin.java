package com.netease.easyml.common.collection.graph;

/**
 * Created by linjiuning on 2018/12/18.
 */
public class Gremlin {
    private Object action;
    private State state;
    private Vertex vertex;
    private Object result;

    public Object getAction() {
        return action;
    }

    public Gremlin setAction(Object action) {
        this.action = action;
        return this;
    }

    public State getState() {
        return state;
    }

    public Gremlin setState(State state) {
        this.state = state;
        return this;
    }

    public Vertex getVertex() {
        return vertex;
    }

    public Gremlin setVertex(Vertex vertex) {
        this.vertex = vertex;
        return this;
    }

    public Object getResult() {
        return result;
    }

    public Gremlin setResult(Object result) {
        this.result = result;
        return this;
    }
}
