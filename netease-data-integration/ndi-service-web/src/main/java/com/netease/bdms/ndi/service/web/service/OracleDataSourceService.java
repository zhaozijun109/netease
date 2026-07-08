package com.netease.bdms.ndi.service.web.service;

import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.dto.datasource.DataSourcesResDto;
import com.netease.bdms.ndi.service.web.util.DataSourceTypeEnum;

/**
 * @ClassName OracleDataSourceService
 * @Description Oracle数据源服务
 * @Author Min Zhao
 * @Version 1.0
 **/
public interface OracleDataSourceService {

  /**
   * 列出Oracle数据源列表
   *
   * @param productId
   * @param pageNum
   * @param pageSize
   * @return
   */
  JSONObject listIdAndNameAndCatalogName(Integer productId, Integer pageNum, Integer pageSize);

  DataSourcesResDto getDataSourcesResDto(Integer productId, String searchBy, Integer pageNum, Integer pageSize);

  /**
   * Add a datasource
   *
   * @param createDataSourceParam
   * @return
   */
  Long create(JSONObject createDataSourceParam);

  /**
   * Modify a datasource
   *
   * @param modifyDataSourceParam
   * @return
   */
  JSONObject modify(JSONObject modifyDataSourceParam);
}
