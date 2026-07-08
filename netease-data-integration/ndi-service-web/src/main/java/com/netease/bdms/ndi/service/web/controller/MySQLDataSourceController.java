package com.netease.bdms.ndi.service.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.netease.bdms.ndi.service.web.config.ContextConstant;
import com.netease.bdms.ndi.service.web.config.NdiContext;
import com.netease.bdms.ndi.service.web.dto.datasource.DataSourcesResDto;
import com.netease.bdms.ndi.service.web.service.MySQLDataSourceService;
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
 * @ClassName MySQLDataSourceController
 * @Description MySQL数据源登记
 * @Author Min Zhao
 * @Version 1.0
 **/
@RestController
@RequestMapping(value = "/api/v1/datasource/mysql")
public class MySQLDataSourceController {

  @Autowired
  private MySQLDataSourceService dataSourceService;

  @GetMapping(path = "/listIdAndNameAndCatalogName")
  public ResponseResult listIdAndNameAndCatalogName(@RequestParam(value = "searchKey", required = false) String searchBy,
                                                    @RequestParam(value = "pageNum") Integer pageNum,
                                                    @RequestParam(value = "pageSize") Integer pageSize) {
    Integer productId = NdiContext.get(ContextConstant.PRODUCT_ID);
    Preconditions.checkArgument(productId != null, "获取产品账号失败，请重新登录");
    DataSourcesResDto dataSourcesResDto = dataSourceService.getDataSourcesResDto(productId, searchBy, pageNum, pageSize);
    return ResponseResult.createBySuccess(dataSourcesResDto);
  }

  @PostMapping(value = "/create")
  public ResponseResult create(@RequestBody JSONObject jsonObject) {
    Long dataSourceId = dataSourceService.create(jsonObject);
    return ResponseResult.createBySuccess(dataSourceId);
  }

  @PostMapping(value = "/modify")
  public ResponseResult modify(@RequestBody JSONObject modifyDataSourceParam) {
    ParamUtil.validate(modifyDataSourceParam);
    JSONObject result = dataSourceService.modify(modifyDataSourceParam);
    return ResponseResult.createBySuccess(result);
  }
}
