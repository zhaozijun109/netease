package com.netease.bdms.ndi.service.web.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.dto.task.*;
import com.netease.bdms.ndi.service.web.param.task.*;

/**
 * 任务服务
 * @author zy
 */
public interface TaskService {
  /**
   * 获取开发任务列表
   *
   * @param param
   * @return
   */
  ListDevelopTasksDTO listDevelopTasks(ListTasksParam param);

  /**
   * 获取线上任务列表
   *
   * @param param
   * @return
   */
  ListOnlineTasksDTO listOnlineTasks(ListTasksParam param);

  /**
   * 修改任务负责人
   *
   * @param modifyTasksOwnerParam
   * @return
   */
  ModifyTasksOwnerDTO modifyTaskOwner(ModifyTasksOwnerParam modifyTasksOwnerParam);

  /**
   * 提交任务
   *
   * @param submitTaskParam
   * @return
   */
  SubmitTaskDTO submitTask(SubmitTaskParam submitTaskParam);

  /**
   * 删除任务
   *
   * @param param
   * @return
   */
  void delete(DeleteTasksParam param);

  /**
   * 创建任务
   * @param param
   * @return
   */
  TaskDTO create(CreateTaskParam param);

  /**
   * 修改任务
   *
   * @param param
   * @return
   */
  TaskDTO modify(ModifyTaskParam param);

  /**
   * 获取任务详情
   *
   * @param taskId
   * @param taskType
   * @return
   */
  TaskDTO getTaskDTO(String taskId, String taskType);

  /**
   * 解析HiveSQL
   *
   * @param hiveSQLParam
   * @return
   */
  ParseHiveSQLDTO parseHiveSQL(ParseHiveSQLParam hiveSQLParam);

  /**
   * 获取任务运行时详情
   * 提供给client
   *
   * @param executeTaskParam
   * @return
   */
  ExecuteTaskDTO getExecuteTask(ExecuteTaskParam executeTaskParam);

  /**
   * 获取Hive建表SQL
   *
   * @param createHiveSQLParam
   * @return
   */
//  String getCreateHQL(JSONObject createHiveSQLParam);

  /**
   * 获取任务负责人
   *
   * @param getTaskOwnerParam
   * @return
   */
  GetTaskOwnerDTO getTaskOwner(GetTaskOwnerParam getTaskOwnerParam);

  /**
   * 获取任务名列表
   * 提供给Mammut
   *
   * @param listTaskNamesParam
   * @return
   */
  JSONObject listTaskNames(ListTaskNamesParam listTaskNamesParam);

  /**
   * 获取引用数据源的任务列表
   *
   * @param product
   * @param cluster
   * @param dataSourceId
   * @return
   */
  List<String> listTasksByDataSource(String product, String cluster, Long dataSourceId);

  /**
   * 任务名重复检测接口
   *
   * @param product 产品账号
   * @param cluster 集群名
   * @param taskName 任务名
   * @param taskId 任务id，编辑时传入
   * @return 是否重复
   */
  boolean taskNameExists(String product, String cluster, String taskName, String taskId);

}
