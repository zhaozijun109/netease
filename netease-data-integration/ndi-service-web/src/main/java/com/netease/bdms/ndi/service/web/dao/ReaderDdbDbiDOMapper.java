package com.netease.bdms.ndi.service.web.dao;

import com.netease.bdms.ndi.service.web.pojo.ReaderDdbDbiDO;

public interface ReaderDdbDbiDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ReaderDdbDbiDO record);

    int insertSelective(ReaderDdbDbiDO record);

    ReaderDdbDbiDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ReaderDdbDbiDO record);

    int updateByPrimaryKey(ReaderDdbDbiDO record);
}