package com.netease.bdms.ndi.service.web.dto.datasource;

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
public class ConnectivityStatusDtoV2 {

  private Long dataSourceId;

  private Long checkId;
  /**
   * 执行状态
   */
  private Integer status;

  public ConnectivityStatusDtoV2() {
  }

  public ConnectivityStatusDtoV2(Long dataSourceId, Long checkId, Integer status) {
    this.dataSourceId = dataSourceId;
    this.checkId = checkId;
    this.status = status;
  }
}
