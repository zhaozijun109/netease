package com.netease.bdms.ndi.service.web.dto.datasource;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName AzkabanConnectionStatusAndResultDto
 * @Description Azkabn的连通性状态和结果
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
public class AzkabanConnectionStatusAndResultDto {
  private Long dataSourceId;
  private Integer status;
  private Integer result;

  public AzkabanConnectionStatusAndResultDto() {
  }

  public AzkabanConnectionStatusAndResultDto(Long dataSourceId, Integer status, Integer result) {
    this.dataSourceId = dataSourceId;
    this.status = status;
    this.result = result;
  }
}
