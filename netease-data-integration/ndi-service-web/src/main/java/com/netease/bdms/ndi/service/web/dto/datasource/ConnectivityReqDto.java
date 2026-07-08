package com.netease.bdms.ndi.service.web.dto.datasource;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName ConnectivityReqDto
 * @Description 连通性检测请求dto
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
@ToString
public class ConnectivityReqDto {
  /**
   * 数据源id
   */
  private Long dataSourceId;

  /**
   * 是否检测元数据中心连通性
   */
  private boolean hasMetahub;

  /**
   * 要检测的集群列表
   */
  private List<String> clusterId;
}
