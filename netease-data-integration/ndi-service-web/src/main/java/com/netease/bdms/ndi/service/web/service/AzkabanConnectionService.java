package com.netease.bdms.ndi.service.web.service;

import java.util.List;
import java.util.Map;


import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.dto.datasource.AzkabanConnectionStatusAndResultDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityResultRspDto.*;
import com.netease.bdms.ndi.service.web.util.DataSourceConstant.ConnectivityResultEnum;

/**
 * @ClassName AzkabanConnectionService
 * @Description 数据源与Azkaban 集群的连通性
 * @Author Min Zhao
 * @Version 1.0
 **/
public interface AzkabanConnectionService {

  int deleteByDataSourceAndProductId(Long dataSourceId, Integer productId);
  /**
   * 执行数据源连通性检测
   *
   * @param dataSourceDetail 数据源详情
   * @param clusterIds 测试集群列表
   * @param productId 产品账号
   */
  void execute(JSONObject dataSourceDetail, Integer productId, List<String> clusterIds);

  /**
   * 获取数据源的检测状态
   *
   * @param datasourceIds
   * @param productId
   * @return
   */
  Map<Long, ConnectivityResultEnum> getExecuteStatus(List<Long> datasourceIds, Integer productId);

  /**
   * 获取数据源的检测结果
   *
   * @param dataSourceId
   * @param productId
   * @return
   */
  List<AzkabanConnectivityResult> getExecuteResult(Long dataSourceId, Integer productId);
}
