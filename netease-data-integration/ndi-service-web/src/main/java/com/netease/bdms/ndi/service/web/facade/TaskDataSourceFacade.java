package com.netease.bdms.ndi.service.web.facade;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.netease.bdms.ndi.service.web.config.ContextConstant;
import com.netease.bdms.ndi.service.web.config.NdiContext;
import com.netease.bdms.ndi.service.web.dto.AzkabanJobDto;
import com.netease.bdms.ndi.service.web.dto.ClusterDto;
import com.netease.bdms.ndi.service.web.dto.DataSourceQuoteDto;
import com.netease.bdms.ndi.service.web.dto.datasource.DeleteDataSourceReqDto;
import com.netease.bdms.ndi.service.web.exception.AzkabanException;
import com.netease.bdms.ndi.service.web.exception.DataSourceException;
import com.netease.bdms.ndi.service.web.exception.NdiException;
import com.netease.bdms.ndi.service.web.helper.AzkabanHelper;
import com.netease.bdms.ndi.service.web.service.DataSourceService;
import com.netease.bdms.ndi.service.web.service.TaskDataSourceService;
import com.netease.bdms.ndi.service.web.service.TaskService;
import com.netease.bdms.ndi.service.web.service.UserService;
import com.netease.bdms.ndi.service.web.service.impl.MetahubService;
import com.netease.bdms.ndi.service.web.util.ParamUtil;
import com.netease.bdms.ndi.service.web.util.ProcessStatusEnum;
import com.netease.bdms.ndi.service.web.util.TaskConstant;
import com.netease.bdms.ndi.service.web.util.constant.ResponseCodeConstant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @ClassName TaskDataSourceFacade
 * @Description 任务和数据源facade
 * @Author Min Zhao
 * @Version 1.0
 **/
@Component
public class TaskDataSourceFacade {

  @Autowired
  private TaskService taskService;

  @Autowired
  private DataSourceService dataSourceService;

  @Autowired
  private AzkabanHelper azkabanHelper;

  @Autowired
  private UserService userService;

  @Autowired
  private MetahubService metahubService;

  @Autowired
  private TaskDataSourceService taskDataSourceService;

  /**
   * 数据源删除
   * 数据源删除时，检查该数据源是否被数据传输任务所使用
   *
   * @param deleteDataSourceParam
   * @return
   */
  public JSONObject delete(DeleteDataSourceReqDto deleteDataSourceParam) {
    ParamUtil.validate(deleteDataSourceParam);
    String modifier = NdiContext.get(ContextConstant.EMAIL);
    Integer accountId = NdiContext.get(ContextConstant.PRODUCT_ID);
    String product = NdiContext.get(ContextConstant.PRODUCT);
    String cluster = NdiContext.get(ContextConstant.CLUSTER);
    List<Long> ids = deleteDataSourceParam.getId();
    if (CollectionUtils.isEmpty(ids)) {
      throw new IllegalArgumentException("数据源列表不能为空");
    }

    // TODO: 目前支持个单个数据源删除，批量删除要保证事务
    for (Long id : ids) {
      List<String> taskIdList = taskDataSourceService.selectByDataSourceId(id, product, cluster);
      if (CollectionUtils.isNotEmpty(taskIdList)) {
        throw new DataSourceException(ResponseCodeConstant.DATA_SOURCE_QUOTED, "该数据源已有任务引用，请先解除引用后再试！");
      }
      JSONObject getDataSourceResponse = metahubService.getDataSource(accountId, id);
      if (getDataSourceResponse == null) {
        return null;
      }
      metahubService.deleteDataSource(accountId, id, modifier);
    }
    return null;
  }

  /**
   * 获取数据源的任务引用详情
   *
   * @param dataSourceId 数据源id
   * @param product 产品账号
   * @param clusterId 集群id
   * @return
   */
  public DataSourceQuoteDto getDataSourceQuote(Long dataSourceId, String product, String clusterId, String searchKey, Integer pageNum, Integer pageSize) {
    DataSourceQuoteDto dataSourceQuoteDto = new DataSourceQuoteDto();
    String cluster = NdiContext.get(ContextConstant.CLUSTER);
    List<ClusterDto> clusterDtoList = userService.listCurrentClusters(product);
    if (CollectionUtils.isEmpty(clusterDtoList)) {
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "产品账号不存在集群");
    }
    for (ClusterDto clusterDto : clusterDtoList) {
      if (StringUtils.equalsIgnoreCase(clusterDto.getClusterId(), clusterId)) {
        cluster = clusterDto.getClusterName();
      }
    }

