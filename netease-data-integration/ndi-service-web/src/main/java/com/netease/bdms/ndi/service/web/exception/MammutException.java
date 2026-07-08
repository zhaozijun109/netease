package com.netease.bdms.ndi.service.web.exception;

/**
 * @ClassName MammutException
 * @Description 猛犸元数据服务异常
 * @Author Min Zhao
 * @Version 1.0
 **/
public class MammutException extends RuntimeException {
  private Integer code;

  public MammutException(Integer code) {
    this.code = code;
  }

  public MammutException(Integer code, String message) {
    super(message);
    this.code = code;
  }

  public Integer getCode() {
    return code;
  }
}
