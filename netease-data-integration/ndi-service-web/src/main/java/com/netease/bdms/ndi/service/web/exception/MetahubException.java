package com.netease.bdms.ndi.service.web.exception;

/**
 * @ClassName MetahubException
 * @Description 元数据中心服务异常
 * @Author Min Zhao
 * @Version 1.0
 **/
public class MetahubException extends RuntimeException {
  private Integer code;

  public MetahubException(Integer code) {
    this.code = code;
  }

  public MetahubException(Integer code, String message) {
    super(message);
    this.code = code;
  }

  public Integer getCode() {
    return code;
  }
}
