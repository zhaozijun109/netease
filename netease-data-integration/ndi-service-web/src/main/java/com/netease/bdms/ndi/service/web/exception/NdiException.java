package com.netease.bdms.ndi.service.web.exception;

/**
 * @ClassName NdiException
 * @Description
 * @Author Min Zhao
 * @Version 1.0
 **/

public class NdiException extends RuntimeException {
  private Integer code;

  public NdiException(String message, Integer code) {
    super(message);
    this.code = code;
  }

  public NdiException(Integer code) {
    this.code = code;
  }

  public NdiException(Integer code, String message) {
    super(message);
    this.code = code;
  }

  public Integer getCode() {
    return code;
  }
}
