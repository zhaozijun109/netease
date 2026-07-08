package com.netease.lofter.tango.impl.mapper.trade.slot;

import com.netease.lofter.tango.impl.entity.trade.slot.LuckPrize;
import com.netease.yaolu.commons.datasource.dynamic.DataSource;
import com.netease.yaolu.commons.spring.mybatis.mapper.CommonMapper;
import com.netease.yaolu.commons.spring.mybatis.mapper.DeleteCommonMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DataSource(com.netease.lofter.tango.impl.config.DbConfig.YAOLU_ACTIVITY_DATASOURCE)
public interface LuckPrizeMapper extends DeleteCommonMapper<LuckPrize>, CommonMapper<LuckPrize> {
}