package com.netease.bdms.ndi.service.web.task;

import java.util.Random;

import com.netease.bdms.ndi.service.web.service.impl.MetahubService;
import com.netease.bdms.ndi.service.web.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @ClassName ScheduledTasks
 * @Description 调度任务
 * @Author Min Zhao
 * @Version 1.0
 **/
@Component
public class ScheduledTasks {

  private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

  @Autowired
  private RedisUtil redisUtil;

  @Autowired
  private MetahubService metahubService;

  /**
   * 每隔一分钟检测Redis服务的可用性
   */
  @Scheduled(fixedDelay = 1000 * 60 )
  public void checkRedis() {
    Random random = new Random();
    try {
      Thread.sleep(random.nextInt(1000));
    } catch (InterruptedException ignore) {
    }
    try {
      String testValue = redisUtil.get("test");
    } catch (Exception e) {
      // 通过哨兵监听error日志，及时报警
      log.error("Redis服务异常", e);
    }
  }

  /**
   * 每隔一分钟检测元数据中心的可用性
   */
//  @Scheduled(fixedDelay = 1000 * 60, initialDelay = 1000 * 5)
  public void checkMetahub() {

//    log.info("Starting checking metaHub.");
    // 获取分布式锁，并设置超时时间

    // 发送请求

    // 释放分布式锁
  }

}
