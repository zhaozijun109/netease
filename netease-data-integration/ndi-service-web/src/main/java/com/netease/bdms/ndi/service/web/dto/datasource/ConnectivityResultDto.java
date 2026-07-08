package com.netease.bdms.ndi.service.web.dto.datasource;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @ClassName ConnectivityResultDto
 * @Description 连通性结果dto
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
public class ConnectivityResultDto {

  /**
   * 连通性状态
   */
  private String status;

  private List<DataSourceConResult> dataSourceConResultList;

  public ConnectivityResultDto() {
  }

  public ConnectivityResultDto(String status, List<DataSourceConResult> dataSourceConResultList) {
    this.status = status;
    this.dataSourceConResultList = dataSourceConResultList;
  }

}
