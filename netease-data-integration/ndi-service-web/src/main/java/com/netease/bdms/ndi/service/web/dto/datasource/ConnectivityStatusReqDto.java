package com.netease.bdms.ndi.service.web.dto.datasource;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName ConnectivityStatusReqDto
 * @Description 连通性状态请求dto
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
@ToString
public class ConnectivityStatusReqDto {

  private List<CheckAndDataSourceId> dataSources;

  public ConnectivityStatusReqDto() {
  }

  public ConnectivityStatusReqDto(List<CheckAndDataSourceId> dataSources) {
    this.dataSources = dataSources;
  }
}
