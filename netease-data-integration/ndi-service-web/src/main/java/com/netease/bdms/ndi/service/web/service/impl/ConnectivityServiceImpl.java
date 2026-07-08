package com.netease.bdms.ndi.service.web.service.impl;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netease.bdms.ndi.service.web.config.ContextConstant;
import com.netease.bdms.ndi.service.web.config.NdiContext;
import com.netease.bdms.ndi.service.web.dto.datasource.CheckAndDataSourceId;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityReqDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityResultReqDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityResultRspDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityResultRspDto.*;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityStatusDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityStatusReqDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityStatusResDto;
import com.netease.bdms.ndi.service.web.dto.datasource.DataSourceConnectivityReqDto;
import com.netease.bdms.ndi.service.web.exception.DataSourceException;
import com.netease.bdms.ndi.service.web.service.AzkabanConnectionService;
import com.netease.bdms.ndi.service.web.service.ConnectivityService;
import com.netease.bdms.ndi.service.web.service.DataSourceService;
import com.netease.bdms.ndi.service.web.util.DataSourceConstant.*;
import com.netease.bdms.ndi.service.web.util.constant.ResponseCodeConstant;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName ConnectivityServiceImpl
 * @Description 数据源连通性检测服务实现
 * @Author Min Zhao
 * @Version 1.0
 **/
@Service
public class ConnectivityServiceImpl implements ConnectivityService {

  @Autowired
  private MetahubService metahubService;

  @Autowired
  private AzkabanConnectionService azkabanConnectionService;

  @Autowired
  private DataSourceService dataSourceService;

  @Override
  public CheckAndDataSourceId execute(ConnectivityReqDto connectivityReqDto) {
    Long checkId = null;
    Long datasourceId = connectivityReqDto.getDataSourceId();
    Integer productId = NdiContext.get(ContextConstant.PRODUCT_ID);
    JSONObject dataSourceDetail = dataSourceService.get(productId, datasourceId);
    if (dataSourceDetail == null) {
      throw new DataSourceException(ResponseCodeConstant.DATA_SOURCE_NO_EXIST, "数据源不存在");
    }
    if (connectivityReqDto.isHasMetahub()) {
      String catalogName = metahubService.getCatalogFromCache(datasourceId);
      String result = metahubService.dataSourceConnCheck(catalogName, productId);
      checkId = Long.parseLong(result);
    }

    List<String> clusterIds = connectivityReqDto.getClusterId();
    if (CollectionUtils.isEmpty(clusterIds)) {
      azkabanConnectionService.deleteByDataSourceAndProductId(datasourceId, productId);
      return new CheckAndDataSourceId(checkId, datasourceId);
    }

    azkabanConnectionService.execute(dataSourceDetail, productId, clusterIds);
    CheckAndDataSourceId checkAndDataSourceId = new CheckAndDataSourceId(checkId, datasourceId);
    return checkAndDataSourceId;
  }

  /**
   * @param connectivityStatusReqDto
   * @return
   */
  @Override
  public List<ConnectivityStatusResDto> status(ConnectivityStatusReqDto connectivityStatusReqDto) {
    List<ConnectivityStatusResDto> results = Lists.newArrayList();
    List<CheckAndDataSourceId> dataSources = connectivityStatusReqDto.getDataSources();
    if (CollectionUtils.isEmpty(dataSources)) {
      return results;
    }

    List<Long> notNullCheckIdList = Lists.newArrayList();
    List<Long> dataSourceIdList = Lists.newArrayList();
    dataSources.forEach(item -> {
      if (item.getCheckId() != null) {
        notNullCheckIdList.add(item.getCheckId());
      }
      dataSourceIdList.add(item.getDataSourceId());
    });
    if (CollectionUtils.isEmpty(dataSourceIdList)) {
      return results;
    }


    Integer productId = NdiContext.get(ContextConstant.PRODUCT_ID);
    Map<Long, ConnectivityResultEnum> metaHubConnectivityStatusMap = Maps.newHashMap();
    // 元数据中心的连通状态
    if (CollectionUtils.isNotEmpty(notNullCheckIdList)) {
      metaHubConnectivityStatusMap = metahubService.listMetaHubConnectivityStatus(notNullCheckIdList, productId);
    }

    // azkaban的连通状态
    Map<Long, ConnectivityResultEnum> azkabanConnectivityStatusMap = azkabanConnectionService.getExecuteStatus(dataSourceIdList, productId);

    for (CheckAndDataSourceId checkAndDataSourceId : dataSources) {
      Long checkId = checkAndDataSourceId.getCheckId();
      Long dataSourceId = checkAndDataSourceId.getDataSourceId();
      ConnectivityResultEnum metaHubConnectivity = null;
      if (checkId == null) {
        metaHubConnectivity = ConnectivityResultEnum.UNCHECKED;
      } else {
        metaHubConnectivity = metaHubConnectivityStatusMap.get(checkId);
      }

      ConnectivityResultEnum azkabanConnectivity = azkabanConnectivityStatusMap.get(dataSourceId);
      String status = handleConnectivityStatus(metaHubConnectivity, azkabanConnectivity);
      ConnectivityStatusResDto connectivityStatusResDto = new ConnectivityStatusResDto(dataSourceId, status);
      results.add(connectivityStatusResDto);
    }

    return results;
  }

