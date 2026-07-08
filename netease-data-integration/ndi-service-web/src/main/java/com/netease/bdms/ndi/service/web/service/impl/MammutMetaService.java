package com.netease.bdms.ndi.service.web.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.exception.MammutException;
import com.netease.bdms.ndi.service.web.exception.MetahubException;
import com.netease.bdms.ndi.service.web.exception.NdiException;
import com.netease.bdms.ndi.service.web.util.*;
import com.netease.bdms.ndi.service.web.util.constant.CommonConstants;
import com.netease.bdms.ndi.service.web.util.constant.CommonConstants.MammutService;

import java.net.URI;
import java.security.MessageDigest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.util.UriComponentsBuilder;
import sun.misc.BASE64Encoder;

/**
 * @ClassName MammutMetaService
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Service
public class MammutMetaService {
  private static final Logger log = LoggerFactory.getLogger(MammutMetaService.class);
  @Autowired
  private RestTemplate restTemplate;
  @Autowired
  private ProjectConfigUtil configUtil;

  public JSONObject getAllUsers() throws Exception {
    String uri = "/v1/meta/users/list";
    String url = configUtil.get(MammutService.ADDRESS) + uri + "?apiKey={apiKey}&token={token}";

    Map<String, Object> param = new HashMap<>();
    param.put("apiKey", URLEncoder.encode(configUtil.get(MammutService.API_KEY), "utf-8"));
    param.put("token", URLEncoder.encode(generateToken(configUtil.get(MammutService.MASTER_KEY), uri), "utf-8"));

    JSONObject result = restTemplate.getForObject(url, JSONObject.class, param);

    if (!CommonConstants.HTTP_STATUS_CODE.SUCCESS.equals(result.getInteger("code"))) {
      throw new NdiException(ProcessStatusEnum.HTTP_REQUEST_ERROR.getCode(), result.getString("msg"));
    }
    return result;
  }

  public JSONObject getUserName(String email) throws Exception {
    String uri = "/v1/meta/username/get";
    String url = configUtil.get(MammutService.ADDRESS) + uri + "?apiKey={apiKey}&token={token}&email={email}";

    Map<String, Object> param = new HashMap<>();
    param.put("apiKey", URLEncoder.encode(configUtil.get(MammutService.API_KEY), "utf-8"));
    param.put("token", URLEncoder.encode(generateToken(configUtil.get(MammutService.MASTER_KEY), uri), "utf-8"));
    param.put("email", email);

    JSONObject result = restTemplate.getForObject(url, JSONObject.class, param);

    if (!CommonConstants.HTTP_STATUS_CODE.SUCCESS.equals(result.getInteger("code"))) {
      throw new MammutException(ProcessStatusEnum.HTTP_REQUEST_ERROR.getCode(), result.getString("msg"));
    }
    return result;
  }

  public JSONObject getAccount(String email) {
    String uri = "/v1/meta/account/list";
    String url = configUtil.get(MammutService.ADDRESS) + uri + "?apiKey={apiKey}&token={token}&service={service}&email={email}";

    Map<String, Object> param = new HashMap<>();
    try {
      param.put("apiKey", URLEncoder.encode(configUtil.get(MammutService.API_KEY), "utf-8"));
      param.put("token", URLEncoder.encode(generateToken(configUtil.get(MammutService.MASTER_KEY), uri), "utf-8"));
    } catch (UnsupportedEncodingException e) {
      log.error(e.getMessage(), e);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    param.put("email", email);
    param.put("service", CommonConstants.MAMMUT_SERVICE_NAME);

    JSONObject result = restTemplate.getForObject(url, JSONObject.class, param);

    if (!CommonConstants.HTTP_STATUS_CODE.SUCCESS.equals(result.getInteger("code"))) {
      throw new MammutException(ProcessStatusEnum.HTTP_REQUEST_ERROR.getCode(), result.getString("msg"));
    }
    return result;
  }

  public JSONObject getUserKey(String user) throws Exception {
    String uri = "/v1/meta/keytab/get";
    String url = configUtil.get(MammutService.ADDRESS) + uri + "?apiKey={apiKey}&token={token}&user={user}";

    Map<String, Object> param = new HashMap<>();
    param.put("apiKey", URLEncoder.encode(configUtil.get(MammutService.API_KEY), "utf-8"));
    param.put("token", URLEncoder.encode(generateToken(configUtil.get(MammutService.MASTER_KEY), uri), "utf-8"));
    param.put("user", user);

    JSONObject result = restTemplate.getForObject(url, JSONObject.class, param);
    if (!CommonConstants.HTTP_STATUS_CODE.SUCCESS.equals(result.getInteger("code"))) {
      throw new NdiException(ProcessStatusEnum.HTTP_REQUEST_ERROR.getCode(), result.getString("msg"));
    }
    return result;
  }

  public JSONObject getXmlByHiveid(String clusterId, String product) throws Exception {
    String uri = "/v1/meta/hivesite/get";
    String url = configUtil.get(MammutService.ADDRESS) + uri + "?apiKey={apiKey}&token={token}&product={product}&clusterId={clusterId}";

    Map<String, Object> param = new HashMap<>();
    param.put("apiKey", URLEncoder.encode(configUtil.get(MammutService.API_KEY), "utf-8"));
    param.put("token", URLEncoder.encode(generateToken(configUtil.get(MammutService.MASTER_KEY), uri), "utf-8"));
    param.put("product", product);
    param.put("clusterId", clusterId);

    JSONObject result = restTemplate.getForObject(url, JSONObject.class, param);

    if (!CommonConstants.HTTP_STATUS_CODE.SUCCESS.equals(result.getInteger("code"))) {
      throw new NdiException(ProcessStatusEnum.HTTP_REQUEST_ERROR.getCode(), result.getString("msg"));
    }
    return result;
  }

  public JSONObject getClusters(String product) {
    String uri = "/v1/meta/cluster/list";
    String url = configUtil.get(MammutService.ADDRESS) + uri + "?apiKey={apiKey}&token={token}&product={product}";

    Map<String, Object> param = new HashMap<>();
    try {
      param.put("apiKey", URLEncoder.encode(configUtil.get(MammutService.API_KEY), "utf-8"));
      param.put("token", URLEncoder.encode(generateToken(configUtil.get(MammutService.MASTER_KEY), uri), "utf-8"));
    } catch (UnsupportedEncodingException e) {
      log.error(e.getMessage(), e);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    param.put("product", product);
    JSONObject result = restTemplate.getForObject(url, JSONObject.class, param);

    if (!CommonConstants.HTTP_STATUS_CODE.SUCCESS.equals(result.getInteger("code"))) {
      throw new MammutException(ProcessStatusEnum.HTTP_REQUEST_ERROR.getCode(), result.getString("msg"));
    }
    return result;
  }

  public List<String> getClusterName(String product) {
    List<String> clusterNames = new ArrayList<>();
    JSONObject response = getClusters(product);
    JSONArray result = response.getJSONArray("result");
    if (result != null && result.size() > 0) {
      for (int i = 0; i < result.size(); i++) {
        JSONObject item = result.getJSONObject(i);
        clusterNames.add(item.getString("name"));
      }
    }
    return clusterNames;
  }

  public JSONObject getProductUsers(String product) {
    String uri = "/v1/meta/productusers/list";
    String url = configUtil.get(MammutService.ADDRESS) + uri + "?apiKey={apiKey}&token={token}&product={product}";
    Map<String, Object> param = new HashMap<>();
    try {
      param.put("apiKey", URLEncoder.encode(configUtil.get(MammutService.API_KEY), "utf-8"));
      param.put("token", URLEncoder.encode(generateToken(configUtil.get(MammutService.MASTER_KEY), uri), "utf-8"));
    } catch (UnsupportedEncodingException e) {
      log.error("Encode error", e);
    } catch (Exception e) {
      log.error("Encode error", e);
    }
    param.put("product", product);
    JSONObject result = restTemplate.getForObject(url, JSONObject.class, param);

    if (!CommonConstants.HTTP_STATUS_CODE.SUCCESS.equals(result.getInteger("code"))) {
      throw new MammutException(ProcessStatusEnum.HTTP_REQUEST_ERROR.getCode(), result.getString("msg"));
    }
    return result;
  }

  public JSONObject isAdmin(String email) throws Exception {
    String uri = "/v1/account/user/is-admin";
    String url = configUtil.get(MammutService.ADDRESS) + uri + "?apiKey={apiKey}&token={token}&service={service}&email={email}";
    Map<String, Object> param = new HashMap<>();
    param.put("apiKey", URLEncoder.encode(configUtil.get(MammutService.API_KEY), "utf-8"));
    param.put("token", URLEncoder.encode(generateToken(configUtil.get(MammutService.MASTER_KEY), uri), "utf-8"));
    param.put("service", CommonConstants.MAMMUT_SERVICE_NAME);
    param.put("email", email);
    JSONObject result = restTemplate.getForObject(url, JSONObject.class, param);
    if (!CommonConstants.HTTP_STATUS_CODE.SUCCESS.equals(result.getInteger("code"))) {
      throw new NdiException(ProcessStatusEnum.HTTP_REQUEST_ERROR.getCode(), result.getString("msg"));
    }
    return result;
  }

  public JSONObject isUserBounded(String email, String product) throws Exception {
    String uri = "/v1/account/user-bound/check";
    String url = configUtil.get(MammutService.ADDRESS) + uri +
      "?apiKey={apiKey}&token={token}&service={service}&email={email}&product={product}";
    Map<String, Object> param = new HashMap<>();
    param.put("apiKey", URLEncoder.encode(configUtil.get(MammutService.API_KEY), "utf-8"));
    param.put("token", URLEncoder.encode(generateToken(configUtil.get(MammutService.MASTER_KEY), uri), "utf-8"));
    param.put("service", CommonConstants.MAMMUT_SERVICE_NAME);
    param.put("email", email);
    param.put("product", product);

    JSONObject result = restTemplate.getForObject(url, JSONObject.class, param);

    if (!CommonConstants.HTTP_STATUS_CODE.SUCCESS.equals(result.getInteger("code"))) {
      throw new NdiException(ProcessStatusEnum.HTTP_REQUEST_ERROR.getCode(), result.getString("msg"));
    }
    return result;
  }

  public JSONObject listProduct(String email) {
    ParamUtil.validate(email);
    StringBuilder urlBuilder = new StringBuilder();
    String api = "/v1/meta/product/list";
    urlBuilder.append(configUtil.get(MammutService.ADDRESS)).append(api)
      .append("?token={token}").append("&apiKey={apiKey}").append("&email={email}").append("&service={service}");

    Map<String, Object> param = new HashMap<>();
    try {
      param.put("apiKey", URLEncoder.encode(configUtil.get(MammutService.API_KEY), "utf-8"));
      param.put("token", URLEncoder.encode(generateToken(configUtil.get(MammutService.MASTER_KEY), api), "utf-8"));
    } catch (UnsupportedEncodingException e) {
      log.error(e.getMessage(), e);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    param.put("email", email);
    param.put("service", CommonConstants.MAMMUT_SERVICE_NAME);
    ResponseEntity<JSONObject> responseEntity = null;
    try {
      responseEntity = restTemplate.getForEntity(urlBuilder.toString(), JSONObject.class, param);
    } catch (HttpClientErrorException e) {
      log.error("param:{}," + "_"+ e.getMessage(), param.toString(), e);
      throw new NdiException(ProcessStatusEnum.HTTP_INVOKE_ERROR.getCode(), ProcessStatusEnum.HTTP_INVOKE_ERROR.getMessage());
    }
    if (responseEntity.getStatusCode().is2xxSuccessful()) {
      return responseEntity.getBody();
    } else {
      log.error(ProcessStatusEnum.HTTP_ENDPOINT_RESPONSE_ERROR.getMessage() + "/n" + "url:" + urlBuilder.toString());
      throw new MammutException(ProcessStatusEnum.HTTP_ENDPOINT_RESPONSE_ERROR.getCode(),
        ProcessStatusEnum.HTTP_ENDPOINT_RESPONSE_ERROR.getMessage());
    }
  }

  public JSONObject getAccountByEmail(String email) {
    ParamUtil.validate(email);
    StringBuilder urlBuilder = new StringBuilder();
    String api = "/v1/account/accountgroups/getbyemail";
    urlBuilder.append(configUtil.get(MammutService.ADDRESS)).append(api)
        .append("?token={token}").append("&apiKey={apiKey}").append("&email={email}").append("&service={service}");

    Map<String, Object> param = new HashMap<>();
    try {
      param.put("apiKey", URLEncoder.encode(configUtil.get(MammutService.API_KEY), "utf-8"));
      param.put("token", URLEncoder.encode(generateToken(configUtil.get(MammutService.MASTER_KEY), api), "utf-8"));
    } catch (UnsupportedEncodingException e) {
      log.error(e.getMessage(), e);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    param.put("email", email);
    param.put("service", CommonConstants.MAMMUT_SERVICE_NAME);
    ResponseEntity<JSONObject> responseEntity = null;
    try {
      responseEntity = restTemplate.getForEntity(urlBuilder.toString(), JSONObject.class, param);
    } catch (HttpClientErrorException e) {
      log.error("param:{}," + "_"+ e.getMessage(), param.toString(), e);
      throw new NdiException(ProcessStatusEnum.HTTP_INVOKE_ERROR.getCode(), ProcessStatusEnum.HTTP_INVOKE_ERROR.getMessage());
    }
    if (responseEntity.getStatusCode().is2xxSuccessful()) {
      return responseEntity.getBody();
    } else {
      log.error(ProcessStatusEnum.HTTP_ENDPOINT_RESPONSE_ERROR.getMessage() + "/n" + "url:" + urlBuilder.toString());
      throw new MammutException(ProcessStatusEnum.HTTP_ENDPOINT_RESPONSE_ERROR.getCode(),
          ProcessStatusEnum.HTTP_ENDPOINT_RESPONSE_ERROR.getMessage());
    }
  }

//  public JSONObject getTaskType(String product, String clusterId, Long projectId) {
//    StringBuilder urlBuilder = new StringBuilder();
//    String api = "/v1/meta/tasktype/get";
//    urlBuilder.append(configUtil.get(MammutService.ADDRESS)).append(api)
//        .append("?token={token}").append("&apiKey={apiKey}").append("&email={email}").append("&service={service}");
//
//    Map<String, Object> param = new HashMap<>();
//    try {
//      param.put("apiKey", URLEncoder.encode(configUtil.get(MammutService.API_KEY), "utf-8"));
//      param.put("token", URLEncoder.encode(generateToken(configUtil.get(MammutService.MASTER_KEY), api), "utf-8"));
//    } catch (UnsupportedEncodingException e) {
//      log.error(e.getMessage(), e);
//    } catch (Exception e) {
//      log.error(e.getMessage(), e);
//    }
//
//    param.put("product", product);
//    param.put("clusterId", clusterId);
//    param.put("projectId", projectId);
//    param.put("service", CommonConstants.MAMMUT_SERVICE_NAME);
//    ResponseEntity<JSONObject> responseEntity = null;
//    try {
//      responseEntity = restTemplate.getForEntity(urlBuilder.toString(), JSONObject.class, param);
//    } catch (HttpClientErrorException e) {
//      log.error("param:{}," + "_"+ e.getMessage(), param.toString(), e);
//      throw new NdiException(ProcessStatusEnum.HTTP_INVOKE_ERROR.getCode(), ProcessStatusEnum.HTTP_INVOKE_ERROR.getMessage());
//    }
//    if (responseEntity.getStatusCode().is2xxSuccessful()) {
//      return responseEntity.getBody();
//    } else {
//      log.error(ProcessStatusEnum.HTTP_ENDPOINT_RESPONSE_ERROR.getMessage() + "/n" + "url:" + urlBuilder.toString());
//      throw new MammutException(ProcessStatusEnum.HTTP_ENDPOINT_RESPONSE_ERROR.getCode(),
//          ProcessStatusEnum.HTTP_ENDPOINT_RESPONSE_ERROR.getMessage());
//    }
//  }

  public JSONObject listCluster(String clusterId) {
    StringBuilder urlBuilder = new StringBuilder();
    String api = "/v1/meta/clusters/getByClusterId";
    urlBuilder.append(configUtil.get(MammutService.ADDRESS)).append(api)
        .append("?token={token}").append("&apiKey={apiKey}").append("&clusterid={clusterid}").append("&service={service}");

    Map<String, Object> param = new HashMap<>();
    try {
      param.put("apiKey", URLEncoder.encode(configUtil.get(MammutService.API_KEY), "utf-8"));
      param.put("token", URLEncoder.encode(generateToken(configUtil.get(MammutService.MASTER_KEY), api), "utf-8"));
    } catch (UnsupportedEncodingException e) {
      log.error(e.getMessage(), e);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    param.put("clusterid", clusterId);
    param.put("service", CommonConstants.MAMMUT_SERVICE_NAME);
    ResponseEntity<JSONObject> responseEntity = null;
    try {
      responseEntity = restTemplate.getForEntity(urlBuilder.toString(), JSONObject.class, param);
    } catch (HttpClientErrorException e) {
      log.error("param:{}," + "_"+ e.getMessage(), param.toString(), e);
      throw new NdiException(ProcessStatusEnum.HTTP_INVOKE_ERROR.getCode(), ProcessStatusEnum.HTTP_INVOKE_ERROR.getMessage());
    }
    if (responseEntity.getStatusCode().is2xxSuccessful()) {
      return responseEntity.getBody();
    } else {
      log.error(ProcessStatusEnum.HTTP_ENDPOINT_RESPONSE_ERROR.getMessage() + "/n" + "url:" + urlBuilder.toString());
      throw new MammutException(ProcessStatusEnum.HTTP_ENDPOINT_RESPONSE_ERROR.getCode(),
          ProcessStatusEnum.HTTP_ENDPOINT_RESPONSE_ERROR.getMessage());
    }
  }

  /**
   * GET 方法辅助类
   *
   * @param path
   * @param params
   * @return
   */
  private String doGet(String path, Map<String, Object> params) {
    UriComponentsBuilder builder = UriComponentsBuilder.
        fromUriString(configUtil.get(CommonConstants.MammutService.ADDRESS))
        .path(path);

    for (String key : params.keySet()) {
      if (params.get(key) != null) {
        try {
          builder.queryParam(key, URLEncoder.encode(params.get(key).toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          log.warn("UnsupportedEncodingException, param: {}", params.get(key).toString(), e);
        }
      }
    }
    URI uri = builder.build(true).toUri();

    long startTime = System.currentTimeMillis();
    log.info("###[MetaHub request] url: {}, params: {}", uri.toString(), JSONObject.toJSONString(params));
    ResponseEntity<JSONObject> responseEntity = null;
    HttpEntity httpEntity = new HttpEntity(null);
    try {
      responseEntity = restTemplate
          .exchange(uri.toString(), HttpMethod.GET, httpEntity, JSONObject.class);
    } catch (RestClientException e) {
      log.error(ProcessStatusEnum.HTTP_INVOKE_ERROR.getMessage(), e);
      throw new NdiException(ProcessStatusEnum.HTTP_INVOKE_ERROR.getCode(),
          "Failed to request url:" + path);
    }
//    if (!responseEntity.getStatusCode().is2xxSuccessful()) {
//      throw new MetahubException(ProcessStatusEnum.HTTP_ENDPOINT_RESPONSE_ERROR.getCode(), "Metahub响应非2XX");
//    }
    JSONObject responseBody = responseEntity.getBody();
    log.info("###[MetaHub response] cost: {}, content: {}", System.currentTimeMillis() - startTime, responseBody.toJSONString());
    Integer code = responseBody.getInteger("code");
    if (!CommonConstants.HTTP_STATUS_CODE.SUCCESS.equals(code)) {
      log.error(responseBody.getString("message"));
      throw new MetahubException(code, "MetaHub exception: " + responseBody.getString("message"));
    }

    return JSONObject.toJSONString(responseBody.get("data"));
  }

  /**
   * GET 方法辅助类
   *
   * @param path
   * @param params
   * @return
   */
  private JSONObject doGetWithBody(String path, Map<String, Object> params) {
    UriComponentsBuilder builder = UriComponentsBuilder
        .fromUriString(configUtil.get(CommonConstants.MammutService.ADDRESS))
        .path(path);
    for (String key : params.keySet()) {
      if (params.get(key) != null) {
        try {
          builder.queryParam(key, URLEncoder.encode(params.get(key).toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          log.warn("UnsupportedEncodingException, param: {}", params.get(key).toString(), e);
        }
      }
    }
    URI uri = builder.build(true).toUri();
    long startTime = System.currentTimeMillis();
    log.info("###[MetaHub request] url: {}, params: {}", uri.toString(), JSONObject.toJSONString(params));
    ResponseEntity<JSONObject> responseEntity = null;
    HttpEntity httpEntity = new HttpEntity(null);
    try {
      responseEntity = restTemplate
          .exchange(uri, HttpMethod.GET, httpEntity, JSONObject.class);
    } catch (RestClientException e) {
      log.error(ProcessStatusEnum.HTTP_INVOKE_ERROR.getMessage(), e);
      throw new NdiException(ProcessStatusEnum.HTTP_INVOKE_ERROR.getCode(),
          "Failed to request url:" + path);
    }
//    if (!responseEntity.getStatusCode().is2xxSuccessful()) {
//      throw new MetahubException(ProcessStatusEnum.HTTP_ENDPOINT_RESPONSE_ERROR.getCode(), "Metahub响应非2XX");
//    }
    JSONObject responseBody = responseEntity.getBody();
    log.info("###[MetaHub Response] cost:{}, content: {}", System.currentTimeMillis() - startTime, responseBody.toJSONString());
    return responseBody;
  }

  /**
   * POST 方法辅助类
   *
   * @param path
   * @param params
   * @return
   */
  private String doPost(String path, Map<String, Object> params) {
    StringBuilder urlBuilder = new StringBuilder(configUtil.get(CommonConstants.MammutService.ADDRESS))
        .append(path);
    HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(params);
    ResponseEntity<JSONObject> responseEntity = null;
    long startTime = System.currentTimeMillis();
    try {
      log.info("###[MetaHub Request] url: {}, params: {}", path, params);
      responseEntity = restTemplate
          .postForEntity(urlBuilder.toString(), httpEntity, JSONObject.class);
    } catch (RestClientException e) {
      log.error(ProcessStatusEnum.HTTP_INVOKE_ERROR.getMessage(), e);
      throw new NdiException(ProcessStatusEnum.HTTP_INVOKE_ERROR.getCode(),
          "Failed to request url:" + path);
    }

//    if (!responseEntity.getStatusCode().is2xxSuccessful()) {
//      throw new MetahubException(ProcessStatusEnum.HTTP_ENDPOINT_RESPONSE_ERROR.getCode(), "Metahub响应非2XX");
//    }
    JSONObject responseEntityBody = responseEntity.getBody();
    log.info("###[MetaHub Response] cost: {} ms, content: {}", System.currentTimeMillis() - startTime, responseEntityBody.toJSONString());
    Integer code = responseEntityBody.getInteger("code");
    if (!CommonConstants.HTTP_STATUS_CODE.SUCCESS.equals(code)) {
      log.error(responseEntityBody.getString("message"));
      throw new MetahubException(code,
          "MetaHub exception: " + responseEntityBody.getString("message"));
    }
    return JSONObject.toJSONString(responseEntityBody.get("data"));
  }

  /**
   * 带有响应体的post方法
   *
   * @param path
   * @param params
   * @return code, message, data
   */
  private JSONObject doPostWithBody(String path, Map<String, Object> params) {
    StringBuilder urlBuilder = new StringBuilder(configUtil.get(CommonConstants.MammutService.ADDRESS))
        .append(path);
    HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(params);
    ResponseEntity<JSONObject> responseEntity = null;
    long startTime = System.currentTimeMillis();
    try {
      log.info("###[MetaHub Request] url: {}, params: {}", path, params);
      responseEntity = restTemplate
          .postForEntity(urlBuilder.toString(), httpEntity, JSONObject.class);
    } catch (RestClientException e) {
      log.error(ProcessStatusEnum.HTTP_INVOKE_ERROR.getMessage(), e);
      throw new NdiException(ProcessStatusEnum.HTTP_INVOKE_ERROR.getCode(),
          "Failed to request url:" + path);
    }

//    if (!responseEntity.getStatusCode().is2xxSuccessful()) {
//      throw new MetahubException(ProcessStatusEnum.HTTP_ENDPOINT_RESPONSE_ERROR.getCode(), "Metahub响应非2XX");
//    }

    JSONObject responseBody = responseEntity.getBody();
    log.info("###[MetaHub Response] cost: {} ms, content: {}", System.currentTimeMillis() - startTime, responseBody.toJSONString());
    return responseBody;
  }


  private String generateToken(String masterKey, String uri) throws Exception {
    BASE64Encoder base64en = new BASE64Encoder();
    MessageDigest md5 = MessageDigest.getInstance("MD5");
    return base64en.encode(md5.digest((masterKey + uri).getBytes("utf-8")));
  }
}
