package com.netease.bdms.ndi.service.web.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.config.ContextConstant;
import com.netease.bdms.ndi.service.web.config.NdiContext;
import com.netease.bdms.ndi.service.web.dto.datasource.DataSourcesResDto;
import com.netease.bdms.ndi.service.web.exception.MetahubException;
import com.netease.bdms.ndi.service.web.exception.NdiException;
import com.netease.bdms.ndi.service.web.param.User;
import com.netease.bdms.ndi.service.web.service.DDBDataSourceService;
import com.netease.bdms.ndi.service.web.service.DataSourceService;
import com.netease.bdms.ndi.service.web.service.UserService;
import com.netease.bdms.ndi.service.web.util.DataSourceTypeEnum;
import com.netease.bdms.ndi.service.web.util.ParamUtil;
import com.netease.bdms.ndi.service.web.util.ProcessStatusEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName DDBDataSourceServiceImpl
 * @Description DDB数据源实现
 * @Author Min Zhao
 * @Version 1.0
 **/
@Service
public class DDBDataSourceServiceImpl implements DDBDataSourceService {
  private static final Logger log = LoggerFactory.getLogger(DDBDataSourceServiceImpl.class);
  private static final String DATA_SOURCE_DDB = "ddb";
  private static final String DATA_SOURCE_DDB_QS = "ddbqs";
  private static final String DATA_SOURCE_MYSQL = "mysql";
  @Autowired
  private DataSourceService dataSourceService;
  @Autowired
  private MetahubService metahubService;


  @Override
  public JSONObject listDBIIdAndNameAndCatalogName(Integer productId, Integer pageNum, Integer pageSize) {
    JSONObject listResponse = dataSourceService.listCatalogWithDataSource(productId, DATA_SOURCE_DDB, pageNum, pageSize);
    JSONObject result = dataSourceService.listIdAndNameAndCatalogNameResult(listResponse, DATA_SOURCE_DDB);
    return result;
  }

  @Override
  public JSONObject listQSIdAndNameAndCatalogName(Integer productId, Integer pageNum, Integer pageSize) {
    JSONObject listResponse = dataSourceService.listCatalogWithDataSource(productId, DATA_SOURCE_MYSQL, pageNum, pageSize);
    JSONObject result = dataSourceService.listIdAndNameAndCatalogNameResult(listResponse, DATA_SOURCE_DDB_QS);
    return result;
  }

  @Override
  public DataSourcesResDto getDBIDataSourcesResDto(Integer productId, String searchBy, Integer pageNum, Integer pageSize) {
    return dataSourceService.getDataSourceListDto(productId, DataSourceTypeEnum.DDB.getName(), searchBy, pageNum, pageSize);
  }

  @Override
  public DataSourcesResDto getQSDataSourcesResDto(Integer productId, String searchBy, Integer pageNum, Integer pageSize) {
    return dataSourceService.getDataSourceListDto(productId, DataSourceTypeEnum.DDBQS.getName(), searchBy, pageNum, pageSize);
  }

  @Override
  public Long create(JSONObject createDataSourceParam) {
    /**
     * dbi: ddb; catalogType: ddb
     * qs: ddbqs; catalogType: mysql
     */
    String creator = NdiContext.get(ContextConstant.EMAIL);
    String dataSourceName = createDataSourceParam.getString("name").trim();
    String url = createDataSourceParam.getString("url").trim();
    String username = createDataSourceParam.getString("username").trim();
    String password = createDataSourceParam.getString("password").trim();
    String type = createDataSourceParam.getString("type").trim();

    JSONObject info = new JSONObject();
    info.put("url", url);
    info.put("user", username);
    info.put("userName", username);
    info.put("password", password);

    Long dataSourceId = null;
    if (StringUtils.equalsIgnoreCase(type, DATA_SOURCE_DDB)){
      String version = createDataSourceParam.getString("version");
      info.put("driver", "com.netease.backend.db.DBDriver");
      info.put("version", version);
      info.put("defaultDb", "default");
      dataSourceId = dataSourceService.addDataSource(DataSourceTypeEnum.DDB.getName(), DataSourceTypeEnum.DDB.getName(),
          dataSourceName, info, creator);
    } else if (StringUtils.equalsIgnoreCase(type, DATA_SOURCE_DDB_QS)){
      info.put("driver", "com.mysql.jdbc.Driver");
      dataSourceId = dataSourceService.addDataSource(DataSourceTypeEnum.DDBQS.getName(), DataSourceTypeEnum.DDBQS.getName(),
          dataSourceName, info, creator);
    } else {
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "The type can't be supported");
    }

    return dataSourceId;
  }

  @Override
  public JSONObject modify(JSONObject modifyDataSourceParam) {
    ParamUtil.validate(modifyDataSourceParam);
    Integer accountId = NdiContext.get(ContextConstant.PRODUCT_ID);
    String modifier = NdiContext.get(ContextConstant.EMAIL);
    Long dataSourceId = modifyDataSourceParam.getLong("id");
    String dataSourceName = modifyDataSourceParam.getString("name").trim();
    String url = modifyDataSourceParam.getString("url").trim();
    String username = modifyDataSourceParam.getString("username").trim();
    String password = modifyDataSourceParam.getString("password").trim();
    if (StringUtils.isBlank(password)) {
      JSONObject dataSource = metahubService.getDataSource(accountId, dataSourceId);
      if (dataSource == null) {
        throw new IllegalArgumentException("数据源不存在");
      }
      JSONObject oldInfo = dataSource.getJSONObject("info");
      password = oldInfo.getString("password");
    }

    JSONObject modifyResponse = new JSONObject();
    String type = modifyDataSourceParam.getString("type").trim();
    JSONObject info = new JSONObject();
    info.put("url", url);
    info.put("user", username);
    info.put("userName", username);
    info.put("password", password);
    if (StringUtils.equalsIgnoreCase(type, DATA_SOURCE_DDB)){
      String version = modifyDataSourceParam.getString("version");
      info.put("type", DATA_SOURCE_DDB);
      info.put("driver", "com.netease.backend.db.DBDriver");
      info.put("version", version);
      info.put("defaultDb", "defaultDb");
      modifyResponse = dataSourceService.modifyDataSource(info, accountId, dataSourceId, DATA_SOURCE_DDB, dataSourceName, modifier);
    } else if (StringUtils.equalsIgnoreCase(type, DATA_SOURCE_DDB_QS)){
      info.put("type", DATA_SOURCE_DDB_QS);
      info.put("driver", "com.mysql.jdbc.Driver");
      modifyResponse = dataSourceService.modifyDataSource(info, accountId, dataSourceId, DATA_SOURCE_DDB_QS, dataSourceName, modifier);
    } else {
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "The type can't be supported");
    }

    return modifyResponse;
  }
}
