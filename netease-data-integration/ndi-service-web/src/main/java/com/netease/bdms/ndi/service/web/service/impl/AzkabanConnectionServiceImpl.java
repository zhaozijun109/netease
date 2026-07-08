package com.netease.bdms.ndi.service.web.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netease.bdms.ndi.service.web.config.ContextConstant;
import com.netease.bdms.ndi.service.web.config.NdiContext;
import com.netease.bdms.ndi.service.web.dao.DatasourceAzkabanConnectionDOMapper;
import com.netease.bdms.ndi.service.web.dto.ClusterDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityResultRspDto.*;
import com.netease.bdms.ndi.service.web.exception.DataSourceException;
import com.netease.bdms.ndi.service.web.exception.MetahubException;
import com.netease.bdms.ndi.service.web.helper.AzkabanHelper;
import com.netease.bdms.ndi.service.web.pojo.DatasourceAzkabanConnectionDO;
import com.netease.bdms.ndi.service.web.service.AzkabanConnectionService;
import com.netease.bdms.ndi.service.web.service.DataSourceService;
import com.netease.bdms.ndi.service.web.service.UserService;
import com.netease.bdms.ndi.service.web.util.DataSourceConstant.*;
import com.netease.bdms.ndi.service.web.util.DataSourceTypeEnum;
import com.netease.bdms.ndi.service.web.util.DateUtil;
import com.netease.bdms.ndi.service.web.util.ProcessStatusEnum;
import com.netease.bdms.ndi.service.web.util.constant.ResponseCodeConstant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;


/**
 * @ClassName AzkabanConnectionServiceImpl
 * @Description AzkabanConnectionService 的实现
 * @Author Min Zhao
 * @Version 1.0
 **/
@Service
public class AzkabanConnectionServiceImpl implements AzkabanConnectionService {
  private static final Logger log = LoggerFactory.getLogger(AzkabanConnectionService.class);

  @Autowired
  private AzkabanHelper azkabanHelper;

  @Autowired
  private DatasourceAzkabanConnectionDOMapper connectionDOMapper;

  @Autowired
  private UserService userService;

  @Autowired
  private TaskExecutor taskExecutor;


  @Override
  public void execute(JSONObject dataSourceDetail, Integer productId, List<String> clusterIds) {
    if (CollectionUtils.isEmpty(clusterIds)) {
      throw new IllegalArgumentException("ClusterIdList不能为空");
    }
    // 清理旧的检测状态
    Long datasourceId = dataSourceDetail.getLong("id");
    deleteByDataSourceAndProductId(datasourceId, productId);
    AzkabanClusterConnectivity connectivity = new AzkabanClusterConnectivity(dataSourceDetail, azkabanHelper, connectionDOMapper, clusterIds, datasourceId, productId);
    taskExecutor.execute(connectivity);
  }

  private class AzkabanClusterConnectivity implements Runnable {

    private JSONObject dataSourceDetail;

    private AzkabanHelper azkabanHelper;

    private DatasourceAzkabanConnectionDOMapper connectionDOMapper;

    private List<String> clusterIdList;

    private Long datasourceId;

    private Integer productId;

    public AzkabanClusterConnectivity() {
    }

    public AzkabanClusterConnectivity(JSONObject dataSourceDetail, AzkabanHelper azkabanHelper,
                                      DatasourceAzkabanConnectionDOMapper connectionDOMapper, List<String> clusterIdList, Long datasourceId, Integer productId) {
      this.dataSourceDetail = dataSourceDetail;
      this.azkabanHelper = azkabanHelper;
      this.connectionDOMapper = connectionDOMapper;
      this.clusterIdList = clusterIdList;
      this.datasourceId = datasourceId;
      this.productId = productId;
    }

    @Override
    public void run() {
      JSONObject connectionInfo = dataSourceDetail.getJSONObject("connectionInformation");
      String version = connectionInfo.getString("version");
      String dbUrl = connectionInfo.getString("url");
      String type = dataSourceDetail.getString("type");
      String user = connectionInfo.getString("user");
      String password = connectionInfo.getString("password");
      if (CollectionUtils.isEmpty(clusterIdList)) {
        return;
      }
      connectionDOMapper.updateCheckingStatus(datasourceId, productId, clusterIdList);
      for (String clusterId : clusterIdList) {
        taskExecutor.execute(() -> {
          Map<String, String> urlAndSession = null;
          try {
            urlAndSession = azkabanHelper.getSessionId(clusterId);
          } catch (Exception e) {
            log.error("测试集群连通性异常, clusterId: {}", clusterId, e);
          }
          if (urlAndSession == null) {
            log.error("无法获取到集群的服务器地址");
            connectionDOMapper.updateResult(ConnectivityResultEnum.FAILED.getType(), new JSONObject(), datasourceId, productId, clusterId);
            return;
          }
          String serverUrl = urlAndSession.get(AzkabanHelper.SERVER_URL);
          String sessionId = urlAndSession.get(AzkabanHelper.SESSION_ID);
          String response = null;
          try {
            response = azkabanHelper.checkConnection(serverUrl, sessionId, dbUrl, user, password, type, version);
          } catch (Exception e) {
            log.error("测试集群连通性异常, clusterId: {}", clusterId, e);
          }

          if (response == null) {
            connectionDOMapper.updateResult(ConnectivityResultEnum.FAILED.getType(), new JSONObject(), datasourceId, productId, clusterId);
          } else {
            handleResponse(response, datasourceId, productId, clusterId);
          }
        });
      }
    }
  }

