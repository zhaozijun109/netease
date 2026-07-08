package com.netease.bdms.ndi.service.web.dto.datasource;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName CheckConnInfo
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
public class CheckConnInfo {
  private Boolean result;
  private String exceptionMessage;

  public CheckConnInfo() {
  }

  public CheckConnInfo(Boolean result, String exceptionMessage) {
    this.result = result;
    this.exceptionMessage = exceptionMessage;
  }
}
