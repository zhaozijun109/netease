package com.netease.bdms.ndi.service.web.dao;

import com.netease.bdms.ndi.service.web.pojo.ServiceInfoDO;

public interface ServiceInfoDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ServiceInfoDO record);

    int insertSelective(ServiceInfoDO record);

    ServiceInfoDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ServiceInfoDO record);

    int updateByPrimaryKey(ServiceInfoDO record);

    String selectSecretByServerName(String serverName);
}