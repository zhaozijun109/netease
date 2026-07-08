package com.netease.lofter.tango.impl.mapper.trade.exchangecoupon;

import com.netease.lofter.tango.impl.entity.trade.exchangecoupon.TradeCouponOrder;
import com.netease.yaolu.commons.datasource.dynamic.DataSource;
import com.netease.yaolu.commons.spring.mybatis.mapper.CommonMapper;
import com.netease.yaolu.commons.spring.mybatis.mapper.DeleteCommonMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DataSource(com.netease.lofter.tango.impl.config.DbConfig.MAIN_DATASOURCE)
public interface TradeCouponOrderMapper extends CommonMapper<TradeCouponOrder>, DeleteCommonMapper<TradeCouponOrder>{
}