  @Override
  public int deleteByDataSourceAndProductId(Long dataSourceId, Integer productId) {
    int result = connectionDOMapper.deleteByDataSourceAndProductId(dataSourceId, productId);
    return result;
  }

  private void handleResponse(String response, Long datasourceId, Integer productId, String clusterId) {
    Integer result = 0;
    JSONObject jsonObject = JSONObject.parseObject(response);
    if (jsonObject.containsKey("error")) {
      result = ConnectivityResultEnum.FAILED.getType();
      connectionDOMapper.updateResult(result, jsonObject, datasourceId, productId, clusterId);
      return;
    }

    Integer total = jsonObject.getInteger("total");
    Integer size = jsonObject.getInteger("size");
    Map<String, Object> value = jsonObject.getJSONObject("value");
    if (value == null || value.size() == 0) {
      return;
    }

    int failedNum = 0;
    for (Map.Entry<String, Object> entry : value.entrySet()) {
      JSONObject info = (JSONObject) entry.getValue();
      String status = info.getString("status");
      if (!StringUtils.equalsIgnoreCase(status, "SUCCEED")) {
        failedNum += 1;
      }
    }

    if (total == 0) {
      result = ConnectivityResultEnum.FAILED.getType();
    } else if (failedNum == total) {
      result = ConnectivityResultEnum.FAILED.getType();
    } else if (failedNum == 0) {
      result = ConnectivityResultEnum.SUCCESS.getType();
    } else {
      result = ConnectivityResultEnum.PORTION_SUCCESS.getType();
    }
    connectionDOMapper.updateResult(result, jsonObject, datasourceId, productId, clusterId);
  }

  /**
   * 返回结果数并不等于传输的数量
   * 部分数据源未检测过
   *
   * @param dataSourceIdList
   * @param productId
   * @return
   */
  @Override
  public Map<Long, ConnectivityResultEnum> getExecuteStatus(List<Long> dataSourceIdList, Integer productId) {
    if (CollectionUtils.isEmpty(dataSourceIdList)) {
      throw new IllegalArgumentException("DataSourceIdList不能为空");
    }
    Map<Long, ConnectivityResultEnum> dataSourceStatusMap = new HashedMap();
    List<DatasourceAzkabanConnectionDO> connectionDOList = connectionDOMapper.selectByDatasourceIdsAndProduct(dataSourceIdList, productId);
    if (CollectionUtils.isEmpty(connectionDOList)) {
      for (Long dataSourceId : dataSourceIdList) {
        dataSourceStatusMap.put(dataSourceId, ConnectivityResultEnum.UNCHECKED);
      }
    }

    // 通过dataSourceId进行聚合
    Map<Long, List<DatasourceAzkabanConnectionDO>> datasourceMap = connectionDOList.stream()
        .collect(Collectors.groupingBy(DatasourceAzkabanConnectionDO::getDatasourceId));

    for (Long dataSourceId : dataSourceIdList) {
      List<DatasourceAzkabanConnectionDO> azkabanConnectionDOList = datasourceMap.get(dataSourceId);
      if (CollectionUtils.isEmpty(azkabanConnectionDOList)) {
        dataSourceStatusMap.put(dataSourceId, ConnectivityResultEnum.UNCHECKED);
      } else {
        Integer status = handleStatus(azkabanConnectionDOList);
        dataSourceStatusMap.put(dataSourceId, ConnectivityResultEnum.valueOfType(status));
      }
    }

    return dataSourceStatusMap;
  }

  /**
   * 直接返回状态
   * 状态只管检测过的
   *
   * @param azkabanConnectionDOList
   * @return @see ConnectivityResultEnum
   */
  private Integer handleStatus(List<DatasourceAzkabanConnectionDO> azkabanConnectionDOList) {
    if (CollectionUtils.isEmpty(azkabanConnectionDOList)) {
      return ConnectivityResultEnum.UNCHECKED.getType();
    }

    final List<Integer> statusList = Lists.newArrayList();
    final List<Integer> resultList = Lists.newArrayList();
    azkabanConnectionDOList.forEach(item -> {
      statusList.add(item.getExecStatus());
      resultList.add(item.getExecResult());
    });
    Integer status = ConnectivityResultEnum.CHECKING.getType();
    if (statusList.contains(ConnectivityResultEnum.CHECKING.getType())) {
      return status;
    }
    if (resultList.stream().allMatch(item -> item.equals(ConnectivityResultEnum.SUCCESS.getType()))) {
      status = ConnectivityResultEnum.SUCCESS.getType();
    } else if (resultList.stream().allMatch(item -> item.equals(ConnectivityResultEnum.FAILED.getType()))) {
      status = ConnectivityResultEnum.FAILED.getType();
    } else {
      status = ConnectivityResultEnum.PORTION_SUCCESS.getType();
    }

    return status;
  }

