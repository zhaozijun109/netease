package com.netease.yuanqi.common.pojo.config;

import com.netease.yuanqi.common.utils.Preconditions;
import java.util.Arrays;
import org.apache.http.HttpHost;

public class EsConfig {
    private final HttpHost[] httpHosts;
    private final String username;
    private final String password;

    private EsConfig(HttpHost[] httpHosts, String username, String password) {
        this.httpHosts = Preconditions.checkNotNull(httpHosts);
        this.username = username;
        this.password = password;
    }

    public HttpHost[] getHttpHosts() {
        return httpHosts;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public static EsConfigBuilder builder() {
        return new EsConfigBuilder();
    }

    public static class EsConfigBuilder {
        private HttpHost[] httpHosts;
        private String username;
        private String password;

        public EsConfigBuilder() {}

        public EsConfigBuilder setHttpHosts(HttpHost[] httpHosts) {
            this.httpHosts = httpHosts;
            return this;
        }

        public EsConfigBuilder setUsername(String username) {
            this.username = username;
            return this;
        }

        public EsConfigBuilder setPassword(String password) {
            this.password = password;
            return this;
        }

        public EsConfig build() {
            return new EsConfig(httpHosts, username, password);
        }
    }

    @Override
    public String toString() {
        return "{"
                + "\"httpHosts\":"
                + Arrays.toString(httpHosts)
                + ",\"username\":\""
                + username
                + '\"'
                + ",\"password\":\""
                + password
                + '\"'
                + "}";
    }
}
