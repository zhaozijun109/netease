package com.netease.lofter.tango.integration.apollo.listener;

import com.alibaba.fastjson.JSONObject;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.netease.lofter.tango.api.consts.TangoConfigOpType;
import com.netease.lofter.tango.integration.apollo.ConfigManager;
import com.netease.lofter.tango.integration.apollo.TangoPropertySource;
import com.netease.lofter.tango.integration.apollo.meta.TangoChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.netease.lofter.tango.integration.consts.TangoIntegrationConsts.TANGO_NAMESPACE;
import static com.netease.lofter.tango.integration.consts.TangoIntegrationConsts.isTangoAdminConfigEvent;

@Slf4j
@Component
public class TangoConfigListener implements InitializingBean,
        ConfigChangeListener,
        ApplicationEventPublisherAware,
        EnvironmentAware {

    private static volatile String APP_ID = null;
    private TangoConfigEventPublisher tangoConfigEventPublisher;
    private ConfigurableEnvironment configurableEnvironment;

    @Override
    public void onChange(ConfigChangeEvent changeEvent) {
        List<TangoChangeEvent> tangoChangeEvents = new ArrayList<>();
        TangoPropertySource tangoPropertySource = ConfigManager.getOrCreatePropertySource(configurableEnvironment);
        for (String key : changeEvent.changedKeys()) {
            ConfigChange eventChange = changeEvent.getChange(key);
            String value = eventChange.getNewValue();
            TangoChangeEvent tangoChangeEvent;
            if (isTangoAdminConfigEvent(key)) {
                tangoChangeEvent = JSONObject.parseObject(value, TangoChangeEvent.class);
                if (!ConfigManager.getAppName(configurableEnvironment).equalsIgnoreCase(tangoChangeEvent.getAppId())) {
                    continue;
                }
            } else {
                tangoChangeEvent = TangoChangeEvent.from(eventChange);
            }
            if (isDeleteOp(tangoChangeEvent)) {
                tangoPropertySource.removeProperty(tangoChangeEvent.getConfigKey());
            } else {
                tangoPropertySource.putProperty(tangoChangeEvent.getConfigKey(), tangoChangeEvent.getConfigValue());
            }
            tangoChangeEvents.add(tangoChangeEvent);
        }
        if (tangoChangeEvents.isEmpty()) {
            return;
        }
        Map<String, ConfigChange> changes = tangoChangeEvents.stream()
                .map(TangoChangeEvent::adaptApolloConfig)
                .collect(Collectors.toMap(ConfigChange::getPropertyName, Function.identity(), (oldValue, newValue) -> newValue));
        tangoConfigEventPublisher.publishChangeEvent(new ConfigChangeEvent(TANGO_NAMESPACE, changes));
    }


    private boolean isDeleteOp(TangoChangeEvent tangoChangeEvent) {
        if (TangoConfigOpType.DELETE.name().equalsIgnoreCase(tangoChangeEvent.getEventType())) {
            return true;
        }
        return false;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.tangoConfigEventPublisher = new TangoConfigEventPublisher(applicationEventPublisher);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ConfigService.getConfig(TANGO_NAMESPACE).addChangeListener(this);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.configurableEnvironment = (ConfigurableEnvironment) environment;
    }

}
