package com.netease.bdms.ndi.service.web.service;

import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.dto.DataSourceQuoteDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityResultDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityResultDtoV2;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityStatusDto;
import com.netease.bdms.ndi.service.web.dto.datasource.DataSourceConnectivityReqDto;

import com.netease.bdms.ndi.service.web.dto.datasource.DataSourcesResDto;
import com.netease.bdms.ndi.service.web.dto.datasource.DeleteDataSourceReqDto;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 数据源服务
 */
public interface DataSourceService {

  /**
   * 列出数据源
   *
   * @return
   */
  JSONObject listDataSource(JSONObject listParam);

  DataSourcesResDto getDataSourceListDto(Integer productId, String type, String searchBy, Integer pageNum, Integer pageSize);

  JSONObject listCatalogWithDataSource(Integer accountId, String type, Integer pageNum, Integer pageSize);

  JSONObject listIdAndNameAndCatalogNameResult(JSONObject listDSResponse, String dataSourceType);

  JSONObject getHive();

  JSONObject getHiveIdAndCatalogName();

  /**
   * Get a datasource
   *
   * @return
   */
  JSONObject get(JSONObject getDataSourceParam);

  /**
   * Delete a datasource
   *
   * @param deleteDataSourceParam
   * @return
   */
  JSONObject delete(DeleteDataSourceReqDto deleteDataSourceParam);

  /**
   * 删除数据源
   *
   * @param id 数据源id
   */
  void delete(Long id);

  JSONObject listDatabase(Integer productId, Long dataSourceId, String searchBy, Integer pageNum, Integer pageSize);

  JSONObject listTableName(Integer productId, Long dataSourceId, String databaseName, String searchBy, Integer pageNum, Integer pageSize);

  JSONObject getTable(Integer productId, Long dataSourceId, String db, String table);

  void addCatalog(Integer accountId, String type, String name, String creator, Long dataSourceId);

  JSONObject modifyDataSource(JSONObject connectionInformation, Integer accountId, Long dataSourceId, String dataSourceType,
                              String dataSourceName, String modifier);

  String getDataSourceUrl(Long dataSourceId);

  /**
   * 获取数据源连通性检测状态
   *
   * @param reqDto
   * @return
   */
//  List<ConnectivityStatusDto> listConnectivityStatus(DataSourceConnectivityReqDto reqDto);

  /**
   * 获取数据源连通性检测结果
   *
   * @param reqDto
   * @return
   */
  List<ConnectivityResultDto> listConnectivityResult(DataSourceConnectivityReqDto reqDto);

  /**
   * 数据源连通性检测
   *
   * @param connectivityDto
   * @return
   */
  Long connectivity(ConnectivityDto connectivityDto);

  JSONObject get(String email, String product, Long dataSourceId);

  JSONObject get(Integer accountId, Long dataSourceId);

  JSONObject get(Long dataSourceId);


  Long addDataSource(String dataSourceType, String catalogType, String dataSourceName,
                     JSONObject dataSourceInfo, String creator);

  List<String> listTableNameByRegexp(Long dataSourceId, String db, String regexp, Integer pageNum, Integer pageSize);
}
