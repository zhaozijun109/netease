package com.netease.bdms.ndi.service.web.dao;

import com.netease.bdms.ndi.service.web.pojo.ConfigDO;
import org.apache.ibatis.annotations.Param;

public interface ConfigDOMapper {
  int deleteByPrimaryKey(Integer id);

  int insert(ConfigDO record);

  int insertSelective(ConfigDO record);

  ConfigDO selectByNamespaceAndName(@Param(value = "namespace") String namespace,
                                    @Param(value = "name") String name);

  ConfigDO selectByPrimaryKey(Integer id);

  int updateByPrimaryKeySelective(ConfigDO record);

  int updateByPrimaryKey(ConfigDO record);
}