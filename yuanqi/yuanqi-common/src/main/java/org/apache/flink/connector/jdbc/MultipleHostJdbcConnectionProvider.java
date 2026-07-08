package org.apache.flink.connector.jdbc;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.connector.jdbc.datasource.connections.JdbcConnectionProvider;
import org.apache.flink.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NotThreadSafe
@PublicEvolving
public class MultipleHostJdbcConnectionProvider implements JdbcConnectionProvider, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG =
            LoggerFactory.getLogger(MultipleHostJdbcConnectionProvider.class);
    private static final String JDBC_URL_PATTERN = "jdbc:(\\w+)://([^/]+)(.*)";

    private final JdbcConnectionOptions jdbcOptions;
    private transient Driver loadedDriver;
    private transient Connection connection;

    public MultipleHostJdbcConnectionProvider(JdbcConnectionOptions jdbcOptions) {
        this.jdbcOptions = jdbcOptions;
    }

    @Nullable
    @Override
    public Connection getConnection() {
        return this.connection;
    }

    @Override
    public boolean isConnectionValid() throws SQLException {
        return this.connection != null
                && this.connection.isValid(this.jdbcOptions.getConnectionCheckTimeoutSeconds());
    }

    private Driver loadDriver(String driverName) throws SQLException, ClassNotFoundException {
        Preconditions.checkNotNull(driverName);
        Enumeration<Driver> drivers = DriverManager.getDrivers();

        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getName().equals(driverName)) {
                return driver;
            }
        }

        Class<?> clazz =
                Class.forName(driverName, true, Thread.currentThread().getContextClassLoader());

        try {
            return (Driver) clazz.newInstance();
        } catch (Exception ex) {
            throw new SQLException("Fail to create driver of class " + driverName, ex);
        }
    }

    private Driver getLoadedDriver() throws SQLException, ClassNotFoundException {
        if (this.loadedDriver == null) {
            this.loadedDriver = this.loadDriver(this.jdbcOptions.getDriverName());
        }

        return this.loadedDriver;
    }

    @Override
    public Connection getOrEstablishConnection() throws SQLException, ClassNotFoundException {
        if (this.isConnectionValid()) {
            return this.connection;
        } else {
            if (this.jdbcOptions.getDriverName() == null) {
                this.connection =
                        DriverManager.getConnection(
                                this.jdbcOptions.getDbURL(),
                                this.jdbcOptions.getUsername().orElse(null),
                                this.jdbcOptions.getPassword().orElse(null));
            } else {
                Driver driver = this.getLoadedDriver();
                Properties info = new Properties();
                this.jdbcOptions.getUsername().ifPresent((user) -> info.setProperty("user", user));
                this.jdbcOptions
                        .getPassword()
                        .ifPresent((password) -> info.setProperty("password", password));
                Pattern pattern = Pattern.compile(JDBC_URL_PATTERN);
                Matcher matcher = pattern.matcher(this.jdbcOptions.getDbURL());
                if (matcher.matches()) {
                    String protocol = matcher.group(1);
                    String host = matcher.group(2);
                    String other = matcher.group(3);

                    List<String> hostList =
                            Arrays.stream(host.split(","))
                                    .map(String::trim)
                                    .filter(s -> !s.isEmpty())
                                    .sorted()
                                    .collect(Collectors.toList());
                    if (hostList.size() > 1) {
                        Random random = new Random(System.currentTimeMillis());
                        int hostPosition = random.nextInt(hostList.size());
                        this.connection =
                                driver.connect(
                                        String.format(
                                                "jdbc:%s://%s%s",
                                                protocol, hostList.get(hostPosition), other),
                                        info);
                    } else {
                        this.connection = driver.connect(this.jdbcOptions.getDbURL(), info);
                    }
                } else {
                    throw new SQLException("Invalid JDBC URL: " + this.jdbcOptions.getDbURL());
                }

                if (this.connection == null) {
                    throw new SQLException(
                            "No suitable driver found for " + this.jdbcOptions.getDbURL(), "08001");
                }
            }

            return this.connection;
        }
    }

    @Override
    public void closeConnection() {
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (SQLException e) {
                LOG.warn("JDBC connection close failed.", e);
            } finally {
                this.connection = null;
            }
        }
    }

    @Override
    public Connection reestablishConnection() throws SQLException, ClassNotFoundException {
        this.closeConnection();
        return this.getOrEstablishConnection();
    }

    static {
        DriverManager.getDrivers();
    }

    @Override
    public void close() throws Exception {
        this.closeConnection();
    }
}
