package com.netease.easyml.common.collection.graph;

/**
 * Created by linjiuning on 2018/12/18.
 */
public class Program {
    private String fun;
    private Object[] args;

    public Program(String fun, Object[] args) {
        this.fun = fun;
        this.args = args;
    }

    public String getFun() {
        return fun;
    }

    public void setFun(String fun) {
        this.fun = fun;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public static Program program(String fun, Object[] args) {
        return new Program(fun, args);
    }
}
