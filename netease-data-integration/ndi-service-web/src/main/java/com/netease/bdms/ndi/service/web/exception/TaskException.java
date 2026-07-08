package com.netease.bdms.ndi.service.web.exception;


/**
 * @ClassName DataSourceException
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
public class TaskException extends AbstractCommonException {
  public TaskException(int code, String message) {
    super(message);
    this.code = code;
  }

}
