package com.netease.bdms.ndi.service.web.dto.datasource;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @ClassName DataSourceConnecReqDto
 * @Description
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
public class DataSourceConnectivityReqDto {
  /**
   * checkId列表
   */
  private List<Long> checkIdList;

  public DataSourceConnectivityReqDto() {
  }

  public DataSourceConnectivityReqDto(List<Long> checkIdList) {
    this.checkIdList = checkIdList;
  }
}
