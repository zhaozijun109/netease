package com.netease.bdms.ndi.service.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.netease.bdms.ndi.service.web.config.ContextConstant;
import com.netease.bdms.ndi.service.web.config.NdiContext;
import com.netease.bdms.ndi.service.web.dto.DataSourceQuoteDto;
import com.netease.bdms.ndi.service.web.dto.datasource.CheckAndDataSourceId;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityReqDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityResultDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityResultReqDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityResultRspDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityStatusReqDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityStatusResDto;
import com.netease.bdms.ndi.service.web.dto.datasource.DataSourceConnectivityReqDto;
import com.netease.bdms.ndi.service.web.dto.datasource.DeleteDataSourceReqDto;
import com.netease.bdms.ndi.service.web.facade.TaskDataSourceFacade;
import com.netease.bdms.ndi.service.web.service.ConnectivityService;
import com.netease.bdms.ndi.service.web.service.DataSourceService;
import com.netease.bdms.ndi.service.web.util.ParamUtil;
import com.netease.bdms.ndi.service.web.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据源控制器
 *
 * @author Min Zhao
 */
@RestController
@RequestMapping(path = "/api/v1/datasource")
public class DataSourceController {

  @Autowired
  private DataSourceService dataSourceService;

  @Autowired
  private ConnectivityService connectivityService;

  @Autowired
  private TaskDataSourceFacade taskDataSourceFacade;

  /**
   * 获取数据源详情
   *
   * @param dataSourceId 数据源id
   * @return
   */
  @GetMapping(value = "/get")
  public ResponseResult get(@RequestParam Long dataSourceId) {
    JSONObject result = dataSourceService.get(dataSourceId);
    return ResponseResult.createBySuccess(result);
  }

  /**
   * 获取当前所在集群的hive
   *
   * @return
   */
  @RequestMapping(value = "/hive/get")
  public ResponseResult getHive() {
    JSONObject result = dataSourceService.getHive();
    return ResponseResult.createBySuccess(result);
  }

  @GetMapping(value = "/hive/getIdAndCatalogName")
  public ResponseResult getHiveIdAndCatalogName() {
    JSONObject result = dataSourceService.getHiveIdAndCatalogName();
    return ResponseResult.createBySuccess(result);
  }

  /**
   * 删除数据源
   *
   * @param param
   * @return
   */
  @PostMapping(value = "/delete")
  public ResponseResult delete(@RequestBody DeleteDataSourceReqDto param) {
    ParamUtil.validate(param);
    JSONObject result = taskDataSourceFacade.delete(param);
    return ResponseResult.createBySuccess(result);
  }

  /**
   * 获取数据库下的库名列表
   *
   * @param dataSourceId 数据源id
   * @param searchKey     搜索的值
   * @param pageNum      页码
   * @param pageSize     每页的数量
   * @return 库名列表
   */
  @GetMapping(value = "/database")
  public ResponseResult listDatabase(@RequestParam(value = "dataSourceId") Long dataSourceId,
                                     @RequestParam(value = "searchKey", required = false) String searchKey,
                                     @RequestParam(value = "pageNum") Integer pageNum,
                                     @RequestParam(value = "pageSize") Integer pageSize) {
    Integer productId = NdiContext.get(ContextConstant.PRODUCT_ID);
    Preconditions.checkArgument(productId != null, "获取产品账号失败，请重新登录");
    JSONObject result = dataSourceService.listDatabase(productId, dataSourceId, searchKey, pageNum, pageSize);
    return ResponseResult.createBySuccess(result);
  }

  /**
   * 获取数据库下的表名列表
   *
   * @param dataSourceId 数据源id
   * @param databaseName 库名
   * @param searchKey     搜索值
   * @param pageNum      页码
   * @param pageSize     每页的数量
   * @return 表名列表
   */
  @GetMapping(value = "/tableName")
  public ResponseResult listTableName(@RequestParam(value = "dataSourceId") Long dataSourceId,
                                      @RequestParam(value = "databaseName") String databaseName,
                                      @RequestParam(value = "searchKey", required = false) String searchKey,
                                      @RequestParam(value = "pageNum") Integer pageNum,
                                      @RequestParam(value = "pageSize") Integer pageSize) {
    Integer productId = NdiContext.get(ContextConstant.PRODUCT_ID);
    Preconditions.checkArgument(productId != null, "获取产品账号失败，请重新登录");
    JSONObject result = dataSourceService.listTableName(productId, dataSourceId, databaseName, searchKey, pageNum, pageSize);
    return ResponseResult.createBySuccess(result);
  }

