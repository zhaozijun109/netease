package com.netease.bdms.ndi.service.web.controller;

import com.netease.bdms.ndi.service.web.util.ResponseResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @ClassName CheckController
 * @Description 提供给nginx的检测接口
 * @Author Min Zhao
 * @Version 1.0
 **/
@RequestMapping(value = "/api")
@Controller
public class CheckController {
  @GetMapping(value = "/v1/check")
  @ResponseBody
  public ResponseResult check() {
    return ResponseResult.createBySuccess();
  }
}
