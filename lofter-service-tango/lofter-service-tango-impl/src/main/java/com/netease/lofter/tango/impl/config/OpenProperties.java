package com.netease.lofter.tango.impl.config;

import com.netease.yaolu.commons.spring.apollo.ApolloConfigRefreshBean;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
@ApolloConfigRefreshBean(prefix = OpenProperties.PREFIX)
@ConfigurationProperties(prefix = OpenProperties.PREFIX)
@Getter
@Setter
public class OpenProperties {

    public static final String PREFIX = "kol.open";

    /**
     * 开放平台
     */
    @NestedConfigurationProperty
    private Config oceanEngine = new Config();


    @Getter
    @Setter
    public static class Config {
        private String appId;
        private String appSecret;
        private String oauthCallbackUrl;

    }


}
