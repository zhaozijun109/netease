package com.netease.bdms.ndi.service.web.helper;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.netease.bdms.ndi.service.web.exception.AzkabanException.*;
import com.netease.bdms.ndi.service.web.service.ConfigService;
import com.netease.bdms.ndi.service.web.util.DataSourceTypeEnum;
import com.netease.bdms.ndi.service.web.util.constant.CommonConstants;
import com.netease.bdms.ndi.service.web.util.HttpClientUtil;
import com.netease.bdms.ndi.service.web.util.ProjectConfigUtil;
import lombok.Getter;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * @ClassName AzkabanHelper
 * @Description 调用Azkaban接口实现类
 * @Author Min Zhao
 * @Version 1.0
 **/
@Component
public class AzkabanHelper {

  private static final Logger log = LoggerFactory.getLogger(AzkabanHelper.class);

  @Autowired
  private ProjectConfigUtil configUtil;

  @Autowired
  private ConfigService configService;

  /**
   * azkaban用户名
   */
  private String azkabanUserName;

  /**
   * azkaban密码
   */
  private String azkabanPassword;

  public static final String SERVER_URL = "server_url";

  public static final String SESSION_ID = "session_id";

  /**
   * Azkaban服务器url缓存
   */
  private ConcurrentHashMap<String, URLManager> clusterUrlManagerMap = new ConcurrentHashMap<>();

  @PostConstruct
  public void init() {
    azkabanUserName = configUtil.get(CommonConstants.AzkabanConfig.AZKABAN_USER_NAME);
    azkabanPassword = configUtil.get(CommonConstants.AzkabanConfig.AZKABAN_PASSWORD);
    Map<String, Object> azkabanInfoMap = configService.getAzkabanUrlConfig();
    if (MapUtils.isEmpty(azkabanInfoMap)) {
      return;
    }
    for (Map.Entry<String, Object> entry : azkabanInfoMap.entrySet()) {
      AzkabanHelper.URLManager urlManager = new AzkabanHelper.URLManager((List<String>) entry.getValue());
      clusterUrlManagerMap.put(entry.getKey(), urlManager);
    }
  }

  public String login(String url, String username, String password) throws HttpHostConnectException {
    Map<String, Object> params = Maps.newHashMap();
    params.put("action", "login");
    params.put("username", username);
    params.put("password", password);
    String responseJSON = doPostByPath(url, "", params);
    JSONObject response = JSONObject.parseObject(responseJSON);
    String sessionId = response.getString("session.id");
    if (sessionId == null) {
      String error = response.getString("error");
      log.warn("Failed to get session id from azkaban. The error information is {}", error);
      throw new AzkabanResponseException(error);
    }
    return sessionId;
  }

  public Map<String, String> getSessionId(String clusterId) {
    Map<String, String> resultMap = Maps.newHashMap();
    URLManager urlManager = clusterUrlManagerMap.get(clusterId);
    if (urlManager == null) {
      log.error("没有获取到集群: [{}]的服务器地址", clusterId);
      return null;
    }
    String sessionId = null;
    String url = null;
    for (int i = 0; i < urlManager.size; i++) {
      url = urlManager.getURL();
      try {
        sessionId = login(url, azkabanUserName, azkabanPassword);
      } catch (HttpHostConnectException e) {
        urlManager.next();
        continue;
      }
      if (sessionId != null) {
        break;
      }
    }
    if (sessionId == null) {
      throw new AzkabanResponseException("Failed to get sessionId");
    }
    resultMap.put(SESSION_ID, sessionId);
    resultMap.put(SERVER_URL, url);
    return resultMap;
  }


  public String checkConnectionByURLManager(String clusterId, String dbUrl, String user, String password, String type, String version) {
    URLManager urlManager = clusterUrlManagerMap.get(clusterId);
    if (urlManager == null) {
      log.error("没有获取到集群: [{}]的服务器地址", clusterId);
      return null;
    }
    String sessionId = null;
    String url = null;
    for (int i = 0; i < urlManager.size; i++) {
      url = urlManager.getURL();
      try {
        sessionId = login(url, azkabanUserName, azkabanPassword);
      } catch (HttpHostConnectException e) {
        urlManager.next();
        continue;
      }
      if (sessionId != null) {
        break;
      }
    }
    if (sessionId == null) {
      throw new AzkabanResponseException("Failed to get sessionId");
    }

    String connectionResponse = checkConnection(url, sessionId, dbUrl, user, password, type, version);
    return connectionResponse;
  }

  /**
   * 数据源连通性检测
   *
   * @return
   */
  public String checkConnection(String url, String sessionId, String dbUrl, String user, String password, String type, String version) {
    String path = "/logic";
    Map<String, Object> params = Maps.newHashMap();
    params.put("ajax", "checkconnection");
    params.put("user", user);
    params.put("password", password);
    if (DataSourceTypeEnum.DDB.equalWith(type)) {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("jdbc:ddb://").append(dbUrl).append("?user=").append(url).append("&password=").append(password);
      dbUrl = stringBuilder.toString();
      params.put("version", version);
    }
    params.put("dburl", dbUrl);
    String response = null;
    try {
      response = doGet(url, path, sessionId, params);
    } catch (HttpHostConnectException e) {
      throw new AzkabanRequestException("连接Azkaban服务器失败", e);
    }
    return response;
  }

