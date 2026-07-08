package com.netease.bdms.ndi.service.web.service.impl;

import java.util.List;

import com.google.common.collect.Lists;
import com.netease.bdms.ndi.service.web.dao.TaskDataSourceDOMapper;
import com.netease.bdms.ndi.service.web.pojo.TaskDataSourceDO;
import com.netease.bdms.ndi.service.web.service.TaskDataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName TaskDataSourceServiceImpl
 * @Description 任务数据源实现
 * @Author Min Zhao
 * @Version 1.0
 **/
@Service
public class TaskDataSourceServiceImpl implements TaskDataSourceService {

  @Autowired
  private TaskDataSourceDOMapper taskDataSourceDOMapper;

  @Override
  public List<String> listTaskListByDataSource(String product, String cluster, Long dataSourceId) {
    List<String> taskList = taskDataSourceDOMapper.selectTaskListByDataSource(product, cluster, dataSourceId);
    return taskList;
  }

  @Override
  public int saveTaskDataSource(Long dataSourceId, String taskId, String product, String cluster) {
    TaskDataSourceDO taskDataSourceDO = new TaskDataSourceDO(dataSourceId, taskId, product, cluster);
    int result = taskDataSourceDOMapper.insert(taskDataSourceDO);
    return result;
  }

  @Override
  public int updateTaskDataSource(Long dataSourceId, String taskId, String product, String cluster) {
    TaskDataSourceDO taskDataSourceDO = new TaskDataSourceDO(dataSourceId, taskId, product, cluster);
    int result = taskDataSourceDOMapper.updateByPrimaryKeySelective(taskDataSourceDO);
    return result;
  }

  @Override
  public int deleteByTaskId(String taskId, String product, String cluster) {
    int result = taskDataSourceDOMapper.deleteByTaskId(taskId, product, cluster);
    return result;
  }

  @Override
  public List<String> selectByDataSourceId(Long dataSourceId, String product, String cluster) {
    List<String> taskIdList = taskDataSourceDOMapper.selectTaskListByDataSource(product, cluster, dataSourceId);
    return taskIdList;
  }
}
