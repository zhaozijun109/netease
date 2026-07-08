package com.netease.bdms.ndi.service.web.dao;

import com.netease.bdms.ndi.service.web.pojo.WriterDdbQsDO;

public interface WriterDdbQsDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(WriterDdbQsDO record);

    int insertSelective(WriterDdbQsDO record);

    WriterDdbQsDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(WriterDdbQsDO record);

    int updateByPrimaryKey(WriterDdbQsDO record);
}