    List<String> taskList = taskService.listTasksByDataSource(product, cluster, dataSourceId);
    if (CollectionUtils.isEmpty(taskList)) {
      dataSourceQuoteDto.setTaskList(Lists.newArrayList());
      return dataSourceQuoteDto;
    }
    dataSourceQuoteDto = taskQuote(product, clusterId, taskList, searchKey, pageNum, pageSize);
    return dataSourceQuoteDto;
  }

  public DataSourceQuoteDto taskQuote(String product, String clusterId, List<String> taskList, String searchKey, int pageNum, int pageSize) {
    if (CollectionUtils.isEmpty(taskList)) {
      throw new IllegalArgumentException("任务列表不能为空");
    }
    DataSourceQuoteDto dataSourceQuoteDto = new DataSourceQuoteDto();
    Map<String, String> sessionAndUrlMap = null;
    try {
      sessionAndUrlMap = azkabanHelper.getSessionId(clusterId);
    } catch (Exception e) {
      throw new AzkabanException(ResponseCodeConstant.AZKABAN_REQUEST_ERROR, "获取集群的session失败", e);
    }
    if (MapUtils.isEmpty(sessionAndUrlMap)) {
      return new DataSourceQuoteDto();
    }
    String sessionId = sessionAndUrlMap.get(AzkabanHelper.SESSION_ID);
    String serverUrl = sessionAndUrlMap.get(AzkabanHelper.SERVER_URL);
    String response = azkabanHelper.fetchTasksJobRelation(serverUrl, sessionId, taskList, product, searchKey, pageNum, pageSize);
    JSONObject responseJSON = JSONObject.parseObject(response);
    if (!responseJSON.containsKey("resultList") || !responseJSON.containsKey("totalNum")) {
      throw new AzkabanException("Failed to get JobRelation");
    }
    Integer totalNum = responseJSON.getInteger("totalNum");
    JSONArray resultList = responseJSON.getJSONArray("resultList");
    List<AzkabanJobDto> azkabanJobDtoList = resultList.toJavaList(AzkabanJobDto.class);

    List<DataSourceQuoteDto.TaskNodeDto> taskNodeDtoList = Lists.newArrayList();
    dataSourceQuoteDto.setTaskList(taskNodeDtoList);

    dataSourceQuoteDto.setCreator("");
    if (CollectionUtils.isEmpty(azkabanJobDtoList)) {
      return dataSourceQuoteDto;
    }
    for (AzkabanJobDto azkabanJobDto : azkabanJobDtoList) {
      DataSourceQuoteDto.TaskNodeDto taskNodeDto = new DataSourceQuoteDto.TaskNodeDto();
      DataSourceQuoteDto.AzkabanNodeDto azkabanNodeDto = new DataSourceQuoteDto.AzkabanNodeDto();
      azkabanNodeDto.setName(azkabanJobDto.getJobId());
      azkabanNodeDto.setAddress("");
      DataSourceQuoteDto.AzkabanTaskDto azkabanTaskDto = new DataSourceQuoteDto.AzkabanTaskDto();
      azkabanTaskDto.setName(azkabanJobDto.getFlowAliasName());
      azkabanTaskDto.setType(azkabanJobDto.isDevJob() ? TaskConstant.TaskTypeEnum.DEVELOP.getName() : TaskConstant.TaskTypeEnum.ONLINE.getName());
      azkabanTaskDto.setAddress("");
      taskNodeDto.setNode(azkabanNodeDto);
      taskNodeDto.setTask(azkabanTaskDto);
      String flowOwner = azkabanJobDto.getFlowOwner() == null ? "" : azkabanJobDto.getFlowOwner();
      if (StringUtils.isNotBlank(flowOwner)) {
        flowOwner = userService.getProductUsername(product, flowOwner);
      }
      taskNodeDto.setOwner(flowOwner);
      taskNodeDtoList.add(taskNodeDto);
    }
    dataSourceQuoteDto.setTotal(totalNum);
    return dataSourceQuoteDto;
  }
}
