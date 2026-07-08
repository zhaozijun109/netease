package com.netease.bdms.ndi.service.web.dao;

import com.netease.bdms.ndi.service.web.pojo.WriterHiveDO;

public interface WriterHiveDOMapper {
  int deleteByPrimaryKey(Long id);

  int insert(WriterHiveDO record);

  int insertSelective(WriterHiveDO record);

  WriterHiveDO selectByPrimaryKey(Long id);

  int updateByPrimaryKeySelective(WriterHiveDO record);

  int updateByPrimaryKey(WriterHiveDO record);
}
