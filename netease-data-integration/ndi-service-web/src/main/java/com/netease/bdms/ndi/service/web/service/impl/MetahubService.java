package com.netease.bdms.ndi.service.web.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netease.bdms.ndi.service.web.config.ContextConstant;
import com.netease.bdms.ndi.service.web.config.NdiContext;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityResultDtoV2;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityResultRspDto.*;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityStatusDtoV2;
import com.netease.bdms.ndi.service.web.dto.datasource.DataSourceConResult;
import com.netease.bdms.ndi.service.web.exception.DataSourceException;
import com.netease.bdms.ndi.service.web.exception.MetahubException;
import com.netease.bdms.ndi.service.web.exception.NdiException;
import com.netease.bdms.ndi.service.web.util.*;
import com.netease.bdms.ndi.service.web.util.constant.CommonConstants;
import com.netease.bdms.ndi.service.web.util.constant.CommonConstants.MetahubServcie;
import com.netease.bdms.ndi.service.web.util.constant.CommonConstants.RedisKey;
import com.netease.bdms.ndi.service.web.util.constant.ResponseCodeConstant;
import com.netease.music.wizard.sdk.util.SignatureUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.netease.bdms.ndi.service.web.util.DataSourceConstant.ConnectivityResultEnum;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MetahubServiceImpl
 * @Description 元数据中心服务
 * @Author Min Zhao
 * @Version 1.0
 **/
@Service
public class MetahubService {

  private static final Logger log = LoggerFactory.getLogger(MetahubService.class);
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
   *
   *
   * @param accountId
   * @param checkIdList
   * @return
   */
  private List<ConnectivityResultDtoV2> listConnectivityResult(Integer accountId, List<Long> checkIdList) {
    JSONArray jsonArray = batchConnectionResult(accountId, checkIdList);
    List<ConnectivityResultDtoV2> list = Lists.newArrayList();
    if (jsonArray == null || jsonArray.size() == 0) {
      return list;
    }

    for (int i = 0; i < jsonArray.size(); i++) {
      JSONObject jsonObject = jsonArray.getJSONObject(i);
      Integer status = jsonObject.getInteger("status");
      ConnectivityResultDtoV2 resultDto = new ConnectivityResultDtoV2();
      JSONArray dataSourceConnList = jsonObject.getJSONArray("dataSourceConResultList");
      if (CollectionUtils.isNotEmpty(dataSourceConnList)) {
        List<DataSourceConResult> dataSourceConResultList = JSONArray.parseArray(dataSourceConnList.toJSONString(), DataSourceConResult.class);
        resultDto.setStatus(status);
        resultDto.setDataSourceConResultList(dataSourceConResultList);
        resultDto.setCheckId(dataSourceConResultList.get(0).getCheckerId());
      }

      list.add(resultDto);
    }
    return list;
  }

  private MetahubConnectivityResult handleMetaHubConnectivityResult(ConnectivityResultDtoV2 dtoV2) {
    MetahubConnectivityResult metaHubConnectivityResult = new MetahubConnectivityResult();
    List<DataSourceConResult> dataSourceConResultList = dtoV2.getDataSourceConResultList();
    if (CollectionUtils.isEmpty(dataSourceConResultList)) {
      return metaHubConnectivityResult;
    }
    String updateTime = "";
    String status = ConnectivityResultEnum.nameOfType(dtoV2.getStatus());
    Integer failedNum = 0;
    Integer totalNum = 0;
    List<ConnectivityResultDetail> connectivityResultDetailList = Lists.newArrayList();
    for (DataSourceConResult dataSourceConResult : dataSourceConResultList) {
      updateTime = dataSourceConResult.getCreateTime();
      totalNum += 1;
      if (!dataSourceConResult.getCheckConnInfo().getResult()) {
        failedNum += 1;
      }
      ConnectivityResultDetail connectivityResultDetail = new ConnectivityResultDetail();
      connectivityResultDetail.setHost(dataSourceConResult.getIp());
      connectivityResultDetail.setMessage(dataSourceConResult.getCheckConnInfo().getExceptionMessage());
      connectivityResultDetailList.add(connectivityResultDetail);
    }
    metaHubConnectivityResult.setDetails(connectivityResultDetailList)
        .setFailedNum(failedNum)
        .setTotalNum(totalNum)
        .setStatus(status)
        .setUpdateTime(updateTime);

    return metaHubConnectivityResult;
  }