  /**
   * 通过正则表达式获取数据库下的表名列表
   *
   * @param dataSourceId 数据源id
   * @param databaseName 库名
   * @param regexp       正则表达式
   * @param pageNum      页码，默认为1
   * @param pageSize     每页的数量， 默认为10
   * @return 表名列表
   */
  @GetMapping(value = "/tableName/regexp")
  public ResponseResult<List<String>> listTableNameByRegexp(@RequestParam(value = "dataSourceId") Long dataSourceId,
                                                            @RequestParam(value = "databaseName") String databaseName,
                                                            @RequestParam(value = "regexp") String regexp,
                                                            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
    List<String> tableNameList = dataSourceService.listTableNameByRegexp(dataSourceId, databaseName, regexp, pageNum, pageSize);
    return ResponseResult.createBySuccess(tableNameList);
  }

  /**
   * 获取数据库表的列信息
   *
   * @param dataSourceId
   * @param databaseName
   * @param tableName
   * @return
   */
  @GetMapping(value = "/table")
  public ResponseResult getTable(@RequestParam(value = "dataSourceId") Long dataSourceId,
                                 @RequestParam(value = "databaseName") String databaseName,
                                 @RequestParam(value = "table") String tableName) {
    Integer productId = NdiContext.get(ContextConstant.PRODUCT_ID);
    JSONObject result = dataSourceService.getTable(productId, dataSourceId, databaseName, tableName);
    return ResponseResult.createBySuccess(result);
  }

  @PostMapping(value = "/list")
  public ResponseResult list(@RequestBody JSONObject listParam) {
    JSONObject result = dataSourceService.listDataSource(listParam);
    return ResponseResult.createBySuccess(result);
  }

//  @PostMapping(value = "/connectivity/status")
//  public ResponseResult getConnectivityStatus(@RequestBody DataSourceConnectivityReqDto connectivityReqDto) {
//    List<ConnectivityStatusDto> connectivityStatusDtoList = dataSourceService.listConnectivityStatus(connectivityReqDto);
//    return ResponseResult.createBySuccess(connectivityStatusDtoList);
//  }

  @Deprecated
  @PostMapping(value = "/connectivity/result")
  public ResponseResult getConnectivityResult(@RequestBody DataSourceConnectivityReqDto connectivityReqDto) {
    List<ConnectivityResultDto> connectivityResultDtoList = dataSourceService.listConnectivityResult(connectivityReqDto);
    return ResponseResult.createBySuccess(connectivityResultDtoList);
  }

  @Deprecated
  @PostMapping(value = "/connectivity")
  public ResponseResult connectivity(@RequestBody ConnectivityDto connectivityDto) {
    Long checkId = dataSourceService.connectivity(connectivityDto);
    return ResponseResult.createBySuccess(checkId);
  }

  /**
   * 发起数据源连通性检测
   *
   * @param connectivityReqDto
   * @return
   */
  @PostMapping(value = "/connectivity/multi")
  public ResponseResult connectivityMulti(@RequestBody ConnectivityReqDto connectivityReqDto) {
    CheckAndDataSourceId checkAndDataSourceId = connectivityService.execute(connectivityReqDto);
    return ResponseResult.createBySuccess(checkAndDataSourceId);
  }

  /**
   * 获取数据源连通性检测的状态
   *
   * @param connectivityStatusReqDto
   * @return
   */
  @PostMapping(value = "/connectivity/multi/status")
  public ResponseResult connectivityStatusMulti(@RequestBody ConnectivityStatusReqDto connectivityStatusReqDto) {
    List<ConnectivityStatusResDto> connectivityStatusResDtoList = connectivityService.status(connectivityStatusReqDto);
    return ResponseResult.createBySuccess(connectivityStatusResDtoList);
  }

  /**
   * 获取数据源连通性检测的结果
   *
   * @param connectivityResultReqDto
   * @return
   */
  @PostMapping(value = "/connectivity/multi/result")
  public ResponseResult connectivityResultMulti(@RequestBody ConnectivityResultReqDto connectivityResultReqDto) {
    ConnectivityResultRspDto connectivityResultRspDto = connectivityService.result(connectivityResultReqDto);
    return ResponseResult.createBySuccess(connectivityResultRspDto);
  }

  /**
   * 获取数据源的任务引用
   *
   * @param dataSourceId
   * @param clusterId
   * @return
   */
  @GetMapping(value = "/quote")
  public ResponseResult<DataSourceQuoteDto> getDataSourceQuote(
      @RequestParam(value = "dataSourceId") Long dataSourceId,
      @RequestParam(value = "clusterId") String clusterId,
      @RequestParam(value = "searchKey", required = false) String searchKey,
      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
      @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
    String product = NdiContext.get(ContextConstant.PRODUCT);
    DataSourceQuoteDto dataSourceQuoteDto = taskDataSourceFacade.getDataSourceQuote(dataSourceId, product, clusterId, searchKey, pageNum, pageSize);
    return ResponseResult.createBySuccess(dataSourceQuoteDto);
  }
}
