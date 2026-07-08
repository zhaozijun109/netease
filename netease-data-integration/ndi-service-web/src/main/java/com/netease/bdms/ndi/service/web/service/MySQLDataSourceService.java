package com.netease.bdms.ndi.service.web.service;

import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.dto.datasource.DataSourcesResDto;
import com.netease.bdms.ndi.service.web.util.DataSourceTypeEnum;

/**
 * @ClassName MysqlDataSourceService
 * @Description MySQL
 * @Author Min Zhao
 * @Version 1.0
 **/
public interface MySQLDataSourceService {

  /**
   * List id, name and catalogName of datasource
   *
   * @return
   */
  JSONObject listIdAndNameAndCatalogName(Integer productId, Integer pageNum, Integer pageSize);

  DataSourcesResDto getDataSourcesResDto(Integer productId, String searchBy, Integer pageNum, Integer pageSize);


  /**
   * Add a datasource
   *
   * @param createDataSourceParam
   * @return 数据源id
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
