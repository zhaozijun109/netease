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
public class ConnectivityStatusDto {

  private Long dataSourceId;

  private Long checkId;
  /**
   * 执行状态
   */
  private String execStatus;
  /**
   * 当前完成的机器数
   */
  private Integer currentSize;

  /**
   * 总的机器数
   */
  private Integer totalSize;

  public ConnectivityStatusDto() {
  }

  public ConnectivityStatusDto(Long dataSourceId, Long checkId, String execStatus, Integer currentSize, Integer totalSize) {
    this.dataSourceId = dataSourceId;
    this.checkId = checkId;
    this.execStatus = execStatus;
    this.currentSize = currentSize;
    this.totalSize = totalSize;
  }

  public ConnectivityStatusDto(String execStatus, Integer currentSize, Integer totalSize) {
    this.execStatus = execStatus;
    this.currentSize = currentSize;
    this.totalSize = totalSize;
  }
}
