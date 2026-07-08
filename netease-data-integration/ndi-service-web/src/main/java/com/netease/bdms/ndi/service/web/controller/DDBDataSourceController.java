package com.netease.bdms.ndi.service.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.netease.bdms.ndi.service.web.config.ContextConstant;
import com.netease.bdms.ndi.service.web.config.NdiContext;
import com.netease.bdms.ndi.service.web.dto.datasource.DataSourcesResDto;
import com.netease.bdms.ndi.service.web.service.DDBDataSourceService;
import com.netease.bdms.ndi.service.web.util.ParamUtil;
import com.netease.bdms.ndi.service.web.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName DDBDataSourceController
 * @Description DDB数据源控制器
 * @Author Min Zhao
 * @Version 1.0
 **/
@RestController
@RequestMapping(value = "/api/v1/datasource/ddb")
public class DDBDataSourceController {
  @Autowired
  private DDBDataSourceService ddbDataSourceService;

  @GetMapping(value = "/dbi/listIdAndNameAndCatalogName")
  public ResponseResult listDBINameAndId(@RequestParam(value = "searchKey", required = false) String searchKey,
                                         @RequestParam(value = "pageNum") Integer pageNum,
                                         @RequestParam(value = "pageSize") Integer pageSize) {
    Integer productId = NdiContext.get(ContextConstant.PRODUCT_ID);
    Preconditions.checkArgument(productId != null, "获取产品账号失败，请重新登录");
    DataSourcesResDto dataSourcesResDto = ddbDataSourceService.getDBIDataSourcesResDto(productId, searchKey, pageNum, pageSize);
    return ResponseResult.createBySuccess(dataSourcesResDto);
  }

  @PostMapping(value = "/dbi/create")
  public ResponseResult<Long> createDBI(@RequestBody JSONObject jsonObject) {
    ParamUtil.validate(jsonObject);
    Long dataSourceId = ddbDataSourceService.create(jsonObject);
    return ResponseResult.createBySuccess(dataSourceId);
  }

  @PostMapping(value = "/dbi/modify")
  public ResponseResult modifyDBI(@RequestBody JSONObject modifyDataSourceParam) {
    ParamUtil.validate(modifyDataSourceParam);
    JSONObject response = ddbDataSourceService.modify(modifyDataSourceParam);
    return ResponseResult.createBySuccess(response);
  }

  @GetMapping(value = "/qs/listIdAndNameAndCatalogName")
  public ResponseResult listQSNameAndId(@RequestParam(value = "searchKey", required = false) String searchKey,
                                        @RequestParam(value = "pageNum") Integer pageNum,
                                        @RequestParam(value = "pageSize") Integer pageSize) {
    Integer productId = NdiContext.get(ContextConstant.PRODUCT_ID);
    Preconditions.checkArgument(productId != null, "获取产品账号失败，请重新登录");
    DataSourcesResDto dataSourcesResDto = ddbDataSourceService.getQSDataSourcesResDto(productId, searchKey, pageNum, pageSize);
    return ResponseResult.createBySuccess(dataSourcesResDto);
  }

  @PostMapping(value = "/qs/create")
  public ResponseResult<Long> createQS(@RequestBody JSONObject jsonObject) {
    Long dataSourceId = ddbDataSourceService.create(jsonObject);
    return ResponseResult.createBySuccess(dataSourceId);
  }

  @PostMapping(value = "/qs/modify")
  public ResponseResult modifyQS(@RequestBody JSONObject modifyDataSourceParam) {
    ParamUtil.validate(modifyDataSourceParam);
    JSONObject response = ddbDataSourceService.modify(modifyDataSourceParam);
    return ResponseResult.createBySuccess(response);
  }
}
