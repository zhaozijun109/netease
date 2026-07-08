
package com.netease.lofter.tango.impl.service.trade.exchangecoupon;

import com.netease.lofter.tango.impl.delegate.trade.exchangecoupon.TradeCouponCardOrderMapperDelegate;
import com.netease.lofter.tango.impl.entity.trade.exchangecoupon.TradeCouponCardOrder;
import com.netease.lofter.tango.impl.web.query.trade.exchangecoupon.TradeCouponCardOrderQuery;
import com.netease.lofter.tango.impl.web.vo.trade.exchangecoupon.TradeCouponCardOrderVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;

import com.netease.mm.tk.common.util.BeanConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TradeCouponCardOrderService {

    @Autowired
    private TradeCouponCardOrderMapperDelegate tradeCouponCardOrderDelegate;

    public PageResult<TradeCouponCardOrderVO> listByQuery(TradeCouponCardOrderQuery query) {
        PageResult<TradeCouponCardOrderVO> pageResult = new PageResult<>(query.getPage());
        PageDO<TradeCouponCardOrder> pageDO = tradeCouponCardOrderDelegate.listByQuery(query.getCreateTimeBegin(), query.getCreateTimeEnd(),
            query.getId(),
            query.getTradeId(),
            query.getUserId(),
            query.getStatus(),
            query.getPlatform(),
            query.getPayType(),
            query.getAmount(),
            query.getFee(),
            query.getChannelDivision(),
            query.getCreateTime(),
            query.getFinishTime(),
            query.getProductId(),
            query.getBankOrderSn(),
            query.getBankOrderTime(),
            query.getOffset(), query.getLimit());
       return pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));
    }

    private List<TradeCouponCardOrderVO> populate2VOList(List<TradeCouponCardOrder> list) {
        return BeanConvertUtils.convertList(list, TradeCouponCardOrderVO.class);
    }
}
