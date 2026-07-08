package com.netease.bdms.ndi.service.web.service;

import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.dto.datasource.DataSourcesResDto;
import com.netease.bdms.ndi.service.web.util.DataSourceTypeEnum;


/**
 * @ClassName DDBDataSourceService
 * @Description DDB数据源
 * @Author Min Zhao
 * @Version 1.0
 **/
public interface DDBDataSourceService {

  /**
   * 获取登记的DBI数据源的id、name和catalog
   *
   * @return
   */
  JSONObject listDBIIdAndNameAndCatalogName(Integer productId, Integer pageNum, Integer pageSize);



  /**
   * 获取登记的QS数据源的id、name和catalog
   *
   * @return
   */
  JSONObject listQSIdAndNameAndCatalogName(Integer productId, Integer pageNum, Integer pageSize);

  DataSourcesResDto getDBIDataSourcesResDto(Integer productId, String searchBy, Integer pageNum, Integer pageSize);

  DataSourcesResDto getQSDataSourcesResDto(Integer productId, String searchBy, Integer pageNum, Integer pageSize);
  /**
   * 登记数据源
   *
   * @param param
   * @return
   */
  Long create(JSONObject param);

  /**
   * 修改数据源
   *
   * @param param
   * @return
   */
  JSONObject modify(JSONObject param);

}
