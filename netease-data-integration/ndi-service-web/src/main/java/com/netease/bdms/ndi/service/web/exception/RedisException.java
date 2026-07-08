package com.netease.bdms.ndi.service.web.exception;

import com.netease.bdms.ndi.service.web.util.constant.ResponseCodeConstant;

/**
 * @ClassName RedisException
 * @Description Redis异常
 * @Author Min Zhao
 * @Version 1.0
 **/
public class RedisException extends AbstractCommonException {

  public RedisException(String message) {
    super(message);
    this.code = ResponseCodeConstant.OTHER_SERVER_ERROR;
  }

  public RedisException(String message, Throwable cause) {
    super(message, cause);
    this.code = ResponseCodeConstant.OTHER_SERVER_ERROR;
  }
}
