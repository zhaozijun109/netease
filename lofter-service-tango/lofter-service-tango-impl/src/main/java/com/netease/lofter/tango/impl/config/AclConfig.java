package com.netease.lofter.tango.impl.config;

import com.google.common.collect.Lists;
import com.netease.lofter.acl.sdk.config.ACLDubboRemoteServiceRegistrar;
import com.netease.lofter.acl.sdk.config.ACLMvcConfiguration;
import com.netease.lofter.acl.sdk.config.ACLProviderConfiguration;
import com.netease.lofter.acl.sdk.filter.ACLFilter;
import com.netease.lofter.acl.sdk.meta.ACLFeature;
import com.netease.lofter.tango.impl.web.filter.MockFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.servlet.DispatcherType;


@Configuration
@Import({ACLProviderConfiguration.class, ACLDubboRemoteServiceRegistrar.class, ACLMvcConfiguration.class})
public class AclConfig {

    private static final String PREFIX_PATTERN = "/tango/*";

    @Bean
    public ACLFilter aclFilter() {
        ACLFilter aclFilter = new ACLFilter();
        aclFilter.setFeatures(ACLFeature.ALL);
        aclFilter.setExcludePathPatterns(WebConfig.ANONY_PATH);
        return aclFilter;
    }

    @Bean
    public FilterRegistrationBean<ACLFilter> aclFilterRegistrationBean() {
        FilterRegistrationBean<ACLFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(aclFilter());
        bean.setUrlPatterns(Lists.newArrayList(PREFIX_PATTERN));
        bean.setDispatcherTypes(DispatcherType.REQUEST);
        bean.setOrder(OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER + 1);
        return bean;
    }

    @Bean
    public MockFilter mockFilter() {
        return new MockFilter();
    }

    @Bean
    public FilterRegistrationBean<MockFilter> mockFilterRegistrationBean() {
        FilterRegistrationBean<MockFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(mockFilter());
        bean.setUrlPatterns(Lists.newArrayList(PREFIX_PATTERN));
        bean.setDispatcherTypes(DispatcherType.REQUEST);
        bean.setOrder(OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER - 1);
        bean.setName("mockFilter");
        return bean;
    }

}

