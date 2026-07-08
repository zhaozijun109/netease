package com.netease.easyudf.udf.util;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.R;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;

@Slf4j
public class MilvusUtil {

    private static String ENV = "";

    private static MilvusServiceClient _CLIENT;

    private static MilvusServiceClient[] _CLIENTS;

    public static MilvusServiceClient buildClient(String host, int port, String userName, String password) {
        ConnectParam connectParam =
                ConnectParam.newBuilder()
                        .withHost(host)
                        .withPort(port)
                        .withAuthorization(userName, password)
                        .keepAliveWithoutCalls(true)
                        .build();
        return new MilvusServiceClient(connectParam);
    }

    public static void setEnv(String env) {
        ENV = env;
    }

    public static String getENV() {
        return ENV;
    }

    private static Properties load() throws IOException {
        String config = "/milvus.properties";
        if (ENV.equals("dev")) {
            config = "/milvus.dev.properties";
        }
        Properties prop = new Properties();
        prop.load(MilvusUtil.class.getResourceAsStream(config));
        return prop;
    }

    public synchronized static MilvusServiceClient getOrCreateClient() throws IOException {
        if (Objects.isNull(_CLIENT)) {
            Properties prop = load();
            _CLIENT = buildClient(prop.getProperty("host"), Integer.parseInt(prop.getProperty("port")),
                    prop.getProperty("user"), prop.getProperty("password"));
        }
        return _CLIENT;
    }

    public synchronized static MilvusServiceClient getOrCreateClient(int num) throws IOException {
        if (Objects.isNull(_CLIENTS)) {
            _CLIENTS = new MilvusServiceClient[num];
            Properties prop = load();
            for (int i = 0; i < num; i++) {
                _CLIENTS[i] = buildClient(prop.getProperty("host"), Integer.parseInt(prop.getProperty("port")),
                        prop.getProperty("user"), prop.getProperty("password"));
            }
        }
        Random rng = new Random();
        assert num == _CLIENTS.length;
        return _CLIENTS[rng.nextInt(num)];
    }

    public static <T> void checkStatus(R<T> r) throws Exception {
        checkStatus(r, false);
    }

    public static <T> void checkStatus(R<T> r, boolean ignoreError) throws Exception {
        if (r.getStatus() != R.Status.Success.getCode()) {
            if (ignoreError) {
                log.error(r.getMessage());
            } else {
                throw r.getException();
            }
        }
    }
}