  /**
   * 未检测：都未检测
   * 成功：都成功；一个成功，一个未检测
   * 失败：都失败；一个失败，一个未检测
   * 检测中：有一个检测中
   * 部分成功：其他
   *
   * @param metaHubStatus
   * @param azkabanStatus
   * @return
   */
  private String handleConnectivityStatus(ConnectivityResultEnum metaHubStatus, ConnectivityResultEnum azkabanStatus) {
    if (metaHubStatus.equalWith(ConnectivityResultEnum.UNCHECKED) && azkabanStatus.equalWith(ConnectivityResultEnum.UNCHECKED)) {
      return ConnectivityResultEnum.UNCHECKED.getName();
    } else if (metaHubStatus.equalWith(ConnectivityResultEnum.SUCCESS) && azkabanStatus.equalWith(ConnectivityResultEnum.SUCCESS)) {
      return ConnectivityResultEnum.SUCCESS.getName();
    } else if (metaHubStatus.equalWith(ConnectivityResultEnum.FAILED) && azkabanStatus.equalWith(ConnectivityResultEnum.FAILED)) {
      return ConnectivityResultEnum.FAILED.getName();
    } else if (metaHubStatus.equalWith(ConnectivityResultEnum.CHECKING) || azkabanStatus.equalWith(ConnectivityResultEnum.CHECKING)) {
      return ConnectivityResultEnum.CHECKING.getName();
    } else if ((metaHubStatus.equalWith(ConnectivityResultEnum.SUCCESS) && azkabanStatus.equalWith(ConnectivityResultEnum.UNCHECKED))
        || (metaHubStatus.equalWith(ConnectivityResultEnum.UNCHECKED) && azkabanStatus.equalWith(ConnectivityResultEnum.SUCCESS))) {
      return ConnectivityResultEnum.SUCCESS.getName();
    } else if ((metaHubStatus.equalWith(ConnectivityResultEnum.FAILED) && azkabanStatus.equalWith(ConnectivityResultEnum.UNCHECKED))
        || (metaHubStatus.equalWith(ConnectivityResultEnum.UNCHECKED) && azkabanStatus.equalWith(ConnectivityResultEnum.FAILED))) {
      return ConnectivityResultEnum.FAILED.getName();
    } else {
      return ConnectivityResultEnum.PORTION_SUCCESS.getName();
    }
  }

  @Override
  public ConnectivityResultRspDto result(ConnectivityResultReqDto connectivityResultReqDto) {
    Integer productId = NdiContext.get(ContextConstant.PRODUCT_ID);
    Long checkId = connectivityResultReqDto.getDataSource().getCheckId();
    Long dataSourceId = connectivityResultReqDto.getDataSource().getDataSourceId();
    MetahubConnectivityResult metahubConnectivityResult = metahubService.getMetaHubConnectivityResult(checkId, productId);
    List<AzkabanConnectivityResult> azkabanConnectivityResultList = azkabanConnectionService.getExecuteResult(dataSourceId, productId);
    ConnectivityResultRspDto connectivityResultRspDto = new ConnectivityResultRspDto(metahubConnectivityResult, azkabanConnectivityResultList);
    return connectivityResultRspDto;
  }

}
