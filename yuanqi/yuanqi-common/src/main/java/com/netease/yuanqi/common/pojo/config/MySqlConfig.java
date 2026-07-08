package com.netease.yuanqi.common.pojo.config;

import com.netease.yuanqi.common.utils.Preconditions;
import java.io.Serializable;
import java.util.Properties;

public class MySqlConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String hostname;
    private final Integer port;
    private final String database;
    private final String username;
    private final String password;
    private final String serverTimezone;
    private final String jdbcUrl;
    private final Properties properties;
    private final Integer fetchSize;

    public MySqlConfig(
            String hostname,
            Integer port,
            String database,
            String username,
            String password,
            String serverTimezone,
            String jdbcUrl,
            Properties properties,
            Integer fetchSize) {
        this.hostname = Preconditions.checkNotNull(hostname);
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.serverTimezone = serverTimezone == null ? "Asia/Shanghai" : serverTimezone;
        this.jdbcUrl = jdbcUrl;
        this.properties = properties;
        this.fetchSize = fetchSize == null ? 1024 : fetchSize;
    }

    public String getHostname() {
        return hostname;
    }

    public Integer getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getServerTimezone() {
        return serverTimezone;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public Properties getProperties() {
        return properties;
    }

    public Integer getFetchSize() {
        return fetchSize;
    }

    public static MysqlConfigBuilder builder() {
        return new MysqlConfigBuilder();
    }

    public static class MysqlConfigBuilder {
        private String hostname;
        private Integer port;
        private String database;
        private String username;
        private String password;
        private String serverTimezone;
        private String jdbcUrl;
        private Properties properties;
        private Integer fetchSize;

        public MysqlConfigBuilder() {}

        public MysqlConfigBuilder setHostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public MysqlConfigBuilder setPort(Integer port) {
            this.port = port;
            return this;
        }

        public MysqlConfigBuilder setDatabase(String database) {
            this.database = database;
            return this;
        }

        public MysqlConfigBuilder setUsername(String username) {
            this.username = username;
            return this;
        }

        public MysqlConfigBuilder setPassword(String password) {
            this.password = password;
            return this;
        }

        public MysqlConfigBuilder setServerTimezone(String serverTimezone) {
            this.serverTimezone = serverTimezone;
            return this;
        }

        public MysqlConfigBuilder setJdbcUrl(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
            return this;
        }

        public MysqlConfigBuilder setProperties(Properties properties) {
            this.properties = properties;
            return this;
        }

        public MysqlConfigBuilder setFetchSize(Integer fetchSize) {
            this.fetchSize = fetchSize;
            return this;
        }

        public MySqlConfig build() {
            return new MySqlConfig(
                    hostname,
                    port,
                    database,
                    username,
                    password,
                    serverTimezone,
                    jdbcUrl,
                    properties,
                    fetchSize);
        }
    }

    @Override
    public String toString() {
        return "MySqlConfig{"
                + "hostname='"
                + hostname
                + '\''
                + ", port="
                + port
                + ", database='"
                + database
                + '\''
                + ", username='"
                + username
                + '\''
                + ", password='"
                + password
                + '\''
                + ", serverTimezone='"
                + serverTimezone
                + '\''
                + ", jdbcUrl='"
                + jdbcUrl
                + '\''
                + ", properties='"
                + properties
                + '\''
                + ", fetchSize="
                + fetchSize
                + '}';
    }
}
