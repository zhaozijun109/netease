package com.netease.bdms.ndi.service.web.dto.datasource;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName DataSourceConResult
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
@ToString
public class DataSourceConResult {
  /**
   *
   */
  private Long checkerId;
  private String ip;
  /**
   * 结束时间
   */
  private String createTime;
  private CheckConnInfo checkConnInfo;

  public DataSourceConResult() {
  }

  public DataSourceConResult(Long checkerId, String ip, String createTime, CheckConnInfo checkConnInfo) {
    this.checkerId = checkerId;
    this.ip = ip;
    this.createTime = createTime;
    this.checkConnInfo = checkConnInfo;
  }
}
