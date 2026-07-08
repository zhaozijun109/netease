package com.netease.lofter.tango.impl.mapper.trade.returngift;

import com.netease.yaolu.commons.datasource.dynamic.DataSource;
import org.apache.ibatis.annotations.Mapper;
import com.netease.yaolu.commons.spring.mybatis.mapper.CommonMapper;
import com.netease.yaolu.commons.spring.mybatis.mapper.DeleteCommonMapper;

import com.netease.lofter.tango.impl.entity.trade.returngift.TradeReturnGiftGuideRule;

@Mapper
@DataSource(com.netease.lofter.tango.impl.config.DbConfig.MAIN_DATASOURCE)
public interface TradeReturnGiftGuideRuleMapper extends CommonMapper<TradeReturnGiftGuideRule>, DeleteCommonMapper<TradeReturnGiftGuideRule>{
}