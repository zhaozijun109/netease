package com.netease.bdms.ndi.service.web.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.netease.bdms.ndi.service.web.config.ContextConstant;
import com.netease.bdms.ndi.service.web.config.NdiContext;
import com.netease.bdms.ndi.service.web.dto.DataSourceQuoteDto;
import com.netease.bdms.ndi.service.web.dto.datasource.CheckAndDataSourceId;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityResultDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityResultDtoV2;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityStatusReqDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityStatusResDto;
import com.netease.bdms.ndi.service.web.dto.datasource.DataSourceConResult;
import com.netease.bdms.ndi.service.web.dto.datasource.DataSourceConnectivityReqDto;
import com.netease.bdms.ndi.service.web.dto.datasource.DataSourceSimpleDto;
import com.netease.bdms.ndi.service.web.dto.datasource.DataSourcesResDto;
import com.netease.bdms.ndi.service.web.dto.datasource.DeleteDataSourceReqDto;
import com.netease.bdms.ndi.service.web.exception.MetahubException;
import com.netease.bdms.ndi.service.web.exception.NdiException;
import com.netease.bdms.ndi.service.web.facade.TaskDataSourceFacade;
import com.netease.bdms.ndi.service.web.param.User;
import com.netease.bdms.ndi.service.web.service.ConnectivityService;
import com.netease.bdms.ndi.service.web.service.DataSourceService;
import com.netease.bdms.ndi.service.web.service.TaskDataSourceService;
import com.netease.bdms.ndi.service.web.service.UserService;
import com.netease.bdms.ndi.service.web.util.*;
import com.netease.bdms.ndi.service.web.util.DataSourceConstant.ConnectivityResultEnum;

import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ClassName DataSourceServiceImpl
 * @Description 数据源服务实现
 * @Author Min Zhao
 * @Version 1.0
 **/
@Service
public class DataSourceServiceImpl implements DataSourceService {

  private static final Logger log = LoggerFactory.getLogger(DataSourceService.class);
  private static final String DATA_SOURCE_ENV_READ = "read";
  private static final String DATA_SOURCE_HIVE = "hive";

  @Autowired
  private MetahubService metahubService;

  @Autowired
  private UserService userService;

  @Autowired
  private ConnectivityService connectivityService;

  @Override
  public JSONObject listDataSource(JSONObject requestParam) {
    Integer productId = NdiContext.get(ContextConstant.PRODUCT_ID);
    Preconditions.checkArgument(productId != null, "获取productId失败");

    String dataSourceName = requestParam.getString("searchKey");
    if (StringUtils.isNotBlank(dataSourceName)) {
      requestParam.put("name", dataSourceName);
    }

    String type = requestParam.getString("dataSourceType");
    String sortBy = requestParam.getString("sortBy");
    if (StringUtils.equalsIgnoreCase("createTime", sortBy)) {
      sortBy = MetaHubConstant.DB_CREATE_TIME;
    } else {
      sortBy = MetaHubConstant.DB_UPDATE_TIME;
    }
    String order = requestParam.getString("sortType");
    if (StringUtils.equalsIgnoreCase("descend", order)) {
      order = MetaHubConstant.DESC;
    } else {
      order = MetaHubConstant.ASC;
    }
    Integer pageSize = requestParam.getInteger("pageSize");
    Integer pageNum = requestParam.getInteger("pageNum");
    requestParam.put("sortBy", sortBy);
    requestParam.put("order", order);
    requestParam.put("offset", (pageNum - 1) * pageSize);
    requestParam.put("limit", pageSize);

    String listDSResponse = metahubService.listAccountDatasource(productId, sortBy, order,
        (pageNum - 1) * pageSize, pageSize, dataSourceName, type);
    JSONObject list = getListResult(JSONObject.parseObject(listDSResponse));
    return list;
  }

