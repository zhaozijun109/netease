package com.netease.lofter.tango.impl.config;

import com.netease.lofter.tango.impl.helper.ProfileEnv;
import com.xxl.job.core.executor.XxlJobExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @since 2020-04-29
 */
@Configuration
@Profile({"test", "online"})
public class XxlJobConfig {

    @Value("${xxl-job.admin.addresses}")
    private String address;

    @Value("${xxl.job.admin.accessToken}")
    private String accessToken;

    @Value("${spring.application.name}")
    private String appName;

    @Autowired
    private ProfileEnv profileEnv;

    @Bean(initMethod = "start", destroyMethod = "destroy")
    public XxlJobExecutor xxlJobExecutor() {
        XxlJobExecutor executor = new XxlJobExecutor();
        executor.setAdminAddresses(address);
        executor.setAccessToken(accessToken);
        String name = appName;
        if (profileEnv.isPre()) {
            name = name + "-pre";
        }
        executor.setAppName(name);
        executor.setTitle(name);
        executor.setLogPath("logs/xxl-job");
        executor.setLogRetentionDays(15);
        executor.setAuthor("广告投流开发组");
        executor.setAlarmEmail("guleiyang@corp.netease.com");
        return executor;
    }
}
