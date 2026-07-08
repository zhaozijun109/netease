package com.netease.lofter.tango.impl.config;

//import com.clickhouse.jdbc.ClickHouseDataSource;
import com.clickhouse.jdbc.ClickHouseDataSource;
import com.netease.lofter.tango.impl.Application;
import com.netease.yaolu.commons.datasource.dynamic.config.EnableDynamicDataSource;
import com.netease.yaolu.commons.spring.mybatis.CustomMapperFactoryBean;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration(proxyBeanMethods = false)
@EnableDynamicDataSource(defaultTargetName = DbConfig.DEFAULT_DATASOURCE)
@MapperScan(annotationClass = Mapper.class, basePackageClasses = Application.class, factoryBean = CustomMapperFactoryBean.class)
public class DbConfig {

    public static final String DEFAULT_DATASOURCE = "tangoDataSource";
    public static final String COMIC_STATIS_DATASOURCE = "comicStatisDataSource";
    public static final String CLICKHOUSE_DATASOURCE = "clickhouseDataSource";
    public static final String MAIN_DATASOURCE = "mainDataSource";

    public static final String YAOLU_ACTIVITY_DATASOURCE = "yaoluActivityDataSource";

    @Bean(DEFAULT_DATASOURCE)
    @ConfigurationProperties(prefix = "app.datasource.tango")
    public DataSource tangoDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setPoolName("tango");
        return dataSource;
    }

    @Bean(YAOLU_ACTIVITY_DATASOURCE)
    @ConfigurationProperties(prefix = "app.datasource.activity")
    public DataSource yaoluActivityDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setPoolName("yaolu-activity");
        return dataSource;
    }


    @Bean(MAIN_DATASOURCE)
    @ConfigurationProperties(prefix = "app.datasource.main")
    public DataSource mainDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setPoolName("public");
        return dataSource;
    }

    @Bean(COMIC_STATIS_DATASOURCE)
    @ConfigurationProperties(prefix = "app.datasource.comic-statis")
    public DataSource comicStatisDatasource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setPoolName("comic-statis");
        return dataSource;
    }

    @ConditionalOnProperty(value = "clickhouse.enable")
    @Bean(CLICKHOUSE_DATASOURCE)
    public DataSource clickhouseDataSource(Environment environment) {
        try {
            return new ClickHouseDataSource(environment.getProperty("app.datasource.clickhouse.jdbcUrl"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
