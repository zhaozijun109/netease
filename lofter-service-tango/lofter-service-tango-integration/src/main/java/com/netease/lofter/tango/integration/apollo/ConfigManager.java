package com.netease.lofter.tango.integration.apollo;

import com.alibaba.fastjson.JSONArray;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.netease.lofter.tango.api.dto.config.TangoConfigDTO;
import com.netease.lofter.tango.integration.apollo.listener.LofterApolloConfigFilterListener;
import com.netease.lofter.tango.integration.consts.TangoIntegrationConsts;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.netease.lofter.tango.integration.consts.TangoIntegrationConsts.*;

@Slf4j
public class ConfigManager {

    private static final boolean supportApolloChangeEvent = ClassUtils.isPresent("com.ctrip.framework.apollo.spring.events.ApolloConfigChangeEvent", ConfigManager.class.getClassLoader());
    private static final CopyOnWriteArrayList<ConfigChangeListener> configChangeListener = new CopyOnWriteArrayList<>();
    private static volatile String appName = null;
    private static AtomicBoolean initListener = new AtomicBoolean(false);
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(1, 30, TimeUnit.SECONDS))
            .retryOnConnectionFailure(false)
            .build();


    public static void initizalize(ConfigurableEnvironment environment) {
        String appId = getAppName(environment, false);
        //springboot cloud bootstrap context
        if (appId == null || appId.isEmpty()) {
            return;
        }
        MutablePropertySources propertySources = environment.getPropertySources();
        if (propertySources.contains(TANGO_PROPERTY_SOURCES)) {
            return;
        }
        PropertySource<?> apolloBootstrapPropertySource = propertySources.get(PropertySourcesConstants.APOLLO_PROPERTY_SOURCE_NAME);
        if (apolloBootstrapPropertySource != null) {
            log.info("initialize tango property source after apollo bootstrap property source...");
            PropertySource<?> tangoPropertySource;
            if (!TANGO_APPID.equalsIgnoreCase(appId)) {
                tangoPropertySource = ConfigManager.fetchRemotePropertySource(environment);
            } else {
                //todo 本工程直接读表，目前初始化未加载配置
                tangoPropertySource = getOrCreatePropertySource(environment);
            }
            propertySources.addAfter(PropertySourcesConstants.APOLLO_PROPERTY_SOURCE_NAME, tangoPropertySource);
        }
    }

    public static PropertySource<?> fetchRemotePropertySource(ConfigurableEnvironment environment) {
        for (int i = 0; i < 3; i++) {
            try {
                String token = environment.getRequiredProperty(TANGO_ENDPOINT_TOKEN_KEY);
                String env = environment.getRequiredProperty(ENV_KEY);
                String endpoint = environment.getRequiredProperty(TANGO_ENDPOINT);
                endpoint += "?appId=" + getAppName(environment) + "&token=" + token + "&env=" + env;
                try (Response response = HTTP_CLIENT.newCall(new Request.Builder().url(endpoint).build()).execute()) {
                    log.info("fetch tango config from {}, status:{}", endpoint, response.isSuccessful());
                    List<TangoConfigDTO> tangoConfigDTOS = JSONArray.parseArray(response.body().string(), TangoConfigDTO.class);
                    Map<String, String> map = tangoConfigDTOS.stream().collect(Collectors.toMap(TangoConfigDTO::getConfigKey, TangoConfigDTO::getConfigValue, (oldValue, newValue) -> newValue));
                    return new TangoPropertySource(map);
                }
            } catch (IOException e) {
                log.error("fetch tango config error", e);
            }
        }
        return new TangoPropertySource(new HashMap<>());
    }

    public static TangoPropertySource getOrCreatePropertySource(Environment environment) {
        ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;
        MutablePropertySources propertySources = configurableEnvironment.getPropertySources();
        TangoPropertySource tangoPropertySource = (TangoPropertySource) propertySources.get(TangoIntegrationConsts.TANGO_PROPERTY_SOURCES);
        if (null == tangoPropertySource) {
            tangoPropertySource = new TangoPropertySource(new HashMap<>());
        }
        return tangoPropertySource;
    }


    public static void replaceListener() {
        try {
            if (!initListener.compareAndSet(false, true)) {
                return;
            }
            Config config = ConfigService.getConfig(TANGO_NAMESPACE);
            Field mListeners = ReflectionUtils.findField(config.getClass(), "m_listeners");
            List<ConfigChangeListener> filterListeners = new ArrayList<>();
            if (null != mListeners) {
                mListeners.setAccessible(true);
                List<ConfigChangeListener> configChangeListeners = (List<ConfigChangeListener>) mListeners.get(config);
                for (ConfigChangeListener configChangeListener : configChangeListeners) {
                    ConfigChangeListener listener = configChangeListener;
                    if (!(listener instanceof LofterApolloConfigFilterListener)) {
                        listener = new LofterApolloConfigFilterListener(configChangeListener);
                    }
                    filterListeners.add(listener);
                }
                mListeners.set(config, filterListeners);
                configChangeListener.addAll(filterListeners);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<ConfigChangeListener> configChangeListenerInstance() {
        return configChangeListener;
    }

    public static boolean supportsApolloChangeEvent() {
        return supportApolloChangeEvent;
    }

    public static String getAppName(ConfigurableEnvironment environment) {
        return getAppName(environment, true);
    }

    public static String getAppName(ConfigurableEnvironment environment, boolean required) {
        if (isEmpty(appName)) {
            synchronized (ConfigManager.class) {
                if (isEmpty(appName)) {
                    appName = environment.getProperty(APP_ID_KEY, String.class);
                    if (isEmpty(appName)) {
                        if (required) {
                            appName = environment.getRequiredProperty(APP_NAME_KEY);
                        } else {
                            appName = environment.getProperty(APP_NAME_KEY);
                        }
                    }
                }
            }
        }
        return appName;
    }


    private static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

}
