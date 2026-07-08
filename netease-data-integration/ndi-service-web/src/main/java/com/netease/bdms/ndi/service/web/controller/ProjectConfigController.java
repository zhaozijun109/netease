package com.netease.bdms.ndi.service.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.service.ProjectConfigService;
import com.netease.bdms.ndi.service.web.util.ResponseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName ProjectConfigController
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Controller
@RequestMapping("/project_config")
public class ProjectConfigController {
    private static final Logger log = LoggerFactory.getLogger(ProjectConfigController.class);
    @Autowired
    private ProjectConfigService configService;

    @PostMapping(value = "/set")
    @ResponseBody
    public ResponseResult setConfig(@RequestBody JSONObject param, HttpServletRequest request){
        String apiKey = request.getHeader("apiKey");
        String key = param.getString("key");
        String value = param.getString("value");
        String namespace = param.getString("namespace");
        configService.setConfig(key, value, namespace);
        return ResponseResult.createBySuccess();
    }

    @PostMapping(value = "/update")
    @ResponseBody
    public ResponseResult update(@RequestBody JSONObject param, HttpServletRequest request){
        String key = param.getString("key");
        String value = param.getString("value");
        String namespace = param.getString("namespace");
        configService.updateConfig(key, value, namespace);
        return ResponseResult.createBySuccess();
    }
}
