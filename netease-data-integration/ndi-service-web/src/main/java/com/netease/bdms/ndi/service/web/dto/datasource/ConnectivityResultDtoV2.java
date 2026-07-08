package com.netease.bdms.ndi.service.web.dto.datasource;

import java.util.List;

import lombok.Getter;
import lombok.Setter;


/**
 * @ClassName ConnectivityResultDto
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
public class ConnectivityResultDtoV2 {

  private Long checkId;
  /**
   * 连通性状态
   */
  private Integer status;

  private List<DataSourceConResult> dataSourceConResultList;

  public ConnectivityResultDtoV2() {
  }

  public ConnectivityResultDtoV2(Integer status, List<DataSourceConResult> dataSourceConResultList) {
    this.status = status;
    this.dataSourceConResultList = dataSourceConResultList;
  }

}
