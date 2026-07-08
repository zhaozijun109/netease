package com.netease.bdms.ndi.service.web.dao;

import com.netease.bdms.ndi.service.web.pojo.WriterOracleDO;

public interface WriterOracleDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(WriterOracleDO record);

    int insertSelective(WriterOracleDO record);

    WriterOracleDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(WriterOracleDO record);

    int updateByPrimaryKey(WriterOracleDO record);
}