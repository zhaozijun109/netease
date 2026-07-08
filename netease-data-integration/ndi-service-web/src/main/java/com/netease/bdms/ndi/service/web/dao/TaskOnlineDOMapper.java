package com.netease.bdms.ndi.service.web.dao;

import com.netease.bdms.ndi.service.web.pojo.TaskDevelopDO;
import com.netease.bdms.ndi.service.web.pojo.TaskOnlineDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskOnlineDOMapper {
  int deleteByPrimaryKey(Long id);

  int deleteByTaskId(@Param(value = "taskId") String taskId,
                     @Param(value = "product") String product,
                     @Param(value = "cluster") String cluster);

  int insert(TaskOnlineDO record);

  int insertSelective(TaskOnlineDO record);

  TaskOnlineDO selectByPrimaryKey(Long id);

  TaskOnlineDO selectByTaskId(String taskId);

  int selectCountByTaskId(String taskId);

  List<TaskOnlineDO> selectByProductAndCluster(@Param(value = "readerType") Byte readerType,
                                               @Param(value = "writerType") Byte writerType,
                                               @Param(value = "taskName") String taskName,
                                               @Param(value = "owner") String owner,
                                               @Param(value = "readerUrl") String readerUrl,
                                               @Param(value = "writerUrl") String writerUrl,
                                               @Param(value = "writerTableName") String writerTableName,
                                               @Param(value = "product") String product,
                                               @Param(value = "cluster") String cluster,
                                               @Param(value = "sortType") Byte sortType);

  List<TaskOnlineDO> selectByProductAndClusterAndTaskName(@Param(value = "product") String product,
                                                          @Param(value = "cluster") String cluster,
                                                          @Param(value = "taskName") String taskName);

  /**
   * 根据筛选条件获取排序的任务列表
   *
   * @param readerType Reader类型
   * @param writerType Writer类型
   * @param product    产品账号
   * @param cluster    集群名称
   * @param searchType 搜索类型
   * @param searchKey   搜索值
   * @param sortKey     排序
   * @param sortType   排序类型
   * @return 任务列表
   */
  List<TaskOnlineDO> selectTaskList(@Param(value = "readerType") Byte readerType,
                                     @Param(value = "writerType") Byte writerType,
                                     @Param(value = "product") String product,
                                     @Param(value = "cluster") String cluster,
                                     @Param(value = "searchType") String searchType,
                                     @Param(value = "searchKey") String searchKey,
                                     @Param(value = "sortType") String sortType,
                                     @Param(value = "sortKey") String sortKey);

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

  int selectIdAndNameCount(@Param(value = "product") String product,
                           @Param(value = "cluster") String cluster,
                           @Param(value = "taskName") String taskName);

  int selectCountByProductAndCluster(@Param(value = "readerType") Byte readerType,
                                     @Param(value = "writerType") Byte writerType,
                                     @Param(value = "taskName") String taskName,
                                     @Param(value = "owner") String owner,
                                     @Param(value = "readerUrl") String readerUrl,
                                     @Param(value = "writerUrl") String writerUrl,
                                     @Param(value = "writerTableName") String writerTableName,
                                     @Param(value = "product") String product,
                                     @Param(value = "cluster") String cluster);

  List<String> selectOwnersByProductAndCluster(@Param(value = "product") String product,
                                               @Param(value = "cluster") String cluster);

  int selectOwnersCountByProductAndCluster(@Param(value = "product") String product,
                                           @Param(value = "cluster") String cluster);

  Integer selectVersionByTaskId(String taskId);

  Long selectReaderIdByTaskId(String id);

  Long selectWriterIdByTaskId(String id);

  int updateByPrimaryKeySelective(TaskOnlineDO record);

  int updateByTaskIdSelective(TaskOnlineDO record);

}