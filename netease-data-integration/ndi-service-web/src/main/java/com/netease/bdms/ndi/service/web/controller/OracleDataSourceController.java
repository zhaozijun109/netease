package com.netease.bdms.ndi.service.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.netease.bdms.ndi.service.web.config.ContextConstant;
import com.netease.bdms.ndi.service.web.config.NdiContext;
import com.netease.bdms.ndi.service.web.dto.datasource.DataSourcesResDto;
import com.netease.bdms.ndi.service.web.service.OracleDataSourceService;
import com.netease.bdms.ndi.service.web.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName OracleController
 * @Description Oracle数据源控制器
 * @Author Min Zhao
 * @Version 1.0
 **/
@RestController
@RequestMapping(value = "/api/v1/datasource/oracle")
public class OracleDataSourceController {

  @Autowired
  private OracleDataSourceService oracleDataSourceService;

  /**
   * 登记数据源
   *
   * @param createDataSourceParam
   * @return 数据源id
   */
  @PostMapping(value = "/create")
  public ResponseResult create(@RequestBody JSONObject createDataSourceParam) {
    Long dataSourceId = oracleDataSourceService.create(createDataSourceParam);
    return ResponseResult.createBySuccess(dataSourceId);
  }

  @PostMapping(value = "/modify")
  public ResponseResult modify(@RequestBody JSONObject modifyDataSourceParam) {
    JSONObject response = oracleDataSourceService.modify(modifyDataSourceParam);
    return ResponseResult.createBySuccess(response);
  }

  /**
   * 获取Oracle数据源列表
   *
   * @param pageNum
   * @param pageSize
   * @return
   */
  @GetMapping(value = "/listIdAndNameAndCatalogName")
  public ResponseResult<DataSourcesResDto> listIdAndNameAndCatalogName(@RequestParam(value = "searchKey", required = false) String searchKey,
                                                    @RequestParam(value = "pageNum") Integer pageNum,
                                                    @RequestParam(value = "pageSize") Integer pageSize) {
    Integer productId = NdiContext.get(ContextConstant.PRODUCT_ID);
    Preconditions.checkArgument(productId != null, "获取产品账号失败，请重新登录");
    DataSourcesResDto dataSourcesResDto = oracleDataSourceService.getDataSourcesResDto(productId, searchKey, pageNum, pageSize);
    return ResponseResult.createBySuccess(dataSourcesResDto);
  }
}