  @Override
  public DataSourcesResDto getDataSourceListDto(Integer productId, String type, String searchBy, Integer pageNum, Integer pageSize) {
    DataSourcesResDto dataSourcesResDto = new DataSourcesResDto();
    String response = metahubService.listAccountDatasource(productId, null, null, (pageNum - 1) * pageSize, pageSize, searchBy, type);
    JSONArray list = JSONObject.parseObject(response).getJSONArray("list");
    if (CollectionUtils.isEmpty(list)) {
      dataSourcesResDto.setDataSources(Lists.newArrayList());
      return dataSourcesResDto;
    }
    List<DataSourceSimpleDto> dataSourceSimpleDtoList = list.stream().map(item -> {
      JSONObject dataSource = (JSONObject) item;
      Long dataSourceId = dataSource.getLong("id");
      String dataSourceName = dataSource.getString("name");
      return new DataSourceSimpleDto(dataSourceId, dataSourceName);
    }).collect(Collectors.toList());
    dataSourcesResDto.setDataSources(dataSourceSimpleDtoList);
    return dataSourcesResDto;
  }

  private JSONObject getListResult(JSONObject data) {
    String product = NdiContext.get(ContextConstant.PRODUCT);
    JSONObject result = new JSONObject();
    JSONArray list = data.getJSONArray("list");
    JSONArray dataSources = new JSONArray();

    List<CheckAndDataSourceId> checkAndDataSourceIdList = Lists.newArrayList();

    if (list != null && list.size() > 0) {
      for (int i = 0; i < list.size(); i++) {
        CheckAndDataSourceId checkAndDataSourceId = new CheckAndDataSourceId();
        JSONObject dsItem = list.getJSONObject(i);
        String creator = dsItem.getString("creator");
        String modifier = dsItem.getString("modifier");
        JSONObject dataSource = new JSONObject();
        Long dataSourceId = dsItem.getLong("id");
        dataSource.put("id", dataSourceId);
        checkAndDataSourceId.setDataSourceId(dataSourceId);
        dataSource.put("name", dsItem.get("name"));
        //TODO: 优化
        dataSource.put("owner", userService.getProductUsername(product, creator));
        dataSource.put("modifier", userService.getProductUsername(product, modifier));
        dataSource.put("type", dsItem.get("type"));
        dataSource.put("createTime", dsItem.get("createTime"));
        dataSource.put("modifyTime", dsItem.get("updateTime"));
        JSONObject dataSourceConnCheckStatus = dsItem.getJSONObject("dataSourceConnCheckStatus");
        if (dataSourceConnCheckStatus != null) {
          Integer status = dataSourceConnCheckStatus.getInteger("status");
          String connectivityStatus = DataSourceConstant.ConnectivityResultEnum.nameOfType(status);
          dataSource.put("connectivityStatus", connectivityStatus);
          Long checkId = dataSourceConnCheckStatus.getLong("checkerId");
          dataSource.put("checkId", checkId);
          checkAndDataSourceId.setCheckId(checkId);
        }
        JSONObject info = dsItem.getJSONObject("info");
        if (info != null) {
          String userName = info.getString("userName");
          String user = info.getString("user");
          if (!StringUtils.isBlank(userName)) {
            info.put("user", userName);
          } else if (!StringUtils.isBlank(user)) {
            info.put("user", user);
          }
        }
        info.remove("password");
        dataSource.put("connectionInformation", info);
        dataSources.add(dataSource);
        checkAndDataSourceIdList.add(checkAndDataSourceId);
      }
    }

    List<ConnectivityStatusResDto> connectivityStatusResDtoList = connectivityService.status(new ConnectivityStatusReqDto(checkAndDataSourceIdList));
    Map<Long, ConnectivityStatusResDto> statusMap = connectivityStatusResDtoList.stream()
        .collect(Collectors.toMap(ConnectivityStatusResDto::getDataSourceId, Function.identity(), (a, b) -> a));
    for (int i = 0; i < dataSources.size(); i++) {
      JSONObject dataSource = dataSources.getJSONObject(i);
      Long dataSourceId = dataSource.getLong("id");
      ConnectivityStatusResDto connectivityStatusResDto = statusMap.get(dataSourceId);
      String status = connectivityStatusResDto.getStatus();
      dataSource.put("connectivityStatus", status);
    }
    result.put("pageNum", "");
    result.put("pageSize", "");
    result.put("total", data.get("count"));
    result.put("dataSources", dataSources);
    return result;
  }

