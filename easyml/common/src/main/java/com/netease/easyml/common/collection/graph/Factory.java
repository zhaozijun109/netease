package com.netease.easyml.common.collection.graph;

import com.netease.easyml.common.collection.graph.pipetype.PipeType;
import com.netease.easyml.common.collection.graph.transform.Transform;
import org.javatuples.Pair;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linjiuning on 2018/12/17.
 */
public class Factory {
    private static final Logger log = LoggerFactory.getLogger(Factory.class);
    private static Map<String, PipeType> PROTOTYPES = new HashMap<>();
    private static List<Pair<Transform, Double>> TRANSFORMS = new ArrayList<>();

    private static PipeType fauxPipeType = (query, args, gremlin, state) -> gremlin != null ? gremlin : Constant.PULL;

    static {
        addPipetype(Constant.FAUX, fauxPipeType);

        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage("com.netease.easyml"))
                        .setScanners(new SubTypesScanner())
        );
        for (Class clazz : reflections.getSubTypesOf(PipeType.class)) {
            try {
                Class.forName(clazz.getName());
            } catch (ClassNotFoundException e) {
                log.error("ClassNotFoundException： " + e.getMessage());
            }
        }

        for (Class clazz : reflections.getSubTypesOf(Transform.class)) {
            try {
                Class.forName(clazz.getName());
            } catch (ClassNotFoundException e) {
                log.error("ClassNotFoundException： " + e.getMessage());
            }
        }

        addAlias(Constant.PARENTS, Constant.OUT);
        addAlias(Constant.CHILDREN, Constant.IN);
        addAlias(Constant.NEIGHBOR, Constant.BOTH);
    }

    public static void addPipetype(String name, PipeType prototype) {
        if (PROTOTYPES.containsKey(name)) {
            log.error("Name: " + name + " already exist...");
            return;
        }
        PROTOTYPES.put(name, prototype);
    }

    public static PipeType getPipetype(String name) {
        if (!PROTOTYPES.containsKey(name))
            log.error("Unrecognized pipe type: " + name);
        return PROTOTYPES.getOrDefault(name, fauxPipeType);
    }

    public static void addTransform(Transform transform, double priority) {
        int i = 0;
        for (; i < TRANSFORMS.size(); i++) {
            if (priority > TRANSFORMS.get(i).getValue1())
                break;
        }
        TRANSFORMS.add(i, new Pair<>(transform, priority));
    }

    public static void addAlias(String newName, String oldName) {
        addPipetype(newName, (query, args, gremlin, state) -> gremlin);
        addTransform(program -> {
            List<Program> res = new ArrayList<>();
            for (Program step : program) {
                if (step.getFun().equals(newName))
                    res.add(Program.program(oldName, step.getArgs()));
                else
                    res.add(step);
            }
            return res;
        }, 100);
    }

    public static List<Program> transform(List<Program> program) {
        for (Pair<Transform, Double> tran : TRANSFORMS) {
            program = tran.getValue0().apply(program);
        }
        return program;
    }
}
