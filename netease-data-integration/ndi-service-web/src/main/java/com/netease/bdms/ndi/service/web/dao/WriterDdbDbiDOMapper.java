package com.netease.bdms.ndi.service.web.dao;

import com.netease.bdms.ndi.service.web.pojo.WriterDdbDbiDO;

public interface WriterDdbDbiDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(WriterDdbDbiDO record);

    int insertSelective(WriterDdbDbiDO record);

    WriterDdbDbiDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(WriterDdbDbiDO record);

    int updateByPrimaryKey(WriterDdbDbiDO record);
}