  @Override
  public JSONObject listCatalogWithDataSource(Integer accountId, String type, Integer pageNum, Integer pageSize) {
    int offset = (pageNum - 1) * pageSize;
    JSONObject listDSResponse = metahubService.listCatalogWithDataSources(accountId, offset, pageSize, type, null);
    return listDSResponse;
  }

  @Override
  public JSONObject listIdAndNameAndCatalogNameResult(JSONObject data, String dataSourceType) {
    JSONObject result = new JSONObject();
    JSONArray list = data.getJSONArray("list");
    JSONArray dataSources = new JSONArray();
    if (list != null && list.size() > 0) {
      for (int i = 0; i < list.size(); i++) {
        JSONObject jsonObject = list.getJSONObject(i);
        String catalogName = jsonObject.getString("name");
        JSONArray dataSourceDetail = jsonObject.getJSONArray("dataSourceDetail");
        if (dataSourceDetail != null && dataSourceDetail.size() > 0) {
          for (int j = 0; j < dataSourceDetail.size(); j++) {
            JSONObject dsItem = dataSourceDetail.getJSONObject(j);
            JSONObject dataSource = new JSONObject();
            String type = dsItem.getString("type").trim();
            if (StringUtils.equalsIgnoreCase(type, dataSourceType)) {
              dataSource.put("id", dsItem.get("id"));
              dataSource.put("name", dsItem.get("name"));
              dataSource.put("catalogName", catalogName);
              dataSources.add(dataSource);
            }
          }
        }
      }
    }
    result.put("dataSources", dataSources);
    return result;
  }

  @Override
  public JSONObject getHive() {
    Integer accountId = NdiContext.get(ContextConstant.PRODUCT_ID);
    String clusterId = NdiContext.get(ContextConstant.CLUSTER_ID);
    JSONObject listDSResponse = metahubService.listCatalogWithDataSources(accountId,
        0, 1, DATA_SOURCE_HIVE, clusterId);
    JSONObject result = getHiveResult(listDSResponse);
    return result;
  }

  @Override
  public JSONObject getHiveIdAndCatalogName() {
    Integer accountId = NdiContext.get(ContextConstant.PRODUCT_ID);
    String clusterId = NdiContext.get(ContextConstant.CLUSTER_ID);
    JSONObject listDSResponse = metahubService.listCatalogWithDataSources(accountId,
        0, 1, DATA_SOURCE_HIVE, clusterId);
    JSONObject result = getHiveIdAndCatalogNameResult(listDSResponse);
    return result;
  }

  private JSONObject getHiveIdAndCatalogNameResult(JSONObject data) {
    JSONObject result = new JSONObject();
    JSONArray list = data.getJSONArray("list");
    JSONObject dataSource = new JSONObject();
    if (list != null && list.size() > 0) {
      for (int i = 0; i < list.size(); i++) {
        JSONObject jsonObject = list.getJSONObject(i);
        String catalogName = jsonObject.getString("name");
        JSONArray dataSourceDetail = jsonObject.getJSONArray("dataSourceDetail");
        if (dataSourceDetail != null && dataSourceDetail.size() > 0) {
          for (int j = 0; j < dataSourceDetail.size(); j++) {
            JSONObject dsItem = dataSourceDetail.getJSONObject(j);
            dataSource.put("id", dsItem.get("id"));
            dataSource.put("name", dsItem.get("name"));
            dataSource.put("catalogName", catalogName);
          }
        }
      }
    }
    result.put("dataSource", dataSource);
    return result;
  }