  @Override
  public List<AzkabanConnectivityResult> getExecuteResult(Long dataSourceId, Integer productId) {
    List<DatasourceAzkabanConnectionDO> connectionDOList = connectionDOMapper.selectByDatasourceAndProduct(dataSourceId, productId);
    return handleResult(connectionDOList);
  }

  private List<AzkabanConnectivityResult> handleResult(List<DatasourceAzkabanConnectionDO> connectionDOList) {
    List<AzkabanConnectivityResult> resultList = new ArrayList<>();
    String product = NdiContext.get(ContextConstant.PRODUCT);
    List<ClusterDto> clusterDtoList = userService.listCurrentClusters(product);
    // 集群不可为空，如果为空，说明登录入口有异常
    if (CollectionUtils.isEmpty(clusterDtoList)) {
      throw new IllegalArgumentException("集群不能为空. Product: " + product);
    }

    if (CollectionUtils.isEmpty(connectionDOList)) {
      for (ClusterDto clusterDto : clusterDtoList) {
        AzkabanConnectivityResult azkabanConnectivityResult =
            new AzkabanConnectivityResult(clusterDto.getClusterId(), clusterDto.getClusterName());
        resultList.add(azkabanConnectivityResult);
      }
      return resultList;
    }

    Map<String, DatasourceAzkabanConnectionDO> datasourceAzkabanConnectionDOMap = Maps.newHashMap();
    connectionDOList.stream().forEach(item -> datasourceAzkabanConnectionDOMap.put(item.getClusterId(), item));

    for (ClusterDto clusterDto : clusterDtoList) {
      AzkabanConnectivityResult azkabanConnectivityResult =
          new AzkabanConnectivityResult(clusterDto.getClusterId(), clusterDto.getClusterName());
      DatasourceAzkabanConnectionDO connectionDO = datasourceAzkabanConnectionDOMap.get(clusterDto.getClusterId());
      if (connectionDO == null) {
        resultList.add(azkabanConnectivityResult);
        continue;
      }

      // 其他用户点击了以后
      if (ConnectivityResultEnum.CHECKING.equalWith(connectionDO.getExecStatus())) {
        azkabanConnectivityResult.setStatus(ConnectivityResultEnum.CHECKING.getName());
        resultList.add(azkabanConnectivityResult);
        continue;
      }

      Integer status = connectionDO.getExecResult();
      String updateTime = DateUtil.format(connectionDO.getModifyTime());
      List<ConnectivityResultDetail> details = Lists.newArrayList();
      JSONObject execMessage = connectionDO.getExecMessage();
      // 检测失败，未得到execMessage或value
      if (execMessage == null || MapUtils.isEmpty(execMessage.getJSONObject("value"))) {
        ConnectivityResultDetail connectivityResultDetail = new ConnectivityResultDetail();
        connectivityResultDetail.setHost("");
        connectivityResultDetail.setMessage("未连接到该集群");
        details.add(connectivityResultDetail);
        azkabanConnectivityResult
            .setUpdateTime(updateTime)
            .setStatus(ConnectivityResultEnum.FAILED.getName())
            .setFailedNum(0)
            .setTotalNum(0)
            .setDetails(details);
        resultList.add(azkabanConnectivityResult);
        continue;
      }
      Map<String, Object> value = execMessage.getJSONObject("value");
      int totalNum = value.size();
      int failedNum = 0;
      if (totalNum != 0) {
        for (Map.Entry<String, Object> entry : value.entrySet()) {
          String detailStatus = ((JSONObject) entry.getValue()).getString("status");
          if (!StringUtils.equalsIgnoreCase(detailStatus, "SUCCEED")) {
            failedNum += 1;
          }
          JSONObject messageJSON = (JSONObject) entry.getValue();
          String message = messageJSON.getString("message");
          ConnectivityResultDetail connectivityResultDetail = new ConnectivityResultDetail(
              entry.getKey(), message);
          details.add(connectivityResultDetail);
        }
      }

      if (totalNum == 0) {
        status = ConnectivityResultEnum.FAILED.getType();
      } else if (failedNum == totalNum) {
        status = ConnectivityResultEnum.FAILED.getType();
      } else if (failedNum == 0) {
        status = ConnectivityResultEnum.SUCCESS.getType();
      } else {
        status = ConnectivityResultEnum.PORTION_SUCCESS.getType();
      }

      azkabanConnectivityResult
          .setUpdateTime(updateTime)
          .setStatus(ConnectivityResultEnum.nameOfType(status))
          .setFailedNum(failedNum)
          .setTotalNum(totalNum)
          .setDetails(details);
      resultList.add(azkabanConnectivityResult);
    }
    return resultList;
  }

}
