package com.netease.yuanqi.common.source.mysql;

import com.netease.yuanqi.common.pojo.config.MySqlConfig;
import java.io.Serializable;
import java.util.Properties;

public class MySqlYcySource extends MySqlBaseSource implements Serializable {
    private static final long serialVersionUID = 1L;

    public MySqlYcySource(String hostName, String database, Properties mySqlProps) {
        super(
                MySqlConfig.builder()
                        .setHostname(hostName)
                        .setPort(3306)
                        .setDatabase(database)
                        .setUsername("a13data")
                        .setPassword("Aij5Ks#8qpF&cb")
                        .setFetchSize(1024)
                        .setProperties(mySqlProps)
                        .build());
    }
}