  private JSONObject getHiveResult(JSONObject data) {
    String product = NdiContext.get(ContextConstant.PRODUCT);
    JSONObject result = new JSONObject();
    JSONArray list = data.getJSONArray("list");
    JSONArray dataSources = new JSONArray();
    if (list != null && list.size() > 0) {
      for (int i = 0; i < list.size(); i++) {
        JSONObject jsonObject = list.getJSONObject(i);
        String creator = jsonObject.getString("creator");
        String modifier = jsonObject.getString("modifier");
        String catalogName = jsonObject.getString("name");
        JSONArray dataSourceDetail = jsonObject.getJSONArray("dataSourceDetail");
        if (dataSourceDetail != null && dataSourceDetail.size() > 0) {
          for (int j = 0; j < dataSourceDetail.size(); j++) {
            JSONObject dsItem = dataSourceDetail.getJSONObject(j);
            JSONObject dataSource = new JSONObject();
            dataSource.put("id", dsItem.get("id"));
            dataSource.put("name", dsItem.get("name"));
            dataSource.put("owner", userService.getProductUsername(product, creator));
            dataSource.put("modifier", userService.getProductUsername(product, modifier));
            dataSource.put("type", dsItem.get("type"));
            dataSource.put("createTime", dsItem.get("createTime"));
            dataSource.put("modifyTime", dsItem.get("updateTime"));
            dataSource.put("connectionInformation", dsItem.get("info"));
            dataSource.put("catalogName", catalogName);
            dataSources.add(dataSource);
          }
        }
      }
    }
    result.put("pageNum", "");
    result.put("pageSize", "");
    result.put("total", data.get("count"));
    result.put("dataSources", dataSources);
    return result;
  }

  @Override
  public JSONObject get(JSONObject getDataSourceParam) {
    ParamUtil.validate(getDataSourceParam);
    User user = getDataSourceParam.getObject("user", User.class);
    Integer accountId = null;
    if (user != null) {
      String email = user.getEmail();
      String product = user.getProduct();
      accountId = userService.getProductId(email, product);
    } else {
      accountId = NdiContext.get(ContextConstant.PRODUCT_ID);
    }
    Long dataSourceId = getDataSourceParam.getLong("id");
    JSONObject getDSResponse = metahubService.getDataSource(accountId, dataSourceId);
    if (getDSResponse == null) {
      throw new IllegalArgumentException("数据源不存在");
    }
    JSONObject result = getDataSourceResult(getDSResponse);
    return result;
  }

  @Override
  public JSONObject get(Integer accountId, Long dataSourceId) {
    JSONObject getDSResponse = metahubService.getDataSource(accountId, dataSourceId);
    if (getDSResponse == null) {
      return null;
    }
    JSONObject result = getDataSourceResult(getDSResponse);
    return result;
  }

  @Override
  public JSONObject get(String email, String product, Long dataSourceId) {
    Integer accountId = null;
    if (StringUtils.isNotBlank(email) && StringUtils.isNotBlank(product)) {
      accountId = userService.getProductId(email, product);
    } else {
      accountId = NdiContext.get(ContextConstant.PRODUCT_ID);
    }
    JSONObject getDSResponse = metahubService.getDataSource(accountId, dataSourceId);
    if (getDSResponse == null) {
      throw new IllegalArgumentException("数据源不存在");
    }
    JSONObject result = getDataSourceResult(getDSResponse);
    return result;
  }

  @Override
  public JSONObject get(Long dataSourceId) {
    return get(null, null, dataSourceId);
  }

