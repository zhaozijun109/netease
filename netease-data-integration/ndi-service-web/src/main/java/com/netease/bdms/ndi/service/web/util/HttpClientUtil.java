package com.netease.bdms.ndi.service.web.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netease.bdms.ndi.service.web.exception.ServerException;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

/**
 * HTTP工具类
 *
 * @author ginger
 * @create 2019-09-20 21:07
 */
@Slf4j
public class HttpClientUtil {

  /**
   * 获取客户端IP
   *
   * @param request 本次请求
   * @return 客户端IP
   */
  public static String getClientIP(HttpServletRequest request) {
    String ip = request.getHeader("x-forwarded-for");
    if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("HTTP_CLIENT_IP");
    }
    if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("HTTP_X_FORWARDED_FOR");
    }
    if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    String realIP = Lists.newArrayList(Splitter.on(',').omitEmptyStrings().trimResults().split(ip))
        .get(0);
    return realIP.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : realIP;
  }

  /**
   * 根据Cookie名获取Cookie
   *
   * @param request 本次请求
   * @param name Cookie名
   * @return Cookie
   */
  public static Cookie getCookieByName(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();
    if (null == cookies) {
      return null;
    }

    for (Cookie cookie : cookies) {
      if (StringUtils.equalsIgnoreCase(name, cookie.getName())) {
        return cookie;
      }
    }

    return null;
  }

  /**
   * 构建查询参数
   *
   * @param map 参数列表
   * @return 查询参数
   */
  public static String buildQueryString(Map<String, String> map) {
    if (MapUtils.isEmpty(map)) {
      return "";
    }

    List<String> list = map.keySet().stream()
        .map(key -> encodeStr(key) + "=" + encodeStr(map.get(key)))
        .collect(Collectors.toList());
    return Joiner.on("&").join(list);
  }

  /**
   * 对字符串进行UTF-8编码
   *
   * @param str 要编码的字符串
   * @return 编码后的字符串
   */
  public static String encodeStr(String str) {
    Preconditions.checkArgument(StringUtils.isNotBlank(str));

    try {
      return URLEncoder.encode(str, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      log.error("encode url error, str={}", str, e);
      throw new ServerException("unable to encode " + str);
    }
  }

  /**
   * 对字符串进行UTF-8解码
   *
   * @param str 要解码的字符串
   * @return 解码后的字符串
   */
  public static String decodeStr(String str) {
    Preconditions.checkArgument(StringUtils.isNotBlank(str));

    try {
      return URLDecoder.decode(str, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      log.error("encode url error, str={}", str, e);
      throw new ServerException("unable to encode " + str);
    }
  }

  /**
   * 将查询参数转换为Map
   *
   * 注意：对于数组参数只会记录最后一个
   *
   * @param queryString 原始查询字符串
   * @param decode 是否对查询参数译码
   * @return 转换后的Map
   */
  public static Map<String, String> transformQueryStringToMap(String queryString, boolean decode) {
    if (StringUtils.isBlank(queryString)) {
      return Maps.newHashMap();
    }

    Map<String, String> map = Maps.newHashMap();
    try {
      String[] strs = queryString.split("&");
      for (String str : strs) {
        if (decode) {
          str = URLDecoder.decode(str, "UTF-8");
        }
        if (!str.contains("=")) {
          map.put(str, null);
        } else {
          String[] split = str.split("=", 2);
          map.put(split[0], split[1]);
        }
      }
    } catch (Exception e) {
      log.error("can not transform query string to map", e);
      throw new ServerException("can not transform query string to map");
    }

    return map;
  }

  /**
   * 删除请求
   *
   * @param url 请求地址
   * @return 响应信息
   * @throws IOException io异常
   */
  public static String deleteRequest(String url) throws IOException {
    HttpClient client = HttpPoolManager.getHttpClient();
    HttpDelete httpDelete = new HttpDelete(url);
    HttpResponse httpResponse = client.execute(httpDelete);

    int httpCode = httpResponse.getStatusLine().getStatusCode();
    if (httpCode == HttpURLConnection.HTTP_OK || httpCode == HttpURLConnection.HTTP_CREATED) {
      HttpEntity entity = httpResponse.getEntity();
      return EntityUtils.toString(entity, "utf-8");
    } else {
      log.warn("req url failed, url: {}, retcode: {}, content: {}", url, httpCode,
          EntityUtils.toString(httpResponse.getEntity(), "utf-8"));
    }
    return null;
  }

  /**
   * post请求
   *
   * @param url 请求地址
   * @param content 请求内容
   * @return 响应信息
   * @throws IOException io异常
   */
  public static String postMethod(String url, String content) throws IOException {
    HttpClient client = HttpPoolManager.getHttpClient();
    HttpPost post = new HttpPost(url);
    if (null != content) {
      StringEntity se = new StringEntity(content, "utf-8");
      se.setContentType("application/json");
      post.setEntity(se);
    }
    HttpResponse response = client.execute(post);
    StatusLine sl = response.getStatusLine();
    HttpEntity entity = response.getEntity();
    String contentRep = EntityUtils.toString(entity, "utf-8");
    if (sl.getStatusCode() == HttpStatus.SC_OK) {
      return contentRep;
    } else {
      log.warn("req url failed, url: {}, retcode: {}, content: {}", url, sl.getStatusCode(),
          contentRep);
    }
    return null;
  }

  /**
   * post请求
   *
   * @param url 请求地址
   * @return 响应信息
   * @throws IOException io异常
   */
  public static String postMethod(String url) throws IOException {
    HttpClient client = HttpPoolManager.getHttpClient();
    HttpPost post = new HttpPost(url);
    HttpResponse response = client.execute(post);
    StatusLine sl = response.getStatusLine();
    HttpEntity entity = response.getEntity();
    String contentRep = EntityUtils.toString(entity, "utf-8");
    if (sl.getStatusCode() == HttpStatus.SC_OK) {
      return contentRep;
    } else {
      log.warn("req url failed, url: {}, retcode: {}, content: {}", url, sl.getStatusCode(),
          contentRep);
    }
    return null;
  }

  /**
   * post请求
   *
   * @param url 请求地址
   * @param content 请求内容
   * @return 响应信息
   * @throws IOException io异常
   */
  public static String postMethod(String url, String content, Map<String, String> headers)
      throws IOException {
    HttpClient client = HttpPoolManager.getHttpClient();
    HttpPost post = new HttpPost(url);
    for (String key : headers.keySet()) {
      post.addHeader(key, headers.get(key));
    }
    if (null != content) {
      StringEntity se = new StringEntity(content, "utf-8");
      se.setContentType("application/json");
      post.setEntity(se);
    }
    HttpResponse response = client.execute(post);
    StatusLine sl = response.getStatusLine();
    HttpEntity entity = response.getEntity();
    String contentRep = EntityUtils.toString(entity, "utf-8");
    if (sl.getStatusCode() == HttpStatus.SC_OK) {
      return contentRep;
    } else {
      log.warn("req url failed, url: {}, retcode: {}, content: {}", url, sl.getStatusCode(),
          contentRep);
    }
    return null;
  }

  /**
   * post请求
   *
   * @param url 请求地址
   * @param content 请求内容
   * @return 响应信息
   * @throws IOException io异常
   */
  public static Map<String, Object> postMethodWithSc(String url, String content,
      Map<String, String> headers) throws IOException {
    HttpClient client = HttpPoolManager.getHttpClient();
    HttpPost post = new HttpPost(url);
    for (String key : headers.keySet()) {
      post.addHeader(key, headers.get(key));
    }
    if (null != content) {
      StringEntity se = new StringEntity(content, "utf-8");
      se.setContentType("application/json");
      post.setEntity(se);
    }
    HttpResponse response = client.execute(post);
    StatusLine sl = response.getStatusLine();
    HttpEntity entity = response.getEntity();
    String contentRep = EntityUtils.toString(entity, "utf-8");

    Map<String, Object> result = Maps.newHashMap();
    result.put("code", sl.getStatusCode());
    result.put("content", contentRep);
    if (sl.getStatusCode() != HttpStatus.SC_OK) {
      log.warn("req url failed, url: {}, retcode: {}, content: {}", url, sl.getStatusCode(), contentRep);
    }
    return result;
  }

  /**
   *
   */
  public static String postMethod(String url, List<NameValuePair> pairs, String mediaType)
      throws IOException {
    HttpClient httpClient = HttpPoolManager.getHttpClient();
    HttpPost post = new HttpPost(url);
    UrlEncodedFormEntity se = new UrlEncodedFormEntity(pairs, "UTF-8");
    se.setContentType(mediaType);
    post.setEntity(se);
    HttpResponse response = httpClient.execute(post, HttpClientContext.create());
    StatusLine sl = response.getStatusLine();
    HttpEntity entity = response.getEntity();
    String ret = EntityUtils.toString(entity, "UTF-8");
    if (HttpStatus.SC_OK == sl.getStatusCode()) {
      return ret;
    } else {
      log.warn("request url failed. url {}, return code {}, content {}", url, sl.getStatusCode(),
          ret);
    }
    return null;
  }


  /**
   * post请求，并指定ContentType
   *
   * @param url 请求地址
   * @param content 请求内容
   * @param contentType 内容类型
   * @return 响应内容
   * @throws IOException io异常
   */
  public static String postMethod(String url, String content, String contentType)
      throws IOException {
    HttpClient client = HttpPoolManager.getHttpClient();
    HttpPost post = new HttpPost(url);
    if (null != content) {
      StringEntity se = new StringEntity(content, "utf-8");
      se.setContentType(contentType);
      post.setEntity(se);
    }
    HttpResponse response = client.execute(post);
    StatusLine sl = response.getStatusLine();
    HttpEntity entity = response.getEntity();
    String contentRep = EntityUtils.toString(entity, "utf-8");
    if (sl.getStatusCode() == HttpStatus.SC_OK) {
      return contentRep;
    } else {
      log.warn("req url failed, url: {}, retcode: {}, content: {}", url, sl.getStatusCode(),
          contentRep);
    }
    return null;
  }


  /**
   * post请求，携带参数
   *
   * @param url 请求地址
   * @param nvps 携带的参数
   * @return 响应信息
   * @throws IOException io异常
   */
  public static String postMethod(String url, List<NameValuePair> nvps) throws IOException {
    HttpClient client = HttpPoolManager.getHttpClient();
    HttpPost post = new HttpPost(url);
    post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
    HttpResponse response = client.execute(post);
    StatusLine sl = response.getStatusLine();
    HttpEntity entity = response.getEntity();
    String content = EntityUtils.toString(entity, "utf-8");
    if (sl.getStatusCode() == HttpStatus.SC_OK) {
      return content;
    } else {
      log.warn("req url failed, url: {}, retcode: {}, content: {}", url, sl.getStatusCode(),
          content);
    }
    return null;
  }


  /**
   * post请求，携带参数和请求头
   *
   * @param url 请求地址
   * @param nvps 请求参数
   * @param headers 携带的请求头
   * @return 响应信息
   * @throws IOException io异常
   */
  public static String postMethod(String url, List<NameValuePair> nvps, Map<String, String> headers)
      throws IOException {
    HttpClient client = HttpPoolManager.getHttpClient();
    HttpPost post = new HttpPost(url);
    for (String key : headers.keySet()) {
      post.addHeader(key, headers.get(key));
    }
    post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
    HttpResponse response = client.execute(post);
    StatusLine sl = response.getStatusLine();
    HttpEntity entity = response.getEntity();
    String content = EntityUtils.toString(entity, "utf-8");
    if (sl.getStatusCode() == HttpStatus.SC_OK) {
      return content;
    } else {
      log.warn("req url failed, url: {}, retcode: {}, content: {}", url, sl.getStatusCode(),
          content);
    }
    return null;
  }

  /**
   * get请求
   *
   * @param url 请求地址
   * @return 响应信息
   * @throws IOException io异常
   */
  public static String getMethod(String url, Map<String, String> headers) throws IOException {
    HttpClient client = HttpPoolManager.getHttpClient();
    HttpGet get = new HttpGet(url);
    for (String key : headers.keySet()) {
      get.addHeader(key, headers.get(key));
    }
    HttpResponse response = client.execute(get);
    StatusLine sl = response.getStatusLine();
    HttpEntity entity = response.getEntity();
    String content = EntityUtils.toString(entity, "utf-8");
    if (sl.getStatusCode() == HttpStatus.SC_OK) {
      return content;
    } else {
      log.warn("req url failed, url: {}, retcode: {}, content: {}", url, sl.getStatusCode(),
          content);
    }
    return null;
  }

  /**
   * get请求
   *
   * @param url 请求地址
   * @return 响应信息
   * @throws IOException io异常
   */
  public static Map<String, Object> getMethodWithSc(String url, Map<String, String> headers) throws IOException {
    HttpClient client = HttpPoolManager.getHttpClient();
    HttpGet get = new HttpGet(url);
    for (String key : headers.keySet()) {
      get.addHeader(key, headers.get(key));
    }
    HttpResponse response = client.execute(get);
    StatusLine sl = response.getStatusLine();
    HttpEntity entity = response.getEntity();
    String content = EntityUtils.toString(entity, "utf-8");

    Map<String, Object> result = Maps.newHashMap();
    result.put("code", sl.getStatusCode());
    result.put("content", content);
    if (sl.getStatusCode() != HttpStatus.SC_OK) {
      log.warn("req url failed, url: {}, retcode: {}, content: {}", url, sl.getStatusCode(), content);
    }
    return result;
  }


  /**
   * get请求
   *
   * @param url 请求地址
   * @return 响应信息
   * @throws IOException io异常
   */
  public static String getMethod(String url) throws IOException {
    HttpClient client = HttpPoolManager.getHttpClient();
    HttpGet get = new HttpGet(url);
    HttpResponse response = client.execute(get);
    StatusLine sl = response.getStatusLine();
    HttpEntity entity = response.getEntity();
    String content = EntityUtils.toString(entity, "utf-8");
    if (sl.getStatusCode() == HttpStatus.SC_OK) {
      return content;
    } else {
      log.warn("req url failed, url: {}, retcode: {}, content: {}", url, sl.getStatusCode(),
          content);
    }
    return null;
  }


  /**
   * get请求
   *
   * @param url 请求地址
   * @return http实体
   * @throws IOException io异常
   */
  public static HttpEntity getMethodResponseEntity(String url) throws IOException {
    HttpClient client = HttpPoolManager.getHttpClient();
    HttpGet get = new HttpGet(url);
    HttpResponse response = client.execute(get);
    StatusLine sl = response.getStatusLine();
    HttpEntity entity = response.getEntity();
    if (sl.getStatusCode() == HttpStatus.SC_OK) {
      return entity;
    } else {
      EntityUtils.consume(entity);
      log.warn("req url failed, url: {}, retcode: {}, HttpEntity: {}", url, sl.getStatusCode(),
          entity);
    }
    return null;
  }

  /**
   * put请求
   *
   * @param url 请求地址
   * @param content 请求内容
   * @return 响应内容
   * @throws IOException io异常
   */
  public static String putMethod(String url, String content) throws IOException {
    HttpClient client = HttpPoolManager.getHttpClient();
    HttpPut put = new HttpPut(url);
    StringEntity se = new StringEntity(content, "utf-8");
    se.setContentType("application/json");
    put.setEntity(se);
    HttpResponse response = client.execute(put);
    StatusLine sl = response.getStatusLine();
    HttpEntity entity = response.getEntity();
    String contentRet = EntityUtils.toString(entity, "utf-8");
    if (sl.getStatusCode() == HttpStatus.SC_OK) {
      return contentRet;
    } else {
      log.warn("req url failed, url: {}, retcode: {}, content: {}", url, sl.getStatusCode(),
          contentRet);
    }
    return null;
  }

  /**
   * 以post方法进行文件下载
   *
   * @param url 请求地址
   * @return 响应的字节数组
   * @throws IOException io异常
   */
  public static byte[] downloadInPost(String url) throws IOException {
    HttpClient client = HttpPoolManager.getHttpClient();
    HttpPost post = new HttpPost(url);
    HttpResponse response = client.execute(post);
    HttpEntity entity = response.getEntity();
    return EntityUtils.toByteArray(entity);
  }


  /**
   * 以get方法进行文件下载
   *
   * @param url 请求地址
   * @return 响应的字节数组
   * @throws IOException io异常
   */
  public static byte[] downloadInGet(String url) throws IOException {
    HttpClient client = HttpPoolManager.getHttpClient();
    HttpGet httpGet = new HttpGet(url);
    HttpResponse response = client.execute(httpGet);
    HttpEntity entity = response.getEntity();
    return EntityUtils.toByteArray(entity);
  }


  /**
   * 以get方法进行文件下载，并写入输出流中
   *
   * @param url 请求地址
   * @param os 输出流
   * @throws IOException io异常
   */
  public static void downloadInGetToOutputStream(String url, OutputStream os) throws IOException {
    BufferedInputStream bis;
    try {
      HttpClient client = HttpPoolManager.getHttpClient();
      HttpGet httpGet = new HttpGet(url);
      HttpResponse responseRet = client.execute(httpGet);
      bis = new BufferedInputStream(responseRet.getEntity().getContent());
      byte[] buf = new byte[1024];
      int bytesRead;
      while ((bytesRead = bis.read(buf, 0, buf.length)) != -1) {
        os.write(buf, 0, bytesRead);
      }
      bis.close();
    } catch (Exception e) {
      log.warn("req url failed, url: {}, {}", url, e.getMessage(), e);
    }
  }


}
