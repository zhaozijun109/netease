package com.netease.bdms.ndi.service.web.dao;

import com.netease.bdms.ndi.service.web.pojo.ReaderDdbQsDO;

public interface ReaderDdbQsDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ReaderDdbQsDO record);

    int insertSelective(ReaderDdbQsDO record);

    ReaderDdbQsDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ReaderDdbQsDO record);

    int updateByPrimaryKey(ReaderDdbQsDO record);
}