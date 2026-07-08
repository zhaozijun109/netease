package com.netease.lofter.tango.integration.consts;

public interface TangoIntegrationConsts {

    String TANGO_NAMESPACE = "lofter.tango";
    String TANGO_APPID = "lofter-service-tango";
    String TANGO_ENDPOINT = "tango.config.endpoint";
    String TANGO_ENDPOINT_TOKEN_KEY = "tango.config.access.token";

    String APP_ID_KEY = "app.id";
    String ENV_KEY = "apollo.cluster";
    String APP_NAME_KEY = "spring.application.name";

    String DUBBO_REGISTRY_KEY = "dubbo.registry.address";
    String DUBBO_GROUP_LOFTER_KEY = "dubbo.group.lofter";

    String TANGO_PROPERTY_SOURCES = "TangoPropertySources";


    String TANGO_CONFIG_CHANEGE_EVENT_KEY_PATTERN = "tango.config.change.event";

    static boolean isTangoAdminConfigEvent(String key) {
        return key != null && key.startsWith(TANGO_CONFIG_CHANEGE_EVENT_KEY_PATTERN);
    }
}