  MetahubConnectivityResult getMetaHubConnectivityResult(Long checkId, Integer productId) {
    List<ConnectivityResultDtoV2> connectivityResultDtoV2List = listConnectivityResult(productId, Lists.newArrayList(checkId));
    return handleMetaHubConnectivityResult(connectivityResultDtoV2List.get(0));
  }

  private List<ConnectivityStatusDtoV2> listMetaHubConnectivityStatusIntern(List<Long> checkIdList, Integer productId) {
    JSONArray jsonArray = batchConnectionStatus(productId, checkIdList);
    List<ConnectivityStatusDtoV2> list = Lists.newArrayList();
    if (jsonArray == null || jsonArray.size() == 0) {
      return list;
    }

    for (int i = 0; i < jsonArray.size(); i++) {
      JSONObject jsonObject = jsonArray.getJSONObject(i);
      Integer execStatus = jsonObject.getInteger("execStatus");
      if (execStatus == 0) {
        execStatus = ConnectivityResultEnum.CHECKING.getType();
      } else if (execStatus == 1){
        execStatus = ConnectivityResultEnum.FINISHED.getType();
      } else {
        execStatus = ConnectivityResultEnum.FAILED.getType();
      }
      Long dataSourceId = jsonObject.getLong("datasourecId");
      Long checkId = jsonObject.getLong("checkerId");
      ConnectivityStatusDtoV2 connectivityStatusDto = new ConnectivityStatusDtoV2(dataSourceId, checkId, execStatus);
      list.add(connectivityStatusDto);
    }
    return list;
  }

