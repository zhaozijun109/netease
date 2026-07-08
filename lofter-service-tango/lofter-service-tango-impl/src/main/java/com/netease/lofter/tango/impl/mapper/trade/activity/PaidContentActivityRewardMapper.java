package com.netease.lofter.tango.impl.mapper.trade.activity;

import com.netease.yaolu.commons.datasource.dynamic.DataSource;
import org.apache.ibatis.annotations.Mapper;
import com.netease.yaolu.commons.spring.mybatis.mapper.CommonMapper;
import com.netease.yaolu.commons.spring.mybatis.mapper.DeleteCommonMapper;

import com.netease.lofter.tango.impl.entity.trade.activity.PaidContentActivityReward;

@Mapper
@DataSource(com.netease.lofter.tango.impl.config.DbConfig.MAIN_DATASOURCE)
public interface PaidContentActivityRewardMapper extends CommonMapper<PaidContentActivityReward>, DeleteCommonMapper<PaidContentActivityReward>{
}