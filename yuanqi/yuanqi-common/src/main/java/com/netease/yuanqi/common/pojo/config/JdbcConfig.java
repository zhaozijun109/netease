package com.netease.yuanqi.common.pojo.config;

import com.netease.yuanqi.common.utils.Preconditions;

public class JdbcConfig {
    private final String driverClassName;
    private final String url;
    private final String username;
    private final String password;

    private JdbcConfig(String driverClassName, String url, String username, String password) {
        this.driverClassName = Preconditions.checkNotNull(driverClassName);
        this.url = Preconditions.checkNotNull(url);
        this.username = Preconditions.checkNotNull(username);
        this.password = Preconditions.checkNotNull(password);
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public static JdbcConfigBuilder builder() {
        return new JdbcConfigBuilder();
    }

    public static class JdbcConfigBuilder {
        private String driverClassName;
        private String url;
        private String username;
        private String password;

        public JdbcConfigBuilder() {}

        public JdbcConfigBuilder setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
            return this;
        }

        public JdbcConfigBuilder setUrl(String url) {
            this.url = url;
            return this;
        }

        public JdbcConfigBuilder setUsername(String username) {
            this.username = username;
            return this;
        }

        public JdbcConfigBuilder setPassword(String password) {
            this.password = password;
            return this;
        }

        public JdbcConfig build() {
            return new JdbcConfig(driverClassName, url, username, password);
        }
    }

    @Override
    public String toString() {
        return "{"
                + "\"driverClassName\":\""
                + driverClassName
                + '\"'
                + ",\"url\":\""
                + url
                + '\"'
                + ",\"username\":\""
                + username
                + '\"'
                + ",\"password\":\""
                + password
                + '\"'
                + "}";
    }
}
