package com.netease.bdms.ndi.service.web.dto.datasource;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName ConnectivityDto
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
@ToString
public class ConnectivityDto {
  private Long dataSourceId;
  private String dataSourceType;

  public ConnectivityDto() {
  }

  public ConnectivityDto(Long dataSourceId, String dataSourceType) {
    this.dataSourceId = dataSourceId;
    this.dataSourceType = dataSourceType;
  }
}
