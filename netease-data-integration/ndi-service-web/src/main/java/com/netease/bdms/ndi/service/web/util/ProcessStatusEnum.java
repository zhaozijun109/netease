package com.netease.bdms.ndi.service.web.util;


public enum ProcessStatusEnum {
  SUCCESS(200, "success"),

  HTTP_META_ERROR(10001, "Request failed to meta hub"),
  HTTP_REQUEST_ERROR(10002, "Failed to request using http"),
  HTTP_INVOKE_ERROR(10003, "Failed to invoke http request"),

  HTTP_ENDPOINT_RESPONSE_ERROR(10004, "The response code isn't 200"),
  ILLEGAL_ARGUMENT(20001, "Argument is invalid"),
  NO_LOGIN(30001, "User don't login in."),
  TASK_NAME_DUPLICATION(40001, "任务名称已存在"),
  UNKNOWN_EXCEPTION(50001, "系统异常"),
  DB_READ_ERROR(60001, "Database failed to read"),
  DB_WRITE_ERROR(60002, "Database failed to write"),
  METAHUB_ERROR(70001, "Metahub failed to response"),
  MAMMUT_ERROR(80001, "Mammut failed to response"),

  DATA_SOURCE_EXIST(90001, "数据源已存在"),
  DATA_SOURCE_URL_ILLEGAL(90002, "数据源url非法"),
  AUTH_ERROR(100001, "Failed to auth");

  private final int code;
  private String message;

  ProcessStatusEnum(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public int getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
