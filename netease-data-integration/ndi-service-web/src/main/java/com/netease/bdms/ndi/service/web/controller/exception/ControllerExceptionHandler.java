package com.netease.bdms.ndi.service.web.controller.exception;

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import com.netease.bdms.ndi.service.web.exception.AzkabanException;
import com.netease.bdms.ndi.service.web.exception.DataSourceException;
import com.netease.bdms.ndi.service.web.exception.LoginException;
import com.netease.bdms.ndi.service.web.exception.MammutException;
import com.netease.bdms.ndi.service.web.exception.MetahubException;
import com.netease.bdms.ndi.service.web.exception.NdiException;
import com.netease.bdms.ndi.service.web.exception.RedisException;
import com.netease.bdms.ndi.service.web.exception.TaskException;
import com.netease.bdms.ndi.service.web.util.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 全局异常处理
 *
 * 第三方服务异常；
 * 参数异常；
 * 系统异常；
 * @author Min Zhao
 */
@ControllerAdvice
public class ControllerExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ControllerExceptionHandler.class);

  /**
   * 参数异常处理handler
   *
   * @param e
   * @return
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseBody
  public ResponseResult paramExceptionHandler(Exception e) {
    MethodArgumentNotValidException exception = (MethodArgumentNotValidException) e;
    BindingResult bindingResult = exception.getBindingResult();
    List<ObjectError> objectErrorList = bindingResult.getAllErrors();
    List<String> errorMessageList = objectErrorList.stream()
        .map(ObjectError::getDefaultMessage)
        .collect(Collectors.toList());
    ResponseResult result = new ResponseResult(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(),
        JSONArray.toJSONString(errorMessageList));
    return result;
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(value = HttpStatus.OK)
  @ResponseBody
  public ResponseResult exceptionHandler(Exception ex) {
    ResponseResult responseResult = new ResponseResult();
    if (ex instanceof NdiException) {
      responseResult.setCode(((NdiException) ex).getCode());
      responseResult.setMessage(ex.getMessage());
    } else if (ex instanceof MetahubException) {
      responseResult.setCode(ProcessStatusEnum.METAHUB_ERROR.getCode());
      responseResult.setMessage(ex.getMessage());
    } else if (ex instanceof MammutException) {
      responseResult.setCode(ProcessStatusEnum.MAMMUT_ERROR.getCode());
      responseResult.setMessage(ex.getMessage());
    } else if (ex instanceof TaskException) {
      responseResult.setCode(((TaskException) ex).getCode());
      responseResult.setMessage(ex.getMessage());
    } else if (ex instanceof DataSourceException) {
      responseResult.setCode(((DataSourceException) ex).getCode());
      responseResult.setMessage(ex.getMessage());
    } else if (ex instanceof IllegalArgumentException) {
      responseResult.setCode(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode());
      responseResult.setMessage(ex.getMessage());
    } else if (ex instanceof AzkabanException) {
      responseResult.setCode(((AzkabanException) ex).getCode());
      responseResult.setMessage(ex.getMessage());
    } else if (ex instanceof RedisException) {
      responseResult.setCode(((RedisException) ex).getCode());
      responseResult.setMessage(ex.getMessage());
    } else if (ex instanceof LoginException) {
      responseResult.setCode(((LoginException) ex).getCode());
      responseResult.setMessage(ex.getMessage());
    } else {
      log.error("Unknown exception.", ex);
      responseResult.setCode(ProcessStatusEnum.UNKNOWN_EXCEPTION.getCode());
      responseResult.setMessage(ProcessStatusEnum.UNKNOWN_EXCEPTION.getMessage());
    }
    return responseResult;
  }
}
