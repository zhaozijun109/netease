package com.netease.bdms.ndi.service.web.exception;

/**
 * @ClassName ServerException
 * @Description 服务异常
 * @Author Min Zhao
 * @Version 1.0
 **/
public class ServerException extends AbstractCommonException {
  public ServerException() {
  }

  public ServerException(String message) {
    super(message);
  }
}
