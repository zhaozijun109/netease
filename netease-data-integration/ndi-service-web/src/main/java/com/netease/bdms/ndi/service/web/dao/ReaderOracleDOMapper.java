package com.netease.bdms.ndi.service.web.dao;

import com.netease.bdms.ndi.service.web.pojo.ReaderOracleDO;

public interface ReaderOracleDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ReaderOracleDO record);

    int insertSelective(ReaderOracleDO record);

    ReaderOracleDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ReaderOracleDO record);

    int updateByPrimaryKey(ReaderOracleDO record);
}