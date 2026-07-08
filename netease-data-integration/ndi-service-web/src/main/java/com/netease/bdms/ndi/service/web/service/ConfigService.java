package com.netease.bdms.ndi.service.web.service;

import com.alibaba.fastjson.JSONObject;

/**
 * @ClassName ConfigService
 * @Description 配置服务
 * @Author Min Zhao
 * @Version 1.0
 **/
public interface ConfigService {

  /**
   * 前端获取全局配置
   *
   * @return
   */
  JSONObject getFrontedGlobalConfig();

  void addFrontedConfig(JSONObject param);

  /**
   * 获取Azkaban集群url信息
   * @return
   */
  JSONObject getAzkabanUrlConfig();

}
