package com.netease.bdms.ndi.service.web.service;

import java.util.List;

import com.netease.bdms.ndi.service.web.pojo.TaskDataSourceDO;

/**
 * @ClassName TaskDataSourceService
 * @Description 任务数据源服务
 * @Author Min Zhao
 * @Version 1.0
 **/
public interface TaskDataSourceService {

  /**
   * 通过数据源获取任务列表
   *
   * @param product 产品账号
   * @param cluster 集群
   * @param dataSourceId 数据源id
   * @return 任务id列表
   */
  List<String> listTaskListByDataSource(String product, String cluster, Long dataSourceId);

  int saveTaskDataSource(Long dataSourceId, String taskId, String product, String cluster);

  int updateTaskDataSource(Long dataSourceId, String taskId, String product, String cluster);

  int deleteByTaskId(String taskId, String product, String cluster);

  List<String> selectByDataSourceId(Long dataSourceId, String product, String cluster);
}
