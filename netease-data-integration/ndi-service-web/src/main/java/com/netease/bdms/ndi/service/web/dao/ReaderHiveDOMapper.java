package com.netease.bdms.ndi.service.web.dao;

import com.netease.bdms.ndi.service.web.pojo.ReaderHiveDO;

public interface ReaderHiveDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ReaderHiveDO record);

    int insertSelective(ReaderHiveDO record);

    ReaderHiveDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ReaderHiveDO record);

    int updateByPrimaryKey(ReaderHiveDO record);
}