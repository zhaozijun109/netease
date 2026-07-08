package com.netease.lofter.tango.integration.apollo;

import com.ctrip.framework.apollo.spring.boot.ApolloApplicationContextInitializer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class TangoConfigApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigManager.initizalize(applicationContext.getEnvironment());
    }


    @Override
    public int getOrder() {
        return ApolloApplicationContextInitializer.DEFAULT_ORDER + 1;
    }

}
