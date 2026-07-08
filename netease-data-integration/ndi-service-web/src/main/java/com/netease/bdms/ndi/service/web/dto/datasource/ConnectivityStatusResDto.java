package com.netease.bdms.ndi.service.web.dto.datasource;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName ConnectivityStatusResDto
 * @Description 连通性状态响应dto
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
@ToString
public class ConnectivityStatusResDto {
  private Long dataSourceId;
  private String status;

  public ConnectivityStatusResDto() {
  }

  public ConnectivityStatusResDto(Long dataSourceId, String status) {
    this.dataSourceId = dataSourceId;
    this.status = status;
  }
}