  private JSONObject getDataSourceResult(JSONObject data) {
    String product = NdiContext.get(ContextConstant.PRODUCT);
    JSONObject result = new JSONObject();
    result.put("id", data.get("id"));
    result.put("name", data.get("name"));
    result.put("owner", userService.getProductUsername(product, data.getString("creator")));
    result.put("modifier", userService.getProductUsername(product, data.getString("modifier")));
    result.put("type", data.get("type"));
    result.put("createTime", data.get("createTime"));
    result.put("modifyTime", data.get("updateTime"));
    JSONObject info = data.getJSONObject("info");
    if (info != null) {
      String userName = info.getString("userName");
      String user = info.getString("user");
      if (!StringUtils.isBlank(userName)) {
        info.put("user", userName);
      } else if (!StringUtils.isBlank(user)) {
        info.put("user", user);
      }
    }
    result.put("connectionInformation", info);
    return result;
  }


  @Override
  public JSONObject delete(DeleteDataSourceReqDto deleteDataSourceParam) {
    ParamUtil.validate(deleteDataSourceParam);
    String modifier = NdiContext.get(ContextConstant.EMAIL);
    Integer accountId = NdiContext.get(ContextConstant.PRODUCT_ID);
    List<Long> ids = deleteDataSourceParam.getId();
    if (ids == null || ids.size() < 1) {
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "Deleting id don't is null");
    }

