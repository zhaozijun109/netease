package com.netease.bdms.ndi.service.web.exception;

import com.netease.bdms.ndi.service.web.util.constant.ResponseCodeConstant;

/**
 * @ClassName AzkabanException
 * @Description Azkaban异常
 * @Author Min Zhao
 * @Version 1.0
 **/
public class AzkabanException extends AbstractCommonException {

  public AzkabanException() {

  }

  public AzkabanException(int code, String message) {
    super(message);
    this.code = code;
  }

  public AzkabanException(String message) {
    super(message);
  }

  public AzkabanException(String message, Throwable cause) {
    super(message, cause);
  }

  public AzkabanException(int code, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
  }

  /**
   * Azkaban请求异常
   */
  public static class AzkabanRequestException extends AzkabanException {

    public AzkabanRequestException() {
    }

    public AzkabanRequestException(String message) {
      super(message);
      this.code = ResponseCodeConstant.AZKABAN_REQUEST_ERROR;
    }

    public AzkabanRequestException(String message, Throwable cause) {
      super(message, cause);
      this.code = ResponseCodeConstant.AZKABAN_REQUEST_ERROR;
    }
  }

  /**
   * Azkaban响应异常
   */
  public static class AzkabanResponseException extends AzkabanException {

    public AzkabanResponseException() {
    }

    public AzkabanResponseException(String message) {
      super(message);
      this.code = ResponseCodeConstant.AZKABAN_RESPONSE_ERROR;
    }

    public AzkabanResponseException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
