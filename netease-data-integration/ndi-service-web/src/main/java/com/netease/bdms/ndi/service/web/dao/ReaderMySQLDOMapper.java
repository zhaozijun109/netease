package com.netease.bdms.ndi.service.web.dao;

import com.netease.bdms.ndi.service.web.pojo.ReaderMySQLDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReaderMySQLDOMapper {
  int deleteByPrimaryKey(Long id);

  int insert(ReaderMySQLDO record);

  int insertSelective(ReaderMySQLDO record);

  ReaderMySQLDO selectByPrimaryKey(Long id);

  int updateByPrimaryKeySelective(ReaderMySQLDO record);

  int updateByPrimaryKey(ReaderMySQLDO record);

}