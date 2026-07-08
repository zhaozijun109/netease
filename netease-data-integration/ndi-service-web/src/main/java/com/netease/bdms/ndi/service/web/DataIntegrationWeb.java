package com.netease.bdms.ndi.service.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author zy
 */
@EnableAsync
@EnableScheduling
@SpringBootApplication
@MapperScan(basePackages = "com.netease.bdms.ndi.service.web.dao")
public class DataIntegrationWeb {
  public static void main(String[] args) {
    SpringApplication.run(DataIntegrationWeb.class, args);
  }
}
