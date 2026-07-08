package com.netease.bdms.ndi.service.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.service.ConfigService;
import com.netease.bdms.ndi.service.web.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName ConfigController
 * @Description 配置服务控制器
 * @Author Min Zhao
 * @Version 1.0
 **/
@RequestMapping(value = "/api/v1/config")
@RestController
public class ConfigController {
  @Autowired
  private ConfigService configService;

  @GetMapping(value = "/fronted/global")
  public ResponseResult getFrontedGlobalConfig() {
    return ResponseResult.createBySuccess(configService.getFrontedGlobalConfig());
  }

  @PostMapping(value = "/add")
  @ResponseBody
  public ResponseResult add(@RequestBody JSONObject param) {
    configService.addFrontedConfig(param);
    return ResponseResult.createBySuccess();
  }

}
