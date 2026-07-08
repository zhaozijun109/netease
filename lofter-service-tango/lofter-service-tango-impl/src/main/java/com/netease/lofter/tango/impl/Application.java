package com.netease.lofter.tango.impl;

import com.netease.lofter.tango.impl.util.BeanUtils;
import com.netease.mm.tk.common.util.BeanConvertUtils;
import com.netease.yaolu.commons.datasource.dynamic.DynamicDataSource;
import com.netease.yaolu.commons.healthecheck.patch.RedisHealthContributorAutoConfiguration;
import com.netease.yaolu.commons.spring.apollo.config.EnableApolloConfigRefreshBean;
import com.netease.yaolu.commons.spring.cache.config.EnableCacheInterceptor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@EnableCacheInterceptor
@EnableConfigurationProperties
@EnableApolloConfigRefreshBean
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        RedisHealthContributorAutoConfiguration.class})
public class Application implements InitializingBean {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        BeanConvertUtils.registerConverter((source, clazz) -> {
            Object target = null;
            try {
                target = clazz.newInstance();
                BeanUtils.copyNonNullProperties(source, target);
                return target;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Bean
    public ApplicationRunner applicationRunner(ObjectProvider<DataSource> dataSourceProvider) {
        return args -> {
            dataSourceProvider.stream().forEach(dataSource -> {
                try {
                    if (dataSource instanceof DynamicDataSource) {
                        return;
                    }
                    Connection connection = dataSource.getConnection();
                    connection.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        };
    }
}
