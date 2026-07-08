package com.netease.lofter.tango.impl.service.trade.exchangecoupon;

import com.google.common.collect.Lists;
import com.netease.lofter.tango.impl.delegate.trade.exchangecoupon.TradeUserExchangeCouponDelegate;
import com.netease.lofter.tango.impl.entity.trade.exchangecoupon.TradeUserExchangeCoupon;
import com.netease.lofter.tango.impl.web.query.trade.exchangecoupon.TradeUserExchangeCouponQuery;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.trade.exchangecoupon.TradeUserExchangeCouponVO;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.netease.mm.tk.common.util.BeanConvertUtils;

import java.util.List;

@Service
public class TradeUserExchangeCouponService {
    @Autowired
    private TradeUserExchangeCouponDelegate exchangeCouponDelegate;

    public PageResult<TradeUserExchangeCouponVO> listByQuery(TradeUserExchangeCouponQuery query) {
        PageResult<TradeUserExchangeCouponVO> pageResult = new PageResult<>(query.getPage());

        SqlSelect queryCoupon = new SqlSelect();
        if (query.getTradeId() != null) {
            queryCoupon.andEquals("tradeId", query.getTradeId());
        }

        if (query.getUserId() != null) {
            queryCoupon.andEquals("userId", query.getUserId());
        }

        if (query.getType() != null  && query.getScene() != null && query.getScene() == 9 && query.getType() == 7) {
            queryCoupon.andIn("type", Lists.newArrayList(7, 12));
            queryCoupon.andIn("scene", Lists.newArrayList(9,13,14));
        } else {
            if (query.getType() != null) {
                queryCoupon.andEquals("type", query.getType());
            }

            if (query.getScene() != null) {
                queryCoupon.andEquals("scene", query.getScene());
            }
        }

        int count = exchangeCouponDelegate.count(queryCoupon);

        queryCoupon.offset(query.getOffset()).limit(query.getLimit());
        List<TradeUserExchangeCoupon> couponList = exchangeCouponDelegate.selectListNoLimit(queryCoupon);
        List<TradeUserExchangeCouponVO> res = BeanConvertUtils.convertList(couponList, TradeUserExchangeCouponVO.class);
        return pageResult.total(count).list(res);
    }

}