    for (Long id : ids) {
//      DataSourceQuoteDto dataSourceQuoteDto = taskDataSourceFacade.getDataSourceQuote(id, product, clusterId);
//      if (CollectionUtils.isNotEmpty(dataSourceQuoteDto.getTaskList())) {
//        throw new NdiException(-300, "数据源被任务引用，不能删除");
//      }
      JSONObject getDataSourceResponse = metahubService.getDataSource(accountId, id);
      if (getDataSourceResponse == null) {
        return null;
      }
      metahubService.deleteDataSource(accountId, id, modifier);
    }
    return null;
  }

  @Override
  public void delete(Long id) {
    Integer accountId = NdiContext.get(ContextConstant.PRODUCT_ID);
    String modifier = NdiContext.get(ContextConstant.EMAIL);
    JSONObject response = metahubService.getDataSource(accountId, id);
    if (response == null) {
      return;
    }
    metahubService.deleteDataSource(accountId, id, modifier);
  }

  @Override
  public JSONObject listDatabase(Integer productId, Long dataSourceId, String searchBy, Integer pageNum, Integer pageSize) {
    int offset = (pageNum - 1) * pageSize;
    String catalog = metahubService.getCatalogFromCache(dataSourceId);
    JSONArray listDBResponse = metahubService.listDatabases(productId, catalog, offset, pageSize,
        searchBy, dataSourceId);
    JSONObject result = listDatabaseResult(listDBResponse);
    return result;
  }

  private JSONObject listDatabaseResult(JSONArray listDBResponse) {
    ParamUtil.validate(listDBResponse);
    JSONArray dbs = new JSONArray();
    JSONArray data = listDBResponse;
    if (data != null && data.size() > 0) {
      for (int i = 0; i < data.size(); i++) {
        JSONObject item = data.getJSONObject(i);
        dbs.add(item.get("db"));
      }
    }

    JSONObject result = new JSONObject();
    result.put("dbs", dbs);
    return result;
  }

  @Override
  public JSONObject listTableName(Integer productId, Long dataSourceId, String databaseName, String searchBy, Integer pageNum, Integer pageSize) {
    int offset = (pageNum - 1) * pageSize;
    String catalog = metahubService.getCatalogFromCache(dataSourceId);
    JSONArray listTNResponse = metahubService.listTableNames(catalog, databaseName, offset, pageSize, productId,
        searchBy, dataSourceId);
    JSONObject result = listTableNameResult(listTNResponse);
    return result;
  }

  public JSONObject listTableNameResult(JSONArray listTNResponse) {
    ParamUtil.validate(listTNResponse);
    JSONArray data = listTNResponse;
    JSONObject result = new JSONObject();
    result.put("tableNames", data);
    return result;
  }

  @Override
  public JSONObject getTable(Integer productId, Long dataSourceId, String db, String table) {
    String catalog = metahubService.getCatalogFromCache(dataSourceId);
    JSONObject getTResponse = metahubService.getTable(catalog, productId, db, table);
    JSONObject result = getTResult(getTResponse);
    return result;
  }

  private JSONObject getTResult(JSONObject getTResponse) {
    ParamUtil.validate(getTResponse);
    JSONArray columns = new JSONArray();
    JSONArray partitions = new JSONArray();
    JSONObject data = getTResponse;
    String db = data.getString("db");
    String table = data.getString("table");
    JSONArray fields = data.getJSONArray("fields");
    if (fields != null && fields.size() > 0) {
      for (int i = 0; i < fields.size(); i++) {
        JSONObject field = fields.getJSONObject(i);
        JSONObject column = new JSONObject();
        column.put("comment", field.get("comments"));
        column.put("name", field.get("name"));
        column.put("sourceType", field.get("sourceType"));
        column.put("type", field.get("type"));
        Boolean partitionKey = field.getBoolean("partitionKey");
        column.put("partitionKey", partitionKey);
        if (partitionKey) {
          partitions.add(field.get("name"));
        }
        columns.add(column);
      }
    }
    JSONObject result = new JSONObject();
    result.put("db", db);
    result.put("table", table);
    result.put("columns", columns);
    result.put("partitions", partitions);
    return result;
  }

  /**
   * 登记数据源
   *
   * @param dataSourceType 数据源类型
   * @param catalogType    catalogType
   * @param dataSourceName 数据源名字
   * @param dataSourceInfo 数据源连接信息
   * @param creator        登记人
   * @return 数据源id
   */
  @Override
  public Long addDataSource(String dataSourceType, String catalogType, String dataSourceName,
                            JSONObject dataSourceInfo, String creator) {
    Integer accountId = NdiContext.get(ContextConstant.PRODUCT_ID);
    JSONObject addDSResponse = metahubService.addDataSource(accountId, dataSourceName, dataSourceType,
        DATA_SOURCE_ENV_READ, dataSourceInfo, creator);
    Long dataSourceId = addDSResponse.getLong("id");
    String catalog = metahubService.getCatalog(dataSourceId, accountId);
    if (StringUtils.isNotBlank(catalog)) {
      return dataSourceId;
    }
    // 登记数据源添加catalog失败时，即认为数据源登记失败，catalog添加失败将导致不能获取库表列
    try {
      addCatalog(accountId, catalogType, dataSourceName, creator, dataSourceId);
    } catch (MetahubException e) {
      log.error("Failed to add catalog", e);
      delete(dataSourceId);
      throw e;
    }
    return dataSourceId;
  }

  @Override
  public List<String> listTableNameByRegexp(Long dataSourceId, String db, String regexp, Integer pageNum, Integer pageSize) {
    String catalog = metahubService.getCatalogFromCache(dataSourceId);
    Integer offset = (pageNum - 1) * pageSize;
    List<String> tableNameList = metahubService.listTableNameByRegex(catalog, db, offset, pageSize, regexp);
    return tableNameList;
  }

  @Override
  public JSONObject modifyDataSource(JSONObject connectionInformation, Integer accountId, Long dataSourceId,
                                     String dataSourceType, String dataSourceName, String modifier) {
    JSONObject modifyDS = new JSONObject();
    modifyDS.put("id", dataSourceId);
    modifyDS.put("name", dataSourceName);
    modifyDS.put("type", dataSourceType);
    modifyDS.put("env", DATA_SOURCE_ENV_READ);
    modifyDS.put("info", connectionInformation);
    modifyDS.put("accountId", accountId);
    modifyDS.put("modifier", modifier);
    JSONObject modifyResponse = metahubService.modifyDataSource(modifyDS);
    return modifyResponse;
  }

  @Override
  public String getDataSourceUrl(Long dataSourceId) {
    JSONObject getDataSourceResult = get(dataSourceId);
    String url = null;
    try {
      url = getDataSourceResult.getJSONObject("connectionInformation").getString("url");
    } catch (Exception e) {
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "The datasource don't exist connection information");
    }
    return url;
  }


  @Override
  public List<ConnectivityResultDto> listConnectivityResult(DataSourceConnectivityReqDto reqDto) {
    List<Long> checkIdList = reqDto.getCheckIdList();
    Integer accountId = NdiContext.get(ContextConstant.PRODUCT_ID);
    JSONArray jsonArray = metahubService.batchConnectionResult(accountId, checkIdList);
    List<ConnectivityResultDto> list = Lists.newArrayList();
    if (jsonArray == null || jsonArray.size() == 0) {
      return list;
    }

    for (int i = 0; i < jsonArray.size(); i++) {
      JSONObject jsonObject = jsonArray.getJSONObject(i);
      Integer status = jsonObject.getInteger("status");
      String statusString = DataSourceConstant.ConnectivityResultEnum.nameOfType(status);
      JSONArray dataSourceConnList = jsonObject.getJSONArray("dataSourceConResultList");
      List<DataSourceConResult> dataSourceConResultList = JSONArray.parseArray(dataSourceConnList.toJSONString(), DataSourceConResult.class);
      ConnectivityResultDto resultDto = new ConnectivityResultDto(statusString, dataSourceConResultList);
      list.add(resultDto);
    }
    return list;
  }

  public Map<Long, ConnectivityResultEnum> listMetaHubConnectivityStatus(List<Long> checkIdList) {
    Integer accountId = NdiContext.get(ContextConstant.PRODUCT_ID);
    JSONArray jsonArray = metahubService.batchConnectionResult(accountId, checkIdList);
    List<ConnectivityResultDtoV2> list = Lists.newArrayList();
    if (jsonArray == null || jsonArray.size() == 0) {

    }

    for (int i = 0; i < jsonArray.size(); i++) {
      JSONObject jsonObject = jsonArray.getJSONObject(i);
      Integer status = jsonObject.getInteger("status");

      JSONArray dataSourceConnList = jsonObject.getJSONArray("dataSourceConResultList");
      List<DataSourceConResult> dataSourceConResultList = JSONArray.parseArray(dataSourceConnList.toJSONString(), DataSourceConResult.class);
      ConnectivityResultDtoV2 resultDto = new ConnectivityResultDtoV2(status, dataSourceConResultList);
      list.add(resultDto);
    }
    return null;
  }

  @Override
  public Long connectivity(ConnectivityDto connectivityDto) {
    Integer accountId = NdiContext.get(ContextConstant.PRODUCT_ID);
    String catalog = metahubService.getCatalogFromCache(connectivityDto.getDataSourceId());
    String result = metahubService.dataSourceConnCheck(catalog, accountId);
    Long checkId = Long.parseLong(result);
    return checkId;
  }

  @Override
  public void addCatalog(Integer accountId, String type, String name, String creator, Long dataSourceId) {
    JSONObject addCatalogParam = new JSONObject();
    String catalog = UUID.randomUUID().toString();
    addCatalogParam.put("accountId", accountId);
    addCatalogParam.put("type", type);
    addCatalogParam.put("name", dataSourceId + "_" + catalog);
    addCatalogParam.put("description", type + "_" + name + "_catalog");
    addCatalogParam.put("creator", creator);
    addCatalogParam.put("modifier", creator);

    JSONArray jsonArray = new JSONArray();
    JSONObject ds = new JSONObject(2);
    ds.put("dataSourceId", dataSourceId);
    ds.put("isDefault", 1);
    jsonArray.add(ds);
    addCatalogParam.put("dataSourceList", jsonArray);
    metahubService.addCatalog(addCatalogParam);
  }

}
