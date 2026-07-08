package com.netease.easyml.common.collection.graph;

import com.netease.easyml.common.collection.graph.pipetype.PipeType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by linjiuning on 2018/12/17.
 */
public class Query {
    private Graph graph;
    private List<Program> program;
    private List<State> state;

    public Query(Graph graph) {
        this.graph = graph;
        this.program = new ArrayList<>();
        this.state = new ArrayList<>();
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public List<Program> getProgram() {
        return program;
    }

    public void setProgram(List<Program> program) {
        this.program = program;
    }

    public List<State> getState() {
        return state;
    }

    public void setState(List<State> state) {
        this.state = state;
    }

    public Query add(String name, Object... args) {
        program.add(Program.program(name, args));
        state.add(new State());
        return this;
    }

    public List<Object> run() {
        program = Factory.transform(program);
        int max = program.size() - 1;
        int pc = max;
        int done = -1;
        Object maybeGremlin = false;
        List<Gremlin> results = new ArrayList<>();

        while (done < max) {
            Program tuple = program.get(pc);
            PipeType pipeType = Factory.getPipetype(tuple.getFun());
            Object[] args = tuple.getArgs();
            if (state.get(pc) == null) {
                state.set(pc, new State());
            }
            maybeGremlin = pipeType.apply(this, args, maybeGremlin, state.get(pc));

            if (maybeGremlin.equals(Constant.PULL)) {
                maybeGremlin = false;
                if (pc - 1 > done) {
                    pc--;
                    continue;
                } else
                    done = pc;
            } else if (maybeGremlin.equals(Constant.DONE)) {
                maybeGremlin = false;
                done = pc;
            }

            pc++;
            if (pc > max) {
                if (!maybeGremlin.equals(false)) {
                    results.add((Gremlin) maybeGremlin);
                }
                maybeGremlin = false;
                pc--;
            }
        }

        // return either results (like property('name')) or vertices
        return results.stream().map((tgremlin) -> tgremlin.getResult() != null
                ? tgremlin.getResult() : tgremlin.getVertex()).collect(Collectors.toList());
    }
}
