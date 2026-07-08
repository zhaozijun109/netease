package com.netease.lofter.tango.impl.delegate.trade.exchangecoupon;

import com.netease.lofter.tango.impl.entity.trade.exchangecoupon.TradeUserExchangeCoupon;
import com.netease.lofter.tango.impl.mapper.trade.exchangecoupon.TradeUserExchangeCouponMapper;
import com.netease.yaolu.commons.spring.mybatis.service.CommonService;
import com.netease.yaolu.commons.spring.mybatis.service.DeleteCommonService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class TradeUserExchangeCouponDelegate implements CommonService<TradeUserExchangeCouponMapper, TradeUserExchangeCoupon>, DeleteCommonService<TradeUserExchangeCouponMapper, TradeUserExchangeCoupon> {
}
