package com.netease.bdms.ndi.service.web.dao;

import java.util.List;

import com.netease.bdms.ndi.service.web.pojo.TaskDataSourceDO;
import org.apache.ibatis.annotations.Param;

public interface TaskDataSourceDOMapper {
  int deleteByPrimaryKey(Long id);

  int insert(TaskDataSourceDO record);

  int insertSelective(TaskDataSourceDO record);

  TaskDataSourceDO selectByPrimaryKey(Long id);

  int updateByPrimaryKeySelective(TaskDataSourceDO record);

  int updateByPrimaryKey(TaskDataSourceDO record);

  List<String> selectTaskListByDataSource(@Param(value = "product") String product,
                                          @Param(value = "cluster") String cluster,
                                          @Param(value = "dataSourceId") Long dataSourceId);

  int deleteByTaskId(@Param(value = "taskId") String taskId,
                     @Param(value = "product") String product,
                     @Param(value = "cluster") String cluster);

}