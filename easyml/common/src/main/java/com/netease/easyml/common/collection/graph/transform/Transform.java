package com.netease.easyml.common.collection.graph.transform;

import com.netease.easyml.common.collection.graph.Program;

import java.util.List;

/**
 * Created by linjiuning on 2018/12/18.
 */
@FunctionalInterface
public interface Transform {
    List<Program> apply(List<Program> program);
}
