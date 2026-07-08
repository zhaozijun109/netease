package com.netease.bdms.ndi.service.web.dto;

import com.netease.bdms.ndi.service.web.controller.interceptor.Worker;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @ClassName CheckLoginDto
 * @Description Check Login接口返回
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
@NoArgsConstructor
public class CheckLoginDto {

  /**
   * 是否登录
   */
  private Boolean hasLogin;

  /**
   * 用户session
   */
  private Worker worker;

  /**
   * ndi_session_id
   */
  private String localToken;

  public CheckLoginDto(Boolean hasLogin,
      Worker worker, String localToken) {
    this.hasLogin = hasLogin;
    this.worker = worker;
    this.localToken = localToken;
  }
}
