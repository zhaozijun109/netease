package com.netease.bdms.ndi.service.web.dao;

import com.netease.bdms.ndi.service.web.pojo.WriterMySQLDO;

public interface WriterMySQLDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(WriterMySQLDO record);

    int insertSelective(WriterMySQLDO record);

    WriterMySQLDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(WriterMySQLDO record);

    int updateByPrimaryKey(WriterMySQLDO record);
}