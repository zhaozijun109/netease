package com.netease.bdms.ndi.service.web.exception;

import com.netease.bdms.ndi.service.web.util.constant.ResponseCodeConstant;
import lombok.Getter;

/**
 * @ClassName AbstractCommonException
 * @Description 抽象的公共异常
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
public class AbstractCommonException extends RuntimeException {

  protected int code = ResponseCodeConstant.SERVER_ERROR;

  protected String message;

  public AbstractCommonException() {
  }

  public AbstractCommonException(String message) {
    super(message);
    this.message = message;
  }

  public AbstractCommonException(String message, Throwable cause) {
    super(message, cause);
    this.message = message;
  }
}
