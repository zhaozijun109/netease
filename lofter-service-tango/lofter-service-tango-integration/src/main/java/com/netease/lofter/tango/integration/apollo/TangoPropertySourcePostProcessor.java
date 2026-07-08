package com.netease.lofter.tango.integration.apollo;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

public class TangoPropertySourcePostProcessor implements BeanFactoryPostProcessor, EnvironmentAware, PriorityOrdered, ApplicationListener<ContextRefreshedEvent> {

    private ConfigurableEnvironment environment;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        ConfigManager.initizalize(environment);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ConfigManager.replaceListener();
    }
}
