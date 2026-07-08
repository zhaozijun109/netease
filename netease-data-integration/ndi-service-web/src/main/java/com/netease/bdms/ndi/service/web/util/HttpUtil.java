package com.netease.bdms.ndi.service.web.util;

import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.exception.NdiException;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


/**
 * @ClassName HttpUtil
 * @Description Http工具类
 * @Author Min Zhao
 * @Version 1.0
 **/
public class HttpUtil {

  public static JSONObject doGet(String url) {
    RestTemplate restTemplate = new RestTemplate();
    JSONObject response = restTemplate.getForObject(url, JSONObject.class);
    return response;
  }

  /**
   * POST 方法辅助类
   *
   * @param path
   * @param params
   * @return
   */
  public static String doPostWithPathParams(String path, Map<String, Object> params) {
    RestTemplate restTemplate = new RestTemplate();
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(path).append("?");
    for (Entry entry : params.entrySet()) {
      stringBuilder.append(entry.getKey()).append("=").append(entry.getValue());
    }
    HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(params, new HttpHeaders());
    ResponseEntity<JSONObject> responseEntity = null;
    try {
      responseEntity = restTemplate.postForEntity(stringBuilder.toString(), httpEntity, JSONObject.class, params.values());
    } catch (RestClientException e) {

      throw new NdiException(ProcessStatusEnum.HTTP_INVOKE_ERROR.getCode(), "Failed to request url:" + path);
    }
    JSONObject responseEntityBody = responseEntity.getBody();
    return responseEntityBody.toJSONString();
  }
}
