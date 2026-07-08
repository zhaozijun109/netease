package com.netease.lofter.tango.impl.config;

import com.netease.lofter.tango.impl.helper.ProfileEnv;
import com.netease.lofter.tango.impl.web.interceptor.AccessUserInterceptor;
import com.netease.lofter.tango.impl.web.interceptor.CorsInterceptor;
import com.netease.lofter.tango.impl.web.interceptor.IpInterceptor;
import com.netease.yaolu.commons.spring.web.bind.ClientIpMethodArgumentResolver;
import com.netease.yaolu.commons.spring.web.bind.RequestAttributeArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration(proxyBeanMethods = true)
public class WebConfig implements WebMvcConfigurer {

    public static final String[] ANONY_PATH = new String[]{"/health/**", "/tango/config/list/internal", "/tango/config/list/pub"
    };
    @Autowired
    private ProfileEnv profileEnv;

    @Bean
    public IpInterceptor ipInterceptor() {
        return new IpInterceptor();
    }

    @Bean
    public AccessUserInterceptor accessUserInterceptor() {
        return new AccessUserInterceptor();
    }

    @Bean
    public CorsInterceptor corsInterceptor() {
        return new CorsInterceptor();
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(corsInterceptor()).addPathPatterns("/**").order(-5);
        registry.addInterceptor(ipInterceptor()).addPathPatterns("/**").excludePathPatterns(ANONY_PATH).order(0);
        registry.addInterceptor(accessUserInterceptor()).addPathPatterns("/**").excludePathPatterns(ANONY_PATH).order(10);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("OPTIONS", "HEAD", "GET", "POST")
                .allowCredentials(true);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new ClientIpMethodArgumentResolver());
        argumentResolvers.add(new RequestAttributeArgumentResolver());
    }

}