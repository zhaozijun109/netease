package com.netease.bdms.ndi.service.web.controller.exception;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

/**
 * @ClassName RestThrowErrorHandler
 * @Description RestTemplate 错误处理
 * @Author Min Zhao
 * @Version 1.0
 **/
public class RestThrowErrorHandler implements ResponseErrorHandler {
  @Override
  public boolean hasError(ClientHttpResponse response) throws IOException {
    return true;
  }

  @Override
  public void handleError(ClientHttpResponse response) throws IOException {

  }
}
