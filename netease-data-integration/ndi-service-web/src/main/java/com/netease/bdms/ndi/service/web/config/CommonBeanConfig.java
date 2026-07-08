package com.netease.bdms.ndi.service.web.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.netease.bdms.ndi.service.web.controller.exception.RestThrowErrorHandler;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName BeanConfig
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Component
public class CommonBeanConfig {

  private static final Logger log = LoggerFactory.getLogger(CommonBeanConfig.class);

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    RestTemplate restTemplate = builder.build();
    restTemplate.setRequestFactory(new HttpComponentsClientRestfulHttpRequestFactory());
    restTemplate.setErrorHandler(new RestThrowErrorHandler());
    return restTemplate;
  }

  private static final class HttpComponentsClientRestfulHttpRequestFactory extends HttpComponentsClientHttpRequestFactory {
    @Override
    protected HttpUriRequest createHttpUriRequest(HttpMethod httpMethod, URI uri) {
      if (httpMethod == HttpMethod.GET) {
        return new HttpGetRequestWithEntity(uri);
      }
      return super.createHttpUriRequest(httpMethod, uri);
    }
  }

  private static final class HttpGetRequestWithEntity extends HttpEntityEnclosingRequestBase {
    public HttpGetRequestWithEntity(final URI uri) {
      super.setURI(uri);
    }

    @Override
    public String getMethod() {
      return HttpMethod.GET.name();
    }
  }

  @Bean
  @Primary
  @ConditionalOnMissingBean(ObjectMapper.class)
  public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
    ObjectMapper objectMapper = builder.createXmlMapper(false).build();
    objectMapper.getSerializerProvider().setNullValueSerializer(new JsonSerializer<Object>() {
      @Override
      public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString("");
      }
    });
    return objectMapper;
  }

  private CorsConfiguration buildConfig() {
    CorsConfiguration corsConfiguration = new CorsConfiguration();
    corsConfiguration.addAllowedOrigin("*");
    corsConfiguration.addAllowedHeader("*");
    corsConfiguration.addAllowedMethod("*");
    corsConfiguration.setAllowCredentials(true);
    return corsConfiguration;
  }

  @Bean
  public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", buildConfig());
    return new CorsFilter(source);
  }

  /**
   * 定时任务执行线程池
   *
   * @return
   */
  @Bean
  public TaskScheduler taskScheduler(){
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setThreadNamePrefix("customTaskScheduler-");
    taskScheduler.setPoolSize(2);
    return taskScheduler;
  }

  /**
   * 异步任务执行线程池
   *
   */
  @Primary
  @Bean
  public TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(10);
    taskExecutor.setMaxPoolSize(20);
    taskExecutor.setQueueCapacity(1000);
    taskExecutor.setKeepAliveSeconds(60);
    taskExecutor.setThreadNamePrefix("customTaskExecutor-");
    taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
    return taskExecutor;
  }

}
