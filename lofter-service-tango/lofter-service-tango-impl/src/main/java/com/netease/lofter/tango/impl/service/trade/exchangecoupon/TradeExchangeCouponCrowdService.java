
package com.netease.lofter.tango.impl.service.trade.exchangecoupon;

import com.netease.lofter.tango.impl.delegate.trade.exchangecoupon.TradeExchangeCouponCrowdMapperDelegate;
import com.netease.lofter.tango.impl.entity.trade.exchangecoupon.TradeExchangeCouponCrowd;
import com.netease.lofter.tango.impl.web.query.trade.exchangecoupon.TradeExchangeCouponCrowdQuery;
import com.netease.lofter.tango.impl.web.vo.trade.exchangecoupon.TradeExchangeCouponCrowdVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.util.BeanUtils;

import com.netease.mm.tk.common.util.BeanConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TradeExchangeCouponCrowdService {

    @Autowired
    private TradeExchangeCouponCrowdMapperDelegate tradeExchangeCouponCrowdDelegate;

    public PageResult<TradeExchangeCouponCrowdVO> listByQuery(TradeExchangeCouponCrowdQuery query) {
        PageResult<TradeExchangeCouponCrowdVO> pageResult = new PageResult<>(query.getPage());
        PageDO<TradeExchangeCouponCrowd> pageDO = tradeExchangeCouponCrowdDelegate.listByQuery(query.getCreateTimeBegin(), query.getCreateTimeEnd(),
            query.getId(),
            query.getSkuId(),
            query.getCrowdId(),
            query.getCreateTime(),
            query.getOffset(), query.getLimit());
       return pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));
    }


    public boolean add(TradeExchangeCouponCrowdVO tradeExchangeCouponCrowdVO) {
        TradeExchangeCouponCrowd tradeExchangeCouponCrowd = BeanConvertUtils.convertBean(tradeExchangeCouponCrowdVO, TradeExchangeCouponCrowd.class);
        tradeExchangeCouponCrowd.setCreateTime(System.currentTimeMillis());
        tradeExchangeCouponCrowdDelegate.insertValue(tradeExchangeCouponCrowd);
        return true;
    }

    public boolean delete(Long id) {
        AssertUtils.notNull(id, "id missing");
        int count = tradeExchangeCouponCrowdDelegate.deleteById(id);
        return count == 1;
    }

    public boolean update(TradeExchangeCouponCrowdVO tradeExchangeCouponCrowdVO) {
        AssertUtils.isTrue(tradeExchangeCouponCrowdVO != null && tradeExchangeCouponCrowdVO.getId() != null, "id missing");
        TradeExchangeCouponCrowd tradeExchangeCouponCrowd = tradeExchangeCouponCrowdDelegate.selectById(tradeExchangeCouponCrowdVO.getId());
        AssertUtils.notNull(tradeExchangeCouponCrowd, "id illegal");
        BeanUtils.copyNonNullProperties(tradeExchangeCouponCrowdVO, tradeExchangeCouponCrowd);
        return true;
    }

    private List<TradeExchangeCouponCrowdVO> populate2VOList(List<TradeExchangeCouponCrowd> list) {
        return BeanConvertUtils.convertList(list, TradeExchangeCouponCrowdVO.class);
    }


}
