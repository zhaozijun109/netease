package com.netease.lofter.tango.impl.mapper.trade.activity;

import com.netease.lofter.tango.impl.entity.trade.activity.PaidContentActivity;
import com.netease.yaolu.commons.datasource.dynamic.DataSource;
import com.netease.yaolu.commons.spring.mybatis.mapper.CommonMapper;
import com.netease.yaolu.commons.spring.mybatis.mapper.DeleteCommonMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DataSource(com.netease.lofter.tango.impl.config.DbConfig.MAIN_DATASOURCE)
public interface PaidContentActivityMapper extends DeleteCommonMapper<PaidContentActivity>, CommonMapper<PaidContentActivity> {
}