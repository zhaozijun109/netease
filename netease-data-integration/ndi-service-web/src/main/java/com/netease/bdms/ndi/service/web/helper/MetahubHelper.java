package com.netease.bdms.ndi.service.web.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.netease.bdms.ndi.service.web.config.ContextConstant;
import com.netease.bdms.ndi.service.web.config.NdiContext;
import com.netease.bdms.ndi.service.web.exception.MetahubException;
import com.netease.bdms.ndi.service.web.exception.NdiException;
import com.netease.bdms.ndi.service.web.util.constant.CommonConstants;
import com.netease.bdms.ndi.service.web.util.constant.CommonConstants.MetahubServcie;
import com.netease.bdms.ndi.service.web.util.constant.CommonConstants.RedisKey;
import com.netease.bdms.ndi.service.web.util.DataSourceTypeEnum;
import com.netease.bdms.ndi.service.web.util.ParamUtil;
import com.netease.bdms.ndi.service.web.util.ProcessStatusEnum;
import com.netease.bdms.ndi.service.web.util.ProjectConfigUtil;
import com.netease.bdms.ndi.service.web.util.RedisUtil;
import com.netease.music.wizard.sdk.util.SignatureUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * @ClassName MetahubServiceImpl
 * @Description Helper返回请求原始的数据，Service返回所需的实际数据
 * @Author Min Zhao
 * @Version 1.0
 **/
@Service
public class MetahubHelper {

  private static final Logger log = LoggerFactory.getLogger(MetahubHelper.class);
  @Autowired
  private RestTemplate restTemplate;
  @Autowired
  private RedisUtil redisUtil;
  @Autowired
  private ProjectConfigUtil configUtil;

  private HttpHeaders getHttpHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String appid = configUtil.get(MetahubServcie.APPID);
    headers.add("appid", appid);
    long timestamp = System.currentTimeMillis();
    headers.add("timestamp", Long.toString(timestamp));
    String secret = configUtil.get(MetahubServcie.SECRET);
    String signature = SignatureUtil.sign(secret, timestamp);
    headers.add("signature", signature);
    return headers;
  }

  /**
   * 添加一个数据源
   * https://nei.netease.com/interface/detail/?pid=43160&id=234908
   *
   * @param
   * @return
   */
  public JSONObject addDataSource(Integer accountId, String name, String type, String env,
      JSONObject dataSourceInfo, String creator) {
    String path = "/metahub/api/v2/datasource/add";
    Map<String, Object> params = new HashMap<>();
    params.put("name", name);
    params.put("type", type);
    params.put("env", env);
    params.put("info", dataSourceInfo);
    params.put("accountId", accountId);
    params.put("creator", creator);
    params.put("modifier", creator);
    params.put("description", "desc");
    JSONObject responseEntity = doPostWithCode(path, params);
    Integer code = responseEntity.getInteger("code");
    if (code == 409) {
      String message = responseEntity.getString("message");
      log.warn("登记数据源异常", message);
      throw new NdiException(ProcessStatusEnum.DATA_SOURCE_EXIST.getCode(),
          ProcessStatusEnum.DATA_SOURCE_EXIST.getMessage());
    } else if (code == 600) {
      String message = responseEntity.getString("message");
      log.warn("登记数据源异常", message);
      throw new NdiException(ProcessStatusEnum.DATA_SOURCE_URL_ILLEGAL.getCode(),
          ProcessStatusEnum.DATA_SOURCE_URL_ILLEGAL.getMessage());
    }
    if (!CommonConstants.HTTP_STATUS_CODE.SUCCESS.equals(code)) {
      String message = responseEntity.getString("message");
      throw new MetahubException(ProcessStatusEnum.METAHUB_ERROR.getCode(),
          "MetaHub exception:" + message);
    }
    JSONObject data = responseEntity.getJSONObject("data");
    return data;
  }

  /**
   * 创建一个catalog
   * https://nei.netease.com/interface/detail/?pid=43160&id=234913
   *
   * @param param
   * @return
   */
  public JSONObject addCatalog(JSONObject param) {
    ParamUtil.validate(param);
    String path = "/metahub/api/v2/catalog/add";
    Map<String, Object> params = Maps.newHashMap();
    params.put("accountId", param.get("accountId"));
    params.put("name", param.get("name"));
    params.put("creator", param.get("creator"));
    params.put("modifier", param.get("modifier"));
    params.put("description", param.get("description"));
    params.put("type", param.get("type"));
    params.put("dataSourceList", param.get("dataSourceList"));
    String response = doPost(path, params);
    return JSONObject.parseObject(response);
  }

  /**
   * 获取catalog下数据库列表
   * https://nei.netease.com/interface/detail/?pid=43160&id=231574
   *
   * @param accountId
   * @param catalog
   * @param offset
   * @param limit
   * @param db
   * @param selectedDsId
   * @return
   */
  public JSONArray listDatabases(Integer accountId, String catalog, Integer offset, Integer limit,
      String db,
      Long selectedDsId) {
    String path = "/metahub/api/v2/db/list";
    Map<String, Object> param = Maps.newHashMap();
    param.put("accountId", accountId);
    param.put("catalog", catalog);
    param.put("offset", offset);
    param.put("limit", limit);
    if (StringUtils.isNotBlank(db)) {
      param.put("db", db);
    }
    if (selectedDsId != null) {
      param.put("selectedDsId", selectedDsId);
    }
    String response = doGet(path, param);
    return JSONArray.parseArray(response);
  }

  /**
   * 列出所有表名
   * https://nei.netease.com/interface/detail/?pid=43160&id=240308
   *
   * @param catalog
   * @param db
   * @param offset
   * @param limit
   * @param accountId
   * @param table
   * @param selectedDsId
   * @return
   */
  public JSONArray listTableNames(String catalog, String db, Integer offset, Integer limit,
      Integer accountId,
      String table, Long selectedDsId) {
    String path = "/metahub/api/v2/table/name/list";
    Map<String, Object> param = Maps.newHashMap();
    param.put("catalog", catalog);
    param.put("db", db);
    param.put("offset", offset);
    param.put("limit", limit);
    param.put("accountId", accountId);
    if (StringUtils.isNotBlank(table)) {
      param.put("table", table);
    }
    if (selectedDsId != null) {
      param.put("selectedDsId", selectedDsId);
    }
    String response = doGet(path, param);
    return JSONArray.parseArray(response);
  }

  /**
   * 获取单个表详情
   * https://nei.netease.com/interface/detail/?pid=43160&id=231579
   *
   * @param catalog
   * @param accountId
   * @param db
   * @param table
   * @return
   */
  public JSONObject getTable(String catalog, Integer accountId, String db, String table) {
    String path = "/metahub/api/v2/table/get";
    Map<String, Object> param = new HashMap<>();
    param.put("catalog", catalog);
    param.put("db", db);
    param.put("table", table);
    param.put("accountId", accountId);
    String response = doGet(path, param);
    return JSONObject.parseObject(response);
  }

  /**
   * 删除一个数据源
   * https://nei.netease.com/interface/detail/?pid=43160&id=234910
   *
   * @param accountId
   * @param dataSourceId
   * @param modifier
   * @return
   */
  public JSONObject deleteDataSource(Integer accountId, Long dataSourceId, String modifier) {
    String path = "/metahub/api/v2/datasource/delete";
    Map<String, Object> params = Maps.newHashMap();
    params.put("id", dataSourceId);
    params.put("accountId", accountId);
    params.put("modifier", modifier);

    String response = doPost(path, params);
    return JSONObject.parseObject(response);
  }

  /**
   * 更新一个数据源
   * https://nei.netease.com/interface/detail/?pid=43160&id=234909
   *
   * @param param
   * @return
   */
  public JSONObject modifyDataSource(JSONObject param) {
    ParamUtil.validate(param);
    String path = "/metahub/api/v2/datasource/update";
    Map<String, Object> params = Maps.newHashMap();
    params.put("id", param.get("id"));
    params.put("name", param.get("name"));
    params.put("type", param.get("type"));
    params.put("env", param.get("env"));
    params.put("info", param.get("info"));
    params.put("accountId", param.get("accountId"));
    params.put("modifier", param.get("modifier"));
    String response = doPost(path, params);
    return JSONObject.parseObject(response);
  }

  /**
   * https://nei.netease.com/interface/detail/res/?pid=43160&id=234911
   *
   * @param accountId
   * @param dataSourceId
   * @return
   */
  public JSONObject getDataSource(Integer accountId, Long dataSourceId) {
    String path = "/metahub/api/v2/datasource/get";
    Map<String, Object> params = Maps.newHashMap();
    params.put("id", dataSourceId);
    params.put("accountId", accountId);
    String response = doGet(path, params);
    return JSONObject.parseObject(response);
  }

  /**
   * 用户获取Hive数据源和数据源的catalog
   * https://nei.netease.com/interface/detail/?pid=43160&id=249571
   *
   * @param
   * @return
   */
  public JSONObject listCatalogWithDataSources(Integer accountId, Integer offset, Integer limit,
      String type, String clusterId) {
    String path = "/metahub/api/v2/catalog/getwithds/list";
    Map<String, Object> param = Maps.newHashMap();
    param.put("accountId", accountId);
    if (offset != null) {
      param.put("offset", offset);
    }
    if (limit != null) {
      param.put("limit", limit);
    }
    if (StringUtils.isNotBlank(type)) {
      param.put("type", type);
    }
    if (StringUtils.equalsIgnoreCase(type, DataSourceTypeEnum.HIVE.name())) {
      param.put("clusterId", clusterId);
    }

    String response = doGet(path, param);
    return JSONObject.parseObject(response);
  }

  /**
   * 获得传入项目账号下有效的数据源列表(accountId)
   * https://nei.netease.com/interface/detail/res/?pid=47157&id=259540
   *
   * @param accountId
   * @param sortBy
   * @param order
   * @param offset
   * @param limit
   * @param name
   * @param type
   * @return
   */
  public String listAccountDatasource(Integer accountId, String sortBy, String order,
      Integer offset, Integer limit,
      String name, String type) {
    String path = "/metahub/api/v2/datasource/account/list";
    Map<String, Object> params = new HashMap<>();
    params.put("accountId", accountId);
    params.put("sortBy", sortBy);
    params.put("offset", offset);
    params.put("limit", limit);
    params.put("order", order);
    if (StringUtils.isNotBlank(name)) {
      params.put("name", name);
    }
    if (StringUtils.isNotBlank(type)) {
      params.put("type", type);
    }
    String result = doGet(path, params);
    return result;
  }

