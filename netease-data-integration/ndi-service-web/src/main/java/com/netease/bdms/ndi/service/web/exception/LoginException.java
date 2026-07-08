package com.netease.bdms.ndi.service.web.exception;

/**
 * @ClassName LoginException
 * @Description 登录异常
 * @Author Min Zhao
 * @Version 1.0
 **/
public class LoginException extends AbstractCommonException {

  public LoginException(int code, String message) {
    super(message);
    this.code = code;
  }
}
