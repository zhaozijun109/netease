package com.netease.bdms.ndi.service.web.config;

import com.netease.bdms.ndi.service.web.controller.interceptor.LogInterceptor;
import com.netease.bdms.ndi.service.web.controller.interceptor.SessionInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/**
 * @ClassName WebConfiguration
 * @Description 服务配置类
 * @Author Min Zhao
 * @Version 1.0
 **/
@Configuration
@EnableWebMvc
public class WebConfiguration implements WebMvcConfigurer {
  @Bean
  public SessionInterceptor sessionInterceptor(){
    return new SessionInterceptor();
  }

  @Bean
  public LogInterceptor logInterceptor() {
    return new LogInterceptor();
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(sessionInterceptor())
        .addPathPatterns("/**")
        .excludePathPatterns("/api/v1/check")
        .order(0);

    registry.addInterceptor(logInterceptor())
        .addPathPatterns("/**/*")
        .excludePathPatterns("/api/v1/check")
        .order(1);
  }
}
