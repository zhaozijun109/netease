package com.netease.easyml.local.mllib.tfserving.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author hejiecheng
 * @Date 2020-04-13
 */
public enum TensorflowGrpcConnectionPool {

    /**
     * 连接单例，池子大小为10可以调整
     */
    INSTANCE(50);

    private Integer poolSize;

    AtomicInteger indexVal;

    TensorflowGrpcConnectionPool(Integer poolSize) {
        this.poolSize = poolSize;
        indexVal = new AtomicInteger(0);
    }

    private Map<String, TensorflowServiceGrpcClient> connectionPool = new ConcurrentHashMap<>();

    public TensorflowServiceGrpcClient getConnection(String ip, Integer port, Integer index) {
        String key = ip + "#" + Integer.toString(port) + "-" + (index % this.poolSize);
        TensorflowServiceGrpcClient tensorflowServiceGrpcClient = connectionPool.get(key);

        //每次新建立连接的时候都要加锁，会有一定的损耗，不过新连接不会太多，可以接受。
        if (null == tensorflowServiceGrpcClient) {
            synchronized (TensorflowGrpcConnectionPool.class) {
                if (null == connectionPool.get(key)) {
                    tensorflowServiceGrpcClient = new TensorflowServiceGrpcClient(ip, port);
                    connectionPool.put(key, tensorflowServiceGrpcClient);
                }
            }
        }
        return connectionPool.get(key);
    }

    private int incrementAndGetIndex() {
        for (; ; ) {
            int index = indexVal.get();
            int next = (index + 1) % poolSize;
            if (indexVal.compareAndSet(index, next)) {
                return index;
            }
        }
    }
}
