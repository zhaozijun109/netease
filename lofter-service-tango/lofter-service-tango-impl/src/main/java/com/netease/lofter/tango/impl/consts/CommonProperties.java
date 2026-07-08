package com.netease.lofter.tango.impl.consts;

import com.netease.yaolu.commons.spring.apollo.ApolloConfigRefreshBean;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author baizhichao
 */
@Getter
@Setter
@Component
@ApolloConfigRefreshBean(prefix = CommonProperties.PREFIX)
@ConfigurationProperties(prefix = CommonProperties.PREFIX)
public class CommonProperties {

    public static final String PREFIX = "tango.common";


    private String localMockUser = "guleiyang@corp.netease.com";


    private ApolloOpenApi apolloOpenApi = new ApolloOpenApi();


    @Getter
    @Setter
    public static class ApolloOpenApi {
        private String portalUrl;
        private String token;
        private String tradeAppToken;
    }

}