  /**
   * 正在检测中：正在检测中
   * 完成：查结果
   * 其他：失败
   *
   * @param checkIdList 非空的checkId列表
   * @return 参数为空时，返回空的Map
   */
  public Map<Long, ConnectivityResultEnum> listMetaHubConnectivityStatus(List<Long> checkIdList, Integer productId) {
    if (CollectionUtils.isEmpty(checkIdList)) {
      throw new IllegalArgumentException("CheckIdList不能为空");
    }
    Map<Long, ConnectivityResultEnum> metaHubConnectivityStatusMap = Maps.newHashMap();
    List<ConnectivityStatusDtoV2> connectivityStatusDtoV2List = listMetaHubConnectivityStatusIntern(checkIdList, productId);
    if (CollectionUtils.isEmpty(connectivityStatusDtoV2List)) {
      for (Long checkId : checkIdList) {
        metaHubConnectivityStatusMap.put(checkId, ConnectivityResultEnum.FAILED);
      }
      return metaHubConnectivityStatusMap;
    }

    List<Long> finishedCheckIdList = Lists.newArrayList();
    for (ConnectivityStatusDtoV2 connectivityStatusDtoV2 : connectivityStatusDtoV2List) {
      Long checkId = connectivityStatusDtoV2.getCheckId();
      Integer status = connectivityStatusDtoV2.getStatus();
      if (ConnectivityResultEnum.CHECKING.equalWith(status)) {
        metaHubConnectivityStatusMap.put(checkId, ConnectivityResultEnum.CHECKING);
      } else if (ConnectivityResultEnum.FINISHED.equalWith(status)) {
        finishedCheckIdList.add(checkId);
      } else {
        metaHubConnectivityStatusMap.put(checkId, ConnectivityResultEnum.FAILED);
      }
    }

    if (CollectionUtils.isEmpty(finishedCheckIdList)) {
      return metaHubConnectivityStatusMap;
    }

    List<ConnectivityResultDtoV2> connectivityResultDtoV2List = listConnectivityResult(productId, finishedCheckIdList);
    for (int i = 0; i < connectivityResultDtoV2List.size(); i++) {
      ConnectivityResultDtoV2 connectivityResultDtoV2 = connectivityResultDtoV2List.get(i);
      Integer status = connectivityResultDtoV2.getStatus();
      metaHubConnectivityStatusMap.put(connectivityResultDtoV2.getCheckId(), ConnectivityResultEnum.valueOfType(status));
    }

    return metaHubConnectivityStatusMap;
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
    JSONObject responseEntity = doPostWithBody(path, params);
    Integer code = responseEntity.getInteger("code");
    if (code == 409) {
      String message = responseEntity.getString("message");
      log.warn("登记数据源异常", message);
      throw new DataSourceException(ResponseCodeConstant.DATA_SOURCE_NAME_EXIST,
          "数据源已存在");
    } else if (code == 600) {
      String message = responseEntity.getString("message");
      log.warn("登记数据源异常", message);
      throw new DataSourceException(ResponseCodeConstant.DATA_SOURCE_URL_ILLEGAL,
          "数据源url非法");
    } else if (code == 602) {
      throw new DataSourceException(ResponseCodeConstant.DATA_SOURCE_NAME_EXIST, "数据源名字已存在");
    } else if (code == 603) {
      String message = responseEntity.getString("message");
      throw new DataSourceException(ResponseCodeConstant.DATA_SOURCE_EXIST, message);
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
   * 得到数据源详情
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
    JSONObject responseBody = doGetWithBody(path, params);

    Integer code = responseBody.getInteger("code");
    if (code == 404) {
      return null;
    }
    if (!CommonConstants.HTTP_STATUS_CODE.SUCCESS.equals(code)) {
      log.error(responseBody.getString("message"));
      throw new MetahubException(code, "MetaHub exception: " + responseBody.getString("message"));
    }
    return responseBody.getJSONObject("data");
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

  public String getCatalogFromCache(Integer accountId, Long dataSourceId) {
    String catalog = redisUtil.hget(
        redisUtil.keyBuilder(CommonConstants.RedisKey.CATALOG_CACHE_KEY, String.valueOf(accountId)),
        String.valueOf(dataSourceId));
    if (!StringUtils.isBlank(catalog)) {
      return catalog;
    }
    String catalogName = getCatalog(dataSourceId, accountId);
    if (StringUtils.isBlank(catalogName)) {
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "数据源catalog不存在");
    }
    cacheCatalog(accountId, dataSourceId, catalogName);
    return catalogName;
  }

  public String getCatalogFromCache(Long dataSourceId) {
    Integer accountId = NdiContext.get(ContextConstant.PRODUCT_ID);
    return getCatalogFromCache(accountId, dataSourceId);
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
  public JSONArray listTableByRegex(String catalog, String db, Integer offset, Integer limit, String regexp) {
    String path = "/metahub/api/v2/table/list/regexp";
    Map<String, Object> params = Maps.newHashMap();
    params.put("catalog", catalog);
    params.put("db", db);
    params.put("offset", offset);
    params.put("limit", limit);
    params.put("regexp", regexp);
    JSONObject responseBody = doGetWithBody(path, params);
    Integer code = responseBody.getInteger("code");
    if (code == 200) {
      JSONArray responseData = responseBody.getJSONArray("data");
      return responseData;
    } else {
      return null;
    }
  }

  /**
   * 通过正则表达式获取表名
   *
   * @param catalog
   * @param db
   * @param offset
   * @param limit
   * @param regexp
   * @return
   */
  public List<String> listTableNameByRegex(String catalog, String db, Integer offset, Integer limit, String regexp) {
    JSONArray responseData = listTableByRegex(catalog, db, offset, limit,regexp);
    if (CollectionUtils.isEmpty(responseData)) {
      return Lists.newArrayList();
    }
    List<String> tableNameList = Lists.newArrayList();

    for (int i =0; i< responseData.size();i++) {
      JSONObject jsonObject = responseData.getJSONObject(i);
      String tableName = jsonObject.getString("table");
      tableNameList.add(tableName);
    }

    return tableNameList;
  }

  /**
   * 获取数据源的catalog
   * https://nei.netease.com/interface/detail/req/?pid=47157&id=265522
   *
   * @param dataSourceId
   * @param accountId
   * @return code = 404， 不存在
   */
  public String getCatalog(Long dataSourceId, Integer accountId) {
    String path = "/metahub/api/v2/datasource/getcatalog";
    Map<String, Object> params = Maps.newHashMap();
    params.put("accountId", accountId);
    params.put("id", dataSourceId);
    JSONObject responseBody = doPostWithBody(path, params);
    Integer code = responseBody.getInteger("code");
    if (code == 404) {
      return null;
    }
    JSONObject responseData = responseBody.getJSONObject("data");
    if (responseData == null || !responseData.containsKey("name")) {
      return null;
    }
    JSONObject name = responseData.getJSONObject("name");
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
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(configUtil.get(MetahubServcie.ADDRESS)).path(path);

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
    HttpEntity httpEntity = new HttpEntity(null, getHttpHeaders());
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
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(configUtil.get(MetahubServcie.ADDRESS)).path(path);
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
    HttpEntity httpEntity = new HttpEntity(null, getHttpHeaders());
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
    StringBuilder urlBuilder = new StringBuilder(configUtil.get(MetahubServcie.ADDRESS))
        .append(path);
    HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(params, getHttpHeaders());
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
    StringBuilder urlBuilder = new StringBuilder(configUtil.get(MetahubServcie.ADDRESS))
        .append(path);
    HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(params, getHttpHeaders());
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
}
