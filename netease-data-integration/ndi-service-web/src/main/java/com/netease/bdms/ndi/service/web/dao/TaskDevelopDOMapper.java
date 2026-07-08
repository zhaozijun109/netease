package com.netease.bdms.ndi.service.web.dao;

import com.netease.bdms.ndi.service.web.pojo.TaskDevelopDO;
import com.netease.bdms.ndi.service.web.pojo.TaskOnlineDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskDevelopDOMapper {
  int deleteByPrimaryKey(Long id);

  int deleteByPrimaryKeys(List<Long> ids);

  int deleteByTaskIds(@Param(value = "taskIds") List<String> taskIds,
                      @Param(value = "product") String product,
                      @Param(value = "cluster") String cluster);

  int deleteByTaskId(@Param(value = "taskId") String taskId,
                     @Param(value = "product") String product,
                     @Param(value = "cluster") String cluster);

  int insert(TaskDevelopDO record);

  int insertSelective(TaskDevelopDO record);

  TaskDevelopDO selectByPrimaryKey(Long id);

  TaskDevelopDO selectByTaskId(String id);

  int selectCountByTaskId(String taskId);

  String selectTaskIdByPrimaryKey(Long id);

  List<TaskDevelopDO> selectByRWTypeAndNameAndOwnerAndWriterTable(@Param(value = "readerType") Byte readerType,
                                                                  @Param(value = "writerType") Byte writerType,
                                                                  @Param(value = "taskName") String taskName,
                                                                  @Param(value = "owner") String owner,
                                                                  @Param(value = "readerUrl") String readerUrl,
                                                                  @Param(value = "writerUrl") String writerUrl,
                                                                  @Param(value = "writerTableName") String writerTableName,
                                                                  @Param(value = "product") String product,
                                                                  @Param(value = "cluster") String cluster,
                                                                  @Param(value = "sortType") Byte sortType);

  /**
   * 根据筛选条件获取排序的任务列表
   *
   * @param readerType Reader类型
   * @param writerType Writer类型
   * @param product    产品账号
   * @param cluster    集群名称
   * @param searchType 搜索类型
   * @param searchKey   搜索值
   * @param sortBy     排序
   * @param sortType   排序类型
   * @return 任务列表
   */
  List<TaskDevelopDO> selectTaskList(@Param(value = "readerType") Byte readerType,
                                     @Param(value = "writerType") Byte writerType,
                                     @Param(value = "product") String product,
                                     @Param(value = "cluster") String cluster,
                                     @Param(value = "searchType") String searchType,
                                     @Param(value = "searchKey") String searchKey,
                                     @Param(value = "sortType") String sortType,
                                     @Param(value = "sortBy") String sortBy);

  /**
   * 根据筛选条件获取排序的任务列表
   *
   * @param readerType Reader类型
   * @param writerType Writer类型
   * @param product    产品账号
   * @param cluster    集群名称
   * @param searchType 搜索类型
   * @param searchKey   搜索值
   * @return 任务列表
   */
  int selectTaskCount(@Param(value = "readerType") Byte readerType,
                      @Param(value = "writerType") Byte writerType,
                      @Param(value = "product") String product,
                      @Param(value = "cluster") String cluster,
                      @Param(value = "searchType") String searchType,
                      @Param(value = "searchKey") String searchKey);

  /**
   * 任务名重名检测
   *
   * @param product  产品账号
   * @param cluster  集群名
   * @param taskName 任务名
   * @param taskId   任务id，编辑时传入
   * @return 任务名的数量
   */
  int selectTaskNameCount(@Param(value = "product") String product,
                          @Param(value = "cluster") String cluster,
                          @Param(value = "taskName") String taskName,
                          @Param(value = "taskId") String taskId);


  int selectTaskCountByRWTypeAndNameAndOwnerAndWriterTable(@Param(value = "readerType") Byte readerType,
                                                           @Param(value = "writerType") Byte writerType,
                                                           @Param(value = "taskName") String taskName,
                                                           @Param(value = "owner") String owner,
                                                           @Param(value = "readerUrl") String readerUrl,
                                                           @Param(value = "writerUrl") String writerUrl,
                                                           @Param(value = "writerTableName") String writerTableName,
                                                           @Param(value = "product") String product,
                                                           @Param(value = "cluster") String cluster);

  List<TaskDevelopDO> selectByProductAndClusterAndTaskName(@Param(value = "product") String product,
                                                           @Param(value = "cluster") String cluster,
                                                           @Param(value = "taskName") String taskName);

  int selectIdAndNameCount(@Param(value = "product") String product,
                           @Param(value = "cluster") String cluster,
                           @Param(value = "taskName") String taskName);

  Byte selectTaskStatusByTaskId(@Param(value = "taskId") String taskId,
                                @Param(value = "product") String product,
                                @Param(value = "cluster") String cluster);

  List<String> selectOwnersByProductAndCluster(@Param(value = "product") String product,
                                               @Param(value = "cluster") String cluster);

  int selectOwnersCountByProductAndCluster(@Param(value = "product") String product,
                                           @Param(value = "cluster") String cluster);

  Long selectReaderIdByTaskId(String id);

  Long selectWriterIdByTaskId(String id);

  int updateByPrimaryKeySelective(TaskDevelopDO record);

  int updateByTaskIdSelective(TaskDevelopDO record);

  int updateByPrimaryKey(TaskDevelopDO record);

  int updateTasksOwnerByTaskIds(@Param(value = "taskIds") List<String> taskIds,
                                @Param(value = "newOwner") String newOwner,
                                @Param(value = "product") String product);

  int updateTaskStatusByTaskId(@Param(value = "taskId") String taskId,
                               @Param(value = "status") Byte status);

}