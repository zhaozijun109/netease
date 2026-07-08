
package com.netease.lofter.tango.impl.service.trade.exchangecoupon;

import com.netease.lofter.tango.impl.delegate.trade.exchangecoupon.TradeExchangeCouponMapperDelegate;
import com.netease.lofter.tango.impl.entity.trade.exchangecoupon.TradeExchangeCoupon;
import com.netease.lofter.tango.impl.web.query.trade.exchangecoupon.TradeExchangeCouponQuery;
import com.netease.lofter.tango.impl.web.vo.trade.exchangecoupon.TradeExchangeCouponVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.util.BeanUtils;

import com.netease.mm.tk.common.util.BeanConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TradeExchangeCouponService {

    @Autowired
    private TradeExchangeCouponMapperDelegate tradeExchangeCouponDelegate;

    public PageResult<TradeExchangeCouponVO> listByQuery(TradeExchangeCouponQuery query) {
        PageResult<TradeExchangeCouponVO> pageResult = new PageResult<>(query.getPage());
        PageDO<TradeExchangeCoupon> pageDO = tradeExchangeCouponDelegate.listByQuery(query.getCreateTimeBegin(), query.getCreateTimeEnd(),
            query.getId(),
            query.getName(),
            query.getScope(),
            query.getIcon(),
            query.getStatus(),
            query.getCreateTime(),
            query.getOffset(), query.getLimit());
       return pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));
    }


    public boolean add(TradeExchangeCouponVO tradeExchangeCouponVO) {
        TradeExchangeCoupon tradeExchangeCoupon = BeanConvertUtils.convertBean(tradeExchangeCouponVO, TradeExchangeCoupon.class);
        tradeExchangeCoupon.setCreateTime(System.currentTimeMillis());
        tradeExchangeCouponDelegate.insertValue(tradeExchangeCoupon);
        return true;
    }

    public boolean delete(Long id) {
        AssertUtils.notNull(id, "id missing");
        int count = tradeExchangeCouponDelegate.deleteById(id);
        return count == 1;
    }

    public boolean update(TradeExchangeCouponVO tradeExchangeCouponVO) {
        AssertUtils.isTrue(tradeExchangeCouponVO != null && tradeExchangeCouponVO.getId() != null, "id missing");
        TradeExchangeCoupon tradeExchangeCoupon = tradeExchangeCouponDelegate.selectById(tradeExchangeCouponVO.getId());
        AssertUtils.notNull(tradeExchangeCoupon, "id illegal");
        BeanUtils.copyNonNullProperties(tradeExchangeCouponVO, tradeExchangeCoupon);
        return true;
    }

    private List<TradeExchangeCouponVO> populate2VOList(List<TradeExchangeCoupon> list) {
        return BeanConvertUtils.convertList(list, TradeExchangeCouponVO.class);
    }


}
