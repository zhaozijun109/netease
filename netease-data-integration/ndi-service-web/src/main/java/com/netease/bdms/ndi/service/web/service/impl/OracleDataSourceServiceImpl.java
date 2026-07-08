package com.netease.bdms.ndi.service.web.service.impl;

import java.util.Date;

import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.config.ContextConstant;
import com.netease.bdms.ndi.service.web.config.NdiContext;
import com.netease.bdms.ndi.service.web.dto.datasource.DataSourcesResDto;
import com.netease.bdms.ndi.service.web.exception.MetahubException;
import com.netease.bdms.ndi.service.web.service.DataSourceService;
import com.netease.bdms.ndi.service.web.service.MySQLDataSourceService;
import com.netease.bdms.ndi.service.web.service.OracleDataSourceService;
import com.netease.bdms.ndi.service.web.service.UserService;
import com.netease.bdms.ndi.service.web.util.DataSourceTypeEnum;
import com.netease.bdms.ndi.service.web.util.DateUtil;
import com.netease.bdms.ndi.service.web.util.ParamUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName OracleDataSourceServiceImpl
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Service
public class OracleDataSourceServiceImpl implements OracleDataSourceService {
  private static final Logger log = LoggerFactory.getLogger(MySQLDataSourceService.class);
  @Autowired
  private MetahubService metahubService;
  @Autowired
  private UserService userService;
  @Autowired
  private DataSourceService dataSourceService;

  private static final String DATA_SOURCE_ORACLE = "oracle";

  @Override
  public JSONObject listIdAndNameAndCatalogName(Integer productId, Integer pageNum, Integer pageSize) {
    JSONObject listResponse = dataSourceService.listCatalogWithDataSource(productId, DATA_SOURCE_ORACLE, pageNum, pageSize);
    JSONObject result = dataSourceService.listIdAndNameAndCatalogNameResult(listResponse, DATA_SOURCE_ORACLE);
    return result;
  }

  @Override
  public DataSourcesResDto getDataSourcesResDto(Integer productId, String searchBy, Integer pageNum, Integer pageSize) {
    return dataSourceService.getDataSourceListDto(productId, DataSourceTypeEnum.ORACLE.getName(), searchBy, pageNum, pageSize);
  }

  @Override
  public Long create(JSONObject createDataSourceParam) {
    ParamUtil.validate(createDataSourceParam);
    String creator = NdiContext.get(ContextConstant.EMAIL);
    String dataSourceName = createDataSourceParam.getString("name").trim();
    String url = createDataSourceParam.getString("url").trim();
    String username = createDataSourceParam.getString("username").trim();
    String password = createDataSourceParam.getString("password").trim();

    JSONObject info = new JSONObject();
    info.put("url", url);
    info.put("user", username);
    info.put("userName", username);
    info.put("password", password);
    Long dataSourceId = dataSourceService.addDataSource(DataSourceTypeEnum.ORACLE.getName(), DataSourceTypeEnum.ORACLE.getName(),
        dataSourceName, info, creator);
    return dataSourceId;
  }

  @Override
  public JSONObject modify(JSONObject modifyDataSourceParam) {
    ParamUtil.validate(modifyDataSourceParam);
    Integer accountId = NdiContext.get(ContextConstant.PRODUCT_ID);
    String modifier = NdiContext.get(ContextConstant.EMAIL);
    JSONObject modifyDS = new JSONObject();
    Long dataSourceId = modifyDataSourceParam.getLong("id");
    modifyDS.put("id", dataSourceId);
    modifyDS.put("name", modifyDataSourceParam.get("name"));
    modifyDS.put("type", DATA_SOURCE_ORACLE);
    modifyDS.put("env", "read");
    String password = modifyDataSourceParam.getString("password");
    if (StringUtils.isBlank(password)) {
      JSONObject dataSource = metahubService.getDataSource(accountId, dataSourceId);
      if (dataSource == null) {
        throw new IllegalArgumentException("数据源不存在");
      }
      JSONObject oldInfo = dataSource.getJSONObject("info");
      password = oldInfo.getString("password");
    }
    JSONObject info = new JSONObject();
    info.put("url", modifyDataSourceParam.get("url"));
    info.put("user", modifyDataSourceParam.get("username"));
    info.put("password", password);
    info.put("driver", "com.mysql.jdbc.Driver");
    modifyDS.put("info", info);
    modifyDS.put("accountId", accountId);
    modifyDS.put("modifier", modifier);
    JSONObject modifyResponse = metahubService.modifyDataSource(modifyDS);
    JSONObject result = modifyResult(modifyResponse);
    return result;
  }

  private JSONObject modifyResult(JSONObject data) {
    String product = NdiContext.get(ContextConstant.PRODUCT);
    String modifier = NdiContext.get(ContextConstant.EMAIL);
    JSONObject result = new JSONObject();
    result.put("id", data.get("id"));
    result.put("name", data.get("name"));
    result.put("createTime", data.get("createTime"));
    result.put("type", data.get("type"));
    result.put("modifyTime", DateUtil.format(new Date()));
    result.put("modifier", userService.getProductUsername(product, modifier));
    JSONObject connectInformation = new JSONObject();
    JSONObject info = data.getJSONObject("info");
    connectInformation.put("user", info.get("user"));
    connectInformation.put("password", info.get("password"));
    connectInformation.put("url", info.get("url"));
    result.put("connectionInformation", connectInformation);
    return result;
  }
}
