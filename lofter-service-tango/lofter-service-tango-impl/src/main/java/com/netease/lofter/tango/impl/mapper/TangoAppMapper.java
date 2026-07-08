package com.netease.lofter.tango.impl.mapper;

import com.netease.lofter.tango.impl.entity.TangoAppDO;
import com.netease.yaolu.commons.spring.mybatis.mapper.CommonMapper;
import com.netease.yaolu.commons.spring.mybatis.mapper.DeleteCommonMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TangoAppMapper extends CommonMapper<TangoAppDO>, DeleteCommonMapper<TangoAppDO> {
}
