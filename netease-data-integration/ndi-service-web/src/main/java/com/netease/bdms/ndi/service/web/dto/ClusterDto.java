package com.netease.bdms.ndi.service.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName ClusterDto
 * @Description 集群dto
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
public class ClusterDto {
  private String clusterId;
  private String clusterName;

  public ClusterDto() {
  }

  public ClusterDto(String clusterId, String clusterName) {
    this.clusterId = clusterId;
    this.clusterName = clusterName;
  }
}
