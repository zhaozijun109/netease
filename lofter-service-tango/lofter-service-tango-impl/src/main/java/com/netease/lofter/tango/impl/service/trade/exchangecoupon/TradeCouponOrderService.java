
package com.netease.lofter.tango.impl.service.trade.exchangecoupon;

import com.netease.lofter.tango.impl.delegate.trade.exchangecoupon.TradeCouponOrderMapperDelegate;
import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.entity.trade.exchangecoupon.TradeCouponOrder;
import com.netease.lofter.tango.impl.web.query.trade.exchangecoupon.TradeCouponOrderQuery;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.trade.exchangecoupon.TradeCouponOrderVO;
import com.netease.mm.tk.common.util.BeanConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TradeCouponOrderService {

    @Autowired
    private TradeCouponOrderMapperDelegate TradeCouponOrderDelegate;

    public PageResult<TradeCouponOrderVO> listByQuery(TradeCouponOrderQuery query) {
        PageResult<TradeCouponOrderVO> pageResult = new PageResult<>(query.getPage());
        PageDO<TradeCouponOrder> pageDO = TradeCouponOrderDelegate.listByQuery(query.getCreateTimeBegin(), query.getCreateTimeEnd(),
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

    private List<TradeCouponOrderVO> populate2VOList(List<TradeCouponOrder> list) {
        return BeanConvertUtils.convertList(list, TradeCouponOrderVO.class);
    }
}
