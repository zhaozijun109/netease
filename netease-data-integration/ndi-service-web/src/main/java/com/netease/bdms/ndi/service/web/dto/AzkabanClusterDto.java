package com.netease.bdms.ndi.service.web.dto;

import java.util.List;

import lombok.Getter;
import org.apache.http.HttpHost;

/**
 * @ClassName AzkabanClusterDto
 * @Description az的集群信息
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
public class AzkabanClusterDto {

  private List<ClusterHost> clusterHostList;

  @Getter
  public static class ClusterHost {
    private String clusterId;
    private List<HttpHost> httpHostList;
  }

}
