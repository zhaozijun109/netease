package com.netease.lofter.tango.impl.mapper.trade.gift;

import com.netease.yaolu.commons.datasource.dynamic.DataSource;
import org.apache.ibatis.annotations.Mapper;
import com.netease.yaolu.commons.spring.mybatis.mapper.CommonMapper;
import com.netease.yaolu.commons.spring.mybatis.mapper.DeleteCommonMapper;

import com.netease.lofter.tango.impl.entity.trade.gift.TradeBcSlot;

@Mapper
@DataSource(com.netease.lofter.tango.impl.config.DbConfig.MAIN_DATASOURCE)
public interface TradeBcSlotMapper extends CommonMapper<TradeBcSlot>, DeleteCommonMapper<TradeBcSlot>{
}