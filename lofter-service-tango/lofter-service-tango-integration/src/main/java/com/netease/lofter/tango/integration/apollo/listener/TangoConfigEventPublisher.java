package com.netease.lofter.tango.integration.apollo.listener;

import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.property.AutoUpdateConfigChangeListener;
import com.netease.lofter.tango.integration.apollo.ConfigManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Constructor;

public class TangoConfigEventPublisher {

    private static volatile Constructor<?> constructor = null;
    private final ApplicationEventPublisher applicationEventPublisher;

    public TangoConfigEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }


    public void publishChangeEvent(ConfigChangeEvent configChangeEvent) {
        if (ConfigManager.supportsApolloChangeEvent()) {
            Object event = null;
            try {
                if (constructor == null) {
                    constructor = ClassUtils.getConstructorIfAvailable(Class.forName("com.ctrip.framework.apollo.spring.events.ApolloConfigChangeEvent"), ConfigChangeEvent.class);
                }
                event = constructor.newInstance(configChangeEvent);
                applicationEventPublisher.publishEvent(event);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            ConfigManager.configChangeListenerInstance()
                    .stream()
                    .map(listener -> listener instanceof LofterApolloConfigFilterListener ? ((LofterApolloConfigFilterListener) listener).getDelagete() : listener)
                    .filter(listener -> listener instanceof AutoUpdateConfigChangeListener)
                    .forEach(listener -> listener.onChange(configChangeEvent));
        }
    }
}
