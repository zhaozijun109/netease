package com.netease.easyml.launcher;

import com.netease.easyml.annotation.Register;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.param.Params;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;

/**
 * Created by linjiuning on 2020/7/20.
 */
public class RegisterManager {
    private static final Logger log = LoggerFactory.getLogger(RegisterManager.class);
    private static final Set<String> PACKAGE_PREFIX = new LinkedHashSet<>(Arrays.asList(
            "com.netease.easyml",
            "org.apache.spark",
            "ml.dmlc.xgboost4j.scala.spark",
            "com.microsoft.azure.synapse"
    ));

    private static volatile boolean REGISTERED = false;

    public static synchronized void addPackagePrefix(String packagePrefix) {
        PACKAGE_PREFIX.add(packagePrefix);
    }

    public static synchronized void removePackagePrefix(String packagePrefix) {
        PACKAGE_PREFIX.remove(packagePrefix);
    }

    public synchronized static void register() {
        if (REGISTERED) {
            return;
        }

        register_();
        REGISTERED = true;
    }

    private synchronized static void register_() {
        List<URL> urls = new ArrayList<>();
        for (String path : PACKAGE_PREFIX) {
            urls.addAll(ClasspathHelper.forPackage(path));
        }
        Set<String> skip = new HashSet<>();

        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(urls)
                        .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner())
        );
        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(Register.class);
        for (Class<?> clazz : typesAnnotatedWith) {
            String canonicalName = clazz.getCanonicalName();
            if (skip.contains(canonicalName)) {
                continue;
            }
            skip.add(canonicalName);
            Register[] annotations = clazz.getAnnotationsByType(Register.class);
            for (Register annotation : annotations) {
                String name = annotation.name();
                if (name.isEmpty()) {
                    name = clazz.getSimpleName();
                }
                if (!annotation.prefix().isEmpty()) {
                    name = annotation.prefix() + name;
                }
                Class<?> parent = annotation.parent();
                if (parent == Object.class) {
                    parent = clazz.getSuperclass();
                    while (parent != Object.class && parent.getSuperclass() != Params.class &&
                            Params.class.isAssignableFrom(parent.getSuperclass())) {
                        parent = parent.getSuperclass();
                    }
                }
                FromParams.register(name, parent, (Class<? extends Params>) clazz, annotation.existOk(), annotation.isDefault());
                for (String alias : annotation.alias()) {
                    FromParams.register(alias, parent, (Class<? extends Params>) clazz);
                }
            }
        }

        reflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(urls)
                        .setScanners(new SubTypesScanner())
        );
        Set<Class<? extends PipelineStage>> subTypesOf = reflections.getSubTypesOf(PipelineStage.class);
        for (Class<? extends PipelineStage> clazz : subTypesOf) {
            if (Modifier.isAbstract(clazz.getModifiers())
                    || Modifier.isInterface(clazz.getModifiers())) {
                continue;
            }
            String canonicalName = clazz.getCanonicalName();
            if (skip.contains(canonicalName)) {
                continue;
            }
            skip.add(canonicalName);
            boolean flag = false;
            Constructor<?>[] constructors = clazz.getConstructors();
            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterCount() == 0) {
                    flag = true;
                    break;
                }
            }
            if (!flag && !Archive.isReadable(clazz)) {
                continue;
            }
            String name = clazz.getSimpleName();
            FromParams.register(name, PipelineStage.class, clazz);

        }
    }
}
