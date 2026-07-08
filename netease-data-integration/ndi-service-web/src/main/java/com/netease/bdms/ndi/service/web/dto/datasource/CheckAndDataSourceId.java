package com.netease.bdms.ndi.service.web.dto.datasource;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName CheckIdAndDataSourceId
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
public class CheckAndDataSourceId {
  /**
   * 元数据中心连通性checkId
   */
  private Long checkId;

  /**
   * 数据源id
   */
  private Long dataSourceId;

  public CheckAndDataSourceId() {
  }

  public CheckAndDataSourceId(Long checkId, Long dataSourceId) {
    this.checkId = checkId;
    this.dataSourceId = dataSourceId;
  }
}
