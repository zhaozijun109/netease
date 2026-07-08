package com.netease.easyml.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.function.Supplier;

/**
 * Created by linjiuning on 2020/7/14.
 */
public class JavaUtil {
    private static final Logger log = LoggerFactory.getLogger(JavaUtil.class);

    public static void addJarsToClassPath(String jars) {
        URLClassLoader classLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        addJarsToClassPath(jars, classLoader);
    }

    public static void addJarsToClassPath(String jars, URLClassLoader classLoader) {
        try {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            for (String jar : jars.split(",")) {
                jar = jar.trim();
                if (jar.isEmpty()) {
                    continue;
                }
                try {
                    log.info("Add jar: " + jar);
                    URL url = new File(jar).toURI().toURL();
                    method.invoke(classLoader, url);
                } catch (MalformedURLException | IllegalAccessException | InvocationTargetException e) {
                    log.error(String.format("Failed to add jar %s to classpath.", jar));
                }
            }
        } catch (NoSuchMethodException e) {
            log.error("NoSuchMethodException: " + e.getMessage());
        }
    }

    public static <T> T safeCall(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.warn("Exception: " + e.getMessage());
        }
        return null;
    }
}
