package com.netease.bdms.ndi.service.web.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.dao.ConfigDOMapper;
import com.netease.bdms.ndi.service.web.pojo.ConfigDO;
import com.netease.bdms.ndi.service.web.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @ClassName ConfigServiceImpl
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Service
public class ConfigServiceImpl implements ConfigService {
  @Autowired
  private ConfigDOMapper configDOMapper;

  @Override
  public JSONObject getFrontedGlobalConfig() {
    ConfigDO configDO = configDOMapper.selectByNamespaceAndName("fronted", "global");
    JSONObject jsonObject = configDO.getProp();
    return jsonObject;
  }

  @Override
  public void addFrontedConfig(JSONObject param) {
    ConfigDO configDO = new ConfigDO();
    configDO.setProp(param.getJSONObject("property"));
    configDO.setName(param.getString("name"));
    configDO.setNamespace(param.getString("namespace"));
    configDO.setCreatTime(new Date());
    configDO.setModifyTime(new Date());
    int result = configDOMapper.insert(configDO);
  }

  @Override
  public JSONObject getAzkabanUrlConfig() {
    ConfigDO configDO = configDOMapper.selectByNamespaceAndName("azkaban", "clustersUrl");
    JSONObject jsonObject = configDO.getProp();
    return jsonObject;
  }

}
