package com.netease.easyml.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by linjiuning on 2020/6/22.
 */
public class ThreadUtil {
    private static final Logger log = LoggerFactory.getLogger(ThreadUtil.class);

    public static <V> List<V> invokeAll(ExecutorService executor, Callable<V>... callables) {
        return invokeAll(executor, Arrays.asList(callables));
    }

    public static <V> List<V> invokeAll(ExecutorService executor, List<Callable<V>> callables) {
        List<V> result = new ArrayList<>();
        try {
            List<Future<V>> futures = executor.invokeAll(callables);
            for (Future<V> future : futures) {
                V r = null;
                try {
                    r = future.get();
                } catch (ExecutionException e) {
                    log.error("ExecutionException: " + e.getMessage());
                }
                result.add(r);
            }
        } catch (InterruptedException e) {
            log.error("InterruptedException: " + e.getMessage());
        }
        return result;
    }

    public static void submitAll(ExecutorService executor, Runnable... runnables) {
        submitAll(executor, Arrays.asList(runnables));
    }

    public static void submitAll(ExecutorService executor, List<Runnable> runnables) {
        try {
            List<Future> futures = new ArrayList<>();
            for (Runnable runnable : runnables) {
                Future<?> future = executor.submit(runnable);
                futures.add(future);
            }
            for (Future future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    log.error("ExecutionException: " + e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            log.error("InterruptedException: " + e.getMessage());
        }
    }
}
