package com.netease.yuanqi.common.utils.redis;

import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import java.util.HashSet;
import java.util.Set;
import org.apache.flink.api.java.tuple.Tuple2;

public class RedisUtils {
    public static RedisClusterClient getClusterClient(String redisHostAndPort) {
        Set<RedisURI> nodes = new HashSet<>();
        for (String hostAndPort : redisHostAndPort.split(",")) {
            nodes.add(
                    RedisURI.Builder.redis(
                                    hostAndPort.split(":")[0],
                                    Integer.parseInt(hostAndPort.split(":")[1]))
                            .build());
        }
        return RedisClusterClient.create(nodes);
    }

    public static RedisClusterClient getClusterClient(
            String redisHostAndPort, Tuple2<String, String> authUserAndPassword) {
        Set<RedisURI> nodes = new HashSet<>();
        for (String hostAndPort : redisHostAndPort.split(",")) {
            nodes.add(
                    RedisURI.Builder.redis(
                                    hostAndPort.split(":")[0],
                                    Integer.parseInt(hostAndPort.split(":")[1]))
                            .withAuthentication(authUserAndPassword.f0, authUserAndPassword.f1)
                            .build());
        }
        return RedisClusterClient.create(nodes);
    }

    public static RedisClusterClient getClusterClient(String redisHostAndPort, String password) {
        Set<RedisURI> nodes = new HashSet<>();
        for (String hostAndPort : redisHostAndPort.split(",")) {
            nodes.add(
                    RedisURI.Builder.redis(
                                    hostAndPort.split(":")[0],
                                    Integer.parseInt(hostAndPort.split(":")[1]))
                            .withPassword(password.toCharArray())
                            .build());
        }
        return RedisClusterClient.create(nodes);
    }
}
