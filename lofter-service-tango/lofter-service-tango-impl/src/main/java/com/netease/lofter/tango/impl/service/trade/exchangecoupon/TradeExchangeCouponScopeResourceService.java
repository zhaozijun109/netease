
package com.netease.lofter.tango.impl.service.trade.exchangecoupon;

import com.netease.lofter.tango.impl.delegate.trade.exchangecoupon.TradeExchangeCouponScopeResourceMapperDelegate;
import com.netease.lofter.tango.impl.entity.trade.exchangecoupon.TradeExchangeCouponScopeResource;
import com.netease.lofter.tango.impl.web.query.trade.exchangecoupon.TradeExchangeCouponScopeResourceQuery;
import com.netease.lofter.tango.impl.web.vo.trade.exchangecoupon.TradeExchangeCouponScopeResourceVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.util.BeanUtils;

import com.netease.mm.tk.common.util.BeanConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TradeExchangeCouponScopeResourceService {

    @Autowired
    private TradeExchangeCouponScopeResourceMapperDelegate tradeExchangeCouponScopeResourceDelegate;

    public PageResult<TradeExchangeCouponScopeResourceVO> listByQuery(TradeExchangeCouponScopeResourceQuery query) {
        PageResult<TradeExchangeCouponScopeResourceVO> pageResult = new PageResult<>(query.getPage());
        PageDO<TradeExchangeCouponScopeResource> pageDO = tradeExchangeCouponScopeResourceDelegate.listByQuery(query.getCreateTimeBegin(), query.getCreateTimeEnd(),
            query.getId(),
            query.getCouponId(),
            query.getResourceId(),
            query.getStatus(),
            query.getCreateTime(),
            query.getOffset(), query.getLimit());
       return pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));
    }


    public boolean add(TradeExchangeCouponScopeResourceVO tradeExchangeCouponScopeResourceVO) {
        TradeExchangeCouponScopeResource tradeExchangeCouponScopeResource = BeanConvertUtils.convertBean(tradeExchangeCouponScopeResourceVO, TradeExchangeCouponScopeResource.class);
        tradeExchangeCouponScopeResource.setCreateTime(System.currentTimeMillis());
        tradeExchangeCouponScopeResourceDelegate.insertValue(tradeExchangeCouponScopeResource);
        return true;
    }

    public boolean delete(Long id) {
        AssertUtils.notNull(id, "id missing");
        int count = tradeExchangeCouponScopeResourceDelegate.deleteById(id);
        return count == 1;
    }

    public boolean update(TradeExchangeCouponScopeResourceVO tradeExchangeCouponScopeResourceVO) {
        AssertUtils.isTrue(tradeExchangeCouponScopeResourceVO != null && tradeExchangeCouponScopeResourceVO.getId() != null, "id missing");
        TradeExchangeCouponScopeResource tradeExchangeCouponScopeResource = tradeExchangeCouponScopeResourceDelegate.selectById(tradeExchangeCouponScopeResourceVO.getId());
        AssertUtils.notNull(tradeExchangeCouponScopeResource, "id illegal");
        BeanUtils.copyNonNullProperties(tradeExchangeCouponScopeResourceVO, tradeExchangeCouponScopeResource);
        return true;
    }

    private List<TradeExchangeCouponScopeResourceVO> populate2VOList(List<TradeExchangeCouponScopeResource> list) {
        return BeanConvertUtils.convertList(list, TradeExchangeCouponScopeResourceVO.class);
    }


}