  /**
   * 任务应用详情
   * https://nei.netease.com/interface/detail/res/?pid=50068&id=266925
   *
   * @param tasks   taskId列表，","分隔
   * @param product 产品账号
   * @return 返回结果的json
   */
  public String fetchTasksJobRelation(String url, String sessionId, List<String> tasks, String product, String searchKey, int pageNum, int pageSize) {
    String path = "/ndi?ajax=fetchTasksJobRelation";
    Map<String, Object> params = Maps.newHashMap();
    params.put("tasks", tasks);
    params.put("product", product);
    params.put("offset", (pageNum - 1) * pageSize);
    params.put("limit", pageSize);
    if (StringUtils.isNotBlank(searchKey)) {
      params.put("jobName", searchKey);
    }
    String response = null;
    try {
      response = doPost(url, path, sessionId, params);
    } catch (HttpHostConnectException e) {
      throw new AzkabanRequestException("连接Azkaban服务器失败", e);
    }
    return response;
  }

  /**
   * GET 方法辅助类
   *
   * @param path   请求路径
   * @param params 请求参数
   * @return
   */
  public String doGet(String url, String path, String sessionId, Map<String, Object> params) throws HttpHostConnectException {
    StringBuilder urlBuilder = new StringBuilder("https://").append(url).append(path).append("?");

    for (String key : params.keySet()) {
      if (params.get(key) == null) {
        urlBuilder.append("&").append("=").append(params.get(key));
      } else {
        try {
          urlBuilder.append("&").append(key).append("=").append(URLEncoder.encode(params.get(key).toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
        }
      }
    }

    Map<String, String> headers = Maps.newHashMap();
    String cookieKey = "azkaban.browser.session.id=";
    headers.put(HttpHeaders.COOKIE, cookieKey + sessionId);
    try {
      long startTime = System.currentTimeMillis();
      log.info("###[Azkaban Request] url: {}", urlBuilder.toString());
      String response = HttpClientUtil.getMethod(urlBuilder.toString(), headers);
      log.info("###[Azkaban Response] cost:{}, content: {}", System.currentTimeMillis() - startTime, response);
      return response;
    } catch (IOException e) {
      if (e instanceof HttpHostConnectException) {
        throw new HttpHostConnectException(e, null);
      } else {
        log.error("Failed to request: {}", urlBuilder.toString(), e);
      }
    }
    return null;
  }

  /**
   * 通过url传参的POST方法
   * HTTPS请求
   *
   * @param url    服务器地址，host:port
   * @param path   请求路径
   * @param params 参数对
   * @return 返回结果，json
   * @throws HttpHostConnectException 连接异常
   */
  public String doPost(String url, String path, String sessionId,  Map<String, Object> params) throws HttpHostConnectException {
    StringBuilder urlBuilder = new StringBuilder("https://").append(url).append("/")
        .append(path);
    try {
      String content = JSONObject.toJSONString(params);
      Map<String, String> headers = Maps.newHashMap();
      String cookieKey = "azkaban.browser.session.id=";
      headers.put(HttpHeaders.COOKIE, cookieKey + sessionId);
      long startTime = System.currentTimeMillis();
      log.info("###[Azkaban Request] url: {}, params: {}", urlBuilder.toString(), content);
      String response = HttpClientUtil.postMethod(urlBuilder.toString(), content, headers);
      log.info("###[Azkaban Response] cost:{}, content: {}", System.currentTimeMillis() - startTime, response);
      if (response != null) {
        return response;
      }
    } catch (IOException e) {
      if (e instanceof HttpHostConnectException) {
        throw new HttpHostConnectException(e, null);
      } else {
        log.error("Failed to request: {}, params: {}", urlBuilder.toString(), JSONObject.toJSONString(params), e);
      }
    }
    return null;
  }

  /**
   * 通过url传参的POST方法
   * HTTPS请求
   *
   * @param url    服务器地址，host:port
   * @param path   请求路径
   * @param params 参数对
   * @return 返回结果，json
   * @throws HttpHostConnectException 连接异常
   */
  public String doPostByPath(String url, String path, Map<String, Object> params) throws HttpHostConnectException {
    StringBuilder urlBuilder = new StringBuilder("https://").append(url).append("/")
        .append(path).append("?");
    try {
      for (String key : params.keySet()) {
        if (params.get(key) == null) {
          urlBuilder.append("&").append("=")
              .append(params.get(key));
        } else {
          urlBuilder.append("&").append(key).append("=")
              .append(URLEncoder.encode(params.get(key).toString(), StandardCharsets.UTF_8.name()));
        }
      }
    } catch (UnsupportedEncodingException e) {
      log.warn("EncodingException.", e);
    }

    try {
      long startTime = System.currentTimeMillis();
      log.info("###[Azkaban Request]: {}", urlBuilder.toString());
      String response = HttpClientUtil.postMethod(urlBuilder.toString());
      log.info("###[Azkaban Response] cost:{}, content: {}", System.currentTimeMillis() - startTime, response);
      if (response != null) {
        return response;
      }
    } catch (IOException e) {
      if (e instanceof HttpHostConnectException) {
        throw new HttpHostConnectException(e, null);
      } else {
        log.error("Failed to request: {}", urlBuilder.toString(), e);
      }
    }
    return null;
  }

  @Getter
  public static class URLManager {
    private int location;
    private int size;
    private boolean isUsed = true;
    private List<String> urlList;

    public URLManager(List<String> urlList) {
      this.urlList = urlList;
      this.location = 0;
      this.size = urlList.size();
    }

    private synchronized String getURL() {
      isUsed = true;
      return urlList.get(location);
    }

    private synchronized void next() {
      if (isUsed) {
        isUsed = false;
        location = (location + 1) % size;
      }
    }
  }

}
