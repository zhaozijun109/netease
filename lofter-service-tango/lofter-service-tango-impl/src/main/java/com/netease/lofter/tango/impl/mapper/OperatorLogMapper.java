package com.netease.lofter.tango.impl.mapper;

import com.netease.lofter.tango.impl.entity.OperatorLog;
import com.netease.yaolu.commons.spring.mybatis.mapper.CommonMapper;
import com.netease.yaolu.commons.spring.mybatis.mapper.DeleteCommonMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperatorLogMapper extends DeleteCommonMapper<OperatorLog>, CommonMapper<OperatorLog> {
}