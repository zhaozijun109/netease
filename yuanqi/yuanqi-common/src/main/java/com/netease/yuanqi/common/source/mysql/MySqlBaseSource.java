package com.netease.yuanqi.common.source.mysql;

import com.netease.yuanqi.common.pojo.config.MySqlConfig;
import com.netease.yuanqi.common.serialization.binlog.debezium.LocalTimestampJsonCdcSchema;
import java.io.Serializable;
import java.util.List;
import org.apache.flink.cdc.connectors.mysql.source.MySqlSource;
import org.apache.flink.cdc.connectors.mysql.table.StartupOptions;
import org.apache.flink.cdc.debezium.DebeziumDeserializationSchema;

public class MySqlBaseSource implements Serializable {
    private static final long serialVersionUID = 1L;
    private final MySqlConfig mysqlConfig;

    public MySqlBaseSource(MySqlConfig mysqlConfig) {
        this.mysqlConfig = mysqlConfig;
    }

    public MySqlSource<String> createMysqlSource(String serverId, List<String> tableList) {
        return MySqlSource.<String>builder()
                .hostname(mysqlConfig.getHostname())
                .port(mysqlConfig.getPort())
                .databaseList(mysqlConfig.getDatabase())
                .username(mysqlConfig.getUsername())
                .password(mysqlConfig.getPassword())
                .scanNewlyAddedTableEnabled(true)
                .serverId(serverId)
                .deserializer(new LocalTimestampJsonCdcSchema())
                .serverTimeZone(mysqlConfig.getServerTimezone())
                .debeziumProperties(mysqlConfig.getProperties())
                .tableList(
                        tableList.stream()
                                .map(table -> mysqlConfig.getDatabase() + "." + table)
                                .toArray(String[]::new))
                .startupOptions(StartupOptions.latest())
                .fetchSize(mysqlConfig.getFetchSize())
                .build();
    }

    public MySqlSource<String> createMysqlSource(
            String serverId, List<String> tableList, boolean includeSchema) {
        return MySqlSource.<String>builder()
                .hostname(mysqlConfig.getHostname())
                .port(mysqlConfig.getPort())
                .databaseList(mysqlConfig.getDatabase())
                .username(mysqlConfig.getUsername())
                .password(mysqlConfig.getPassword())
                .scanNewlyAddedTableEnabled(true)
                .serverId(serverId)
                .deserializer(new LocalTimestampJsonCdcSchema(includeSchema))
                .serverTimeZone(mysqlConfig.getServerTimezone())
                .debeziumProperties(mysqlConfig.getProperties())
                .tableList(
                        tableList.stream()
                                .map(table -> mysqlConfig.getDatabase() + "." + table)
                                .toArray(String[]::new))
                .startupOptions(StartupOptions.latest())
                .fetchSize(mysqlConfig.getFetchSize())
                .build();
    }

    public MySqlSource<String> createMysqlSource(
            String serverId, List<String> tableList, Long startTimestamp, boolean includeSchema) {
        return MySqlSource.<String>builder()
                .hostname(mysqlConfig.getHostname())
                .port(mysqlConfig.getPort())
                .databaseList(mysqlConfig.getDatabase())
                .username(mysqlConfig.getUsername())
                .password(mysqlConfig.getPassword())
                .scanNewlyAddedTableEnabled(true)
                .serverId(serverId)
                .deserializer(new LocalTimestampJsonCdcSchema(includeSchema))
                .serverTimeZone(mysqlConfig.getServerTimezone())
                .debeziumProperties(mysqlConfig.getProperties())
                .tableList(
                        tableList.stream()
                                .map(table -> mysqlConfig.getDatabase() + "." + table)
                                .toArray(String[]::new))
                .startupOptions(StartupOptions.timestamp(startTimestamp))
                .fetchSize(mysqlConfig.getFetchSize())
                .build();
    }

    public MySqlSource<String> createMysqlSource(
            String serverId,
            List<String> tableList,
            StartupOptions startupOptions,
            boolean includeSchema) {
        return MySqlSource.<String>builder()
                .hostname(mysqlConfig.getHostname())
                .port(mysqlConfig.getPort())
                .databaseList(mysqlConfig.getDatabase())
                .username(mysqlConfig.getUsername())
                .password(mysqlConfig.getPassword())
                .scanNewlyAddedTableEnabled(true)
                .serverId(serverId)
                .deserializer(new LocalTimestampJsonCdcSchema(includeSchema))
                .serverTimeZone(mysqlConfig.getServerTimezone())
                .debeziumProperties(mysqlConfig.getProperties())
                .tableList(
                        tableList.stream()
                                .map(table -> mysqlConfig.getDatabase() + "." + table)
                                .toArray(String[]::new))
                .startupOptions(startupOptions)
                .fetchSize(mysqlConfig.getFetchSize())
                .build();
    }

    public MySqlSource<String> createMysqlSource(
            String serverId,
            List<String> tableList,
            StartupOptions startupOptions,
            DebeziumDeserializationSchema<String> schema) {
        return MySqlSource.<String>builder()
                .hostname(mysqlConfig.getHostname())
                .port(mysqlConfig.getPort())
                .databaseList(mysqlConfig.getDatabase())
                .username(mysqlConfig.getUsername())
                .password(mysqlConfig.getPassword())
                .scanNewlyAddedTableEnabled(true)
                .serverId(serverId)
                .deserializer(schema)
                .serverTimeZone(mysqlConfig.getServerTimezone())
                .debeziumProperties(mysqlConfig.getProperties())
                .tableList(
                        tableList.stream()
                                .map(table -> mysqlConfig.getDatabase() + "." + table)
                                .toArray(String[]::new))
                .startupOptions(startupOptions)
                .fetchSize(mysqlConfig.getFetchSize())
                .build();
    }
}