//  public String getCatalogName(Long dataSourceId, String type) {
//    ParamUtil.nonNull(dataSourceId, "DataSourceId can't be null");
//    Integer accountId = NdiContext.get(ContextConstant.PRODUCT_ID);
//    String clusterId = null;
//    if (StringUtils.equalsIgnoreCase(type, DataSourceTypeEnum.Hive.name())) {
//      clusterId = NdiContext.get(ContextConstant.CLUSTER_ID);
//    }
//    JSONObject responseData = listCatalogWithDataSources(accountId, 0, Integer.MAX_VALUE, type,
//        clusterId);
//    JSONArray list = responseData.getJSONArray("list");
//    if (list != null && list.size() > 0) {
//      for (int i = 0; i < list.size(); i++) {
//        JSONObject item = list.getJSONObject(i);
//        String catalog = item.getString("catalog");
//        JSONArray dataSourceDetails = item.getJSONArray("dataSourceDetail");
//        if (dataSourceDetails != null && dataSourceDetails.size() > 0) {
//          for (int j = 0; j < dataSourceDetails.size(); j++) {
//            JSONObject dataSourceDetail = dataSourceDetails.getJSONObject(j);
//            if (dataSourceId.equals(dataSourceDetail.getLong("id"))) {
//              return catalog;
//            }
//          }
//        }
//      }
//    }
//    throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(),
//        "DatasourceId don't have catalog, the dataSource don't exist");
//  }

  public String getCatalogFromCache(Integer accountId, Long dataSourceId) {
    String catalog = redisUtil.hget(
        redisUtil.keyBuilder(RedisKey.CATALOG_CACHE_KEY, String.valueOf(accountId)),
        String.valueOf(dataSourceId));
    if (!StringUtils.isBlank(catalog)) {
      return catalog;
    }
    cacheCatalog(accountId);
    String cacheCatalog = redisUtil.hget(
        redisUtil.keyBuilder(RedisKey.CATALOG_CACHE_KEY, String.valueOf(accountId)),
        String.valueOf(dataSourceId));
    if (StringUtils.isBlank(cacheCatalog)) {
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "数据源catalog不存在");
    }
    return cacheCatalog;
  }

  public String getCatalogFromCache(Long dataSourceId) {
    Integer accountId = NdiContext.get(ContextConstant.PRODUCT_ID);
    return getCatalogFromCache(accountId, dataSourceId);
  }

  /**
   * 缓存项目账号下数据源的catalog 不包括hive TODO: 一个项目账号下会不会有很多数据源？
   *
   * @param accountId
   */
  private void cacheCatalog(Integer accountId) {
    JSONObject responseData = listCatalogWithDataSources(accountId, 0, Integer.MAX_VALUE,
        null, null);
    JSONArray list = responseData.getJSONArray("list");
    if (list != null && list.size() > 0) {
      for (int i = 0; i < list.size(); i++) {
        JSONObject item = list.getJSONObject(i);
        String catalog = item.getString("catalog");
        JSONArray dataSourceDetails = item.getJSONArray("dataSourceDetail");
        if (dataSourceDetails != null && dataSourceDetails.size() > 0) {
          for (int j = 0; j < dataSourceDetails.size(); j++) {
            JSONObject dataSourceDetail = dataSourceDetails.getJSONObject(j);
            Long dataSourceId = dataSourceDetail.getLong("id");
            cacheCatalog(accountId, dataSourceId, catalog);
          }
        }
      }
    }
  }

  /**
   * 缓存catalog
   *
   * @param accountId    项目账号
   * @param dataSourceId 数据源id
   * @param catalogName  数据源catalog
   */
  private void cacheCatalog(Integer accountId, Long dataSourceId, String catalogName) {
    redisUtil.hsetWithExpire(redisUtil.keyBuilder(RedisKey.CATALOG_CACHE_KEY, String.valueOf(accountId)),
        String.valueOf(dataSourceId), catalogName, CommonConstants.RedisExpire.EXPIRE_1_WEEK);
  }

  /**
   * 数据源联通性检查
   * https://nei.netease.com/interface/detail/?pid=47157&id=253053
   *
   * @param catalogName 数据源的catalog
   * @param accountId   产品账号
   * @return
   */
  public String dataSourceConnCheck(String catalogName, Integer accountId) {
    String path = "/metahub/api/datasource/con/check";
    Map<String, Object> params = Maps.newHashMap();
    params.put("accountId", accountId);
    params.put("catalog", catalogName);
    String response = doGet(path, params);
    return response;
  }

  /**
   * 批量获取连通性检查状态
   * https://nei.netease.com/interface/detail/res/?pid=47157&id=261002
   *
   * @param accountId
   * @param ids
   * @return
   */
  public JSONArray batchConnectionStatus(Integer accountId, List<Long> ids) {
    String path = "/metahub/api/datasource/con/status/batch";
    Map<String, Object> params = Maps.newHashMap();
    params.put("accountId", accountId);
    params.put("ids", ids);
    String response = doPost(path, params);
    return JSONArray.parseArray(response);
  }

  /**
   * 批量获取连通性检查结果
   * https://nei.netease.com/interface/detail/req/?pid=47157&id=261003
   *
   * @param accountId
   * @param ids
   * @return
   */
  public JSONArray batchConnectionResult(Integer accountId, List<Long> ids) {
    String path = "/metahub/api/datasource/con/result/batch";
    Map<String, Object> params = Maps.newHashMap();
    params.put("accountId", accountId);
    params.put("ids", ids);
    String response = doPost(path, params);
    return JSONArray.parseArray(response);
  }

  /**
   * 通过正则表达式获取表
   * https://nei.netease.com/interface/detail/req/?pid=47157&id=267549
   *
   * @return
   */
  public JSONArray listTableByRegex() {
    return null;
  }

  /**
   * 获取数据源的catalog
   * https://nei.netease.com/interface/detail/req/?pid=47157&id=265522
   *
   * @param dataSourceId
   * @param accountId
   * @return
   */
  public String getCatalog(Long dataSourceId, Integer accountId) {
    String path = "/metahub/api/v2/datasource/getcatalog";
    Map<String, Object> params = Maps.newHashMap();
    params.put("accountId", accountId);
    params.put("id", dataSourceId);
    String response = doPost(path, params);
    JSONObject jsonObject = JSONObject.parseObject(response);
    if (jsonObject == null) {
      return null;
    }
    if (!jsonObject.containsKey("name")) {
      return null;
    }
    JSONObject name = jsonObject.getJSONObject("name");
    if (name == null || !name.containsKey("catalogName")) {
      return null;
    }
    String catalog = name.getString("catalogName");
    return catalog;
  }

  /**
   * GET 方法辅助类
   *
   * @param path
   * @param params
   * @return
   */
  private String doGet(String path, Map<String, Object> params) {
    StringBuilder urlBuilder = new StringBuilder(configUtil.get(MetahubServcie.ADDRESS))
        .append(path).append("?");
    for (String key : params.keySet()) {
      if (params.get(key) == null) {
        urlBuilder.append("&").append("=").append(params.get(key));
      } else {
        urlBuilder.append("&").append(key).append("=")
            .append(params.get(key));
      }
    }
    log.info("MetaHub request: url: {}, params: {}", urlBuilder.toString(), JSONObject.toJSONString(params));
    ResponseEntity<JSONObject> responseEntity = null;
    HttpEntity httpEntity = new HttpEntity(null, getHttpHeaders());
    log.info("Request url: {}, params: {}", urlBuilder.toString(), JSONObject.toJSONString(params));
    try {
      responseEntity = restTemplate
          .exchange(urlBuilder.toString(), HttpMethod.GET, httpEntity, JSONObject.class);
    } catch (RestClientException e) {
      log.error(ProcessStatusEnum.HTTP_INVOKE_ERROR.getMessage(), e);
      throw new NdiException(ProcessStatusEnum.HTTP_INVOKE_ERROR.getCode(),
          "Failed to request url:" + path);
    }
    JSONObject responseBody = responseEntity.getBody();
    Integer code = responseBody.getInteger("code");
    if (!CommonConstants.HTTP_STATUS_CODE.SUCCESS.equals(code)) {
      log.error(responseBody.getString("message"));
      throw new MetahubException(code, "MetaHub exception: " + responseBody.getString("message"));
    }

    return JSONObject.toJSONString(responseBody.get("data"));
  }

  /**
   * POST 方法辅助类
   *
   * @param path
   * @param params
   * @return
   */
  private String doPost(String path, Map<String, Object> params) {
    StringBuilder urlBuilder = new StringBuilder(configUtil.get(MetahubServcie.ADDRESS))
        .append(path);
    HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(params, getHttpHeaders());
    ResponseEntity<JSONObject> responseEntity = null;
    try {
      log.info("MetaHub request url: {}, params: {}", path, params);
      responseEntity = restTemplate
          .postForEntity(urlBuilder.toString(), httpEntity, JSONObject.class);
    } catch (RestClientException e) {
      log.error(ProcessStatusEnum.HTTP_INVOKE_ERROR.getMessage(), e);
      throw new NdiException(ProcessStatusEnum.HTTP_INVOKE_ERROR.getCode(),
          "Failed to request url:" + path);
    }
    JSONObject responseEntityBody = responseEntity.getBody();
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
  private JSONObject doPostWithCode(String path, Map<String, Object> params) {
    StringBuilder urlBuilder = new StringBuilder(configUtil.get(MetahubServcie.ADDRESS))
        .append(path);
    HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(params, getHttpHeaders());
    ResponseEntity<JSONObject> responseEntity = null;
    try {
      log.info("Request url: {}, params: {}", path, params);
      responseEntity = restTemplate
          .postForEntity(urlBuilder.toString(), httpEntity, JSONObject.class);
    } catch (RestClientException e) {
      log.error(ProcessStatusEnum.HTTP_INVOKE_ERROR.getMessage(), e);
      throw new NdiException(ProcessStatusEnum.HTTP_INVOKE_ERROR.getCode(),
          "Failed to request url:" + path);
    }
    return responseEntity.getBody();
  }
}
