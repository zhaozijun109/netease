package com.netease.bdms.ndi.service.web.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.pojo.DatasourceAzkabanConnectionDO;
import org.apache.ibatis.annotations.Param;

public interface DatasourceAzkabanConnectionDOMapper {
  int deleteByPrimaryKey(Long id);

  int insert(DatasourceAzkabanConnectionDO record);

  int insertSelective(DatasourceAzkabanConnectionDO record);

  DatasourceAzkabanConnectionDO selectByPrimaryKey(Long id);

  int updateByPrimaryKeySelective(DatasourceAzkabanConnectionDO record);

  int updateByPrimaryKey(DatasourceAzkabanConnectionDO record);

  /**
   * 检测结果更新为正在检测中
   *
   * @param productId    产品账号
   * @param clusterIds   集群id列表
   * @param datasourceId 数据源id
   * @return 更新状态
   */
  int updateCheckingStatus(@Param(value = "datasourceId") Long datasourceId,
                           @Param(value = "productId") Integer productId,
                           @Param(value = "clusterIds") List<String> clusterIds);

  /**
   * 检测结果更新为已完成
   *
   * @param productId    产品账号
   * @param clusterIds   集群id列表
   * @param datasourceId 数据源id
   * @return 更新状态
   */
  int  updateFinishedStatus(@Param(value = "datasourceId") Long datasourceId,
                           @Param(value = "productId") Integer productId,
                           @Param(value = "clusterIds") List<String> clusterIds);

  int updateResult(@Param(value = "execResult") Integer result,
                   @Param(value = "execMessage") JSONObject message,
                   @Param(value = "datasourceId") long datasourceId,
                   @Param(value = "productId") int productId,
                   @Param(value = "clusterId") String clusterId);

  int deleteByDataSourceAndProductId(@Param(value = "datasourceId") Long datasourceId,
                                     @Param(value = "productId") int productId);

  List<DatasourceAzkabanConnectionDO> selectByDatasourceIdsAndProduct(@Param(value = "datasourceIds") List<Long> datasourceIds,
                                                                      @Param(value = "productId") int productId);

  List<DatasourceAzkabanConnectionDO> selectByDatasourceAndProduct(@Param(value = "datasourceId") Long datasourceId,
                                                             @Param(value = "productId") int product);

}