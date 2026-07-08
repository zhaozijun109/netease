package com.netease.bdms.ndi.service.web.util;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResponseResult<T> {
  private int code;
  private String message;
  private T result;
  private String reqId;
  private Long cost;

  public ResponseResult() {
  }

  public ResponseResult(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public ResponseResult(int code, String message, T result) {
    this.code = code;
    this.message = message;
    this.result = result;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Object getResult() {
    return result;
  }

  public void setResult(T result) {
    this.result = result;
  }

  public static <T> ResponseResult createBySuccess() {

    return new ResponseResult(ProcessStatusEnum.SUCCESS.getCode(), ProcessStatusEnum.SUCCESS.getMessage(), null);
  }

  public static <T> ResponseResult createBySuccess(T result) {

    return new ResponseResult(ProcessStatusEnum.SUCCESS.getCode(), ProcessStatusEnum.SUCCESS.getMessage(), result);
  }

  public static <T> ResponseResult createByError(Integer code, String message) {

    return new ResponseResult(code, message);
  }


}
