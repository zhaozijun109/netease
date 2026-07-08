package com.netease.bdms.ndi.service.web.exception;

import com.netease.bdms.ndi.service.web.util.constant.ResponseCodeConstant;

/**
 * @ClassName DataSourceException
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
public class DataSourceException extends AbstractCommonException {
  public DataSourceException(String message) {
    super(message);
    this.code = ResponseCodeConstant.DATA_SOURCE_QUOTED;
  }

  public DataSourceException(int code, String message) {
    super(message);
    this.code = code;
  }

  public DataSourceException(String message, Throwable cause) {
    super(message, cause);
    this.code = ResponseCodeConstant.DATA_SOURCE_QUOTED;
  }
}
