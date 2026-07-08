
package com.netease.lofter.tango.impl.service.trade.exchangecoupon;

import com.netease.lofter.tango.impl.delegate.trade.exchangecoupon.TradeReturnGiftExchangeRecordMapperDelegate;
import com.netease.lofter.tango.impl.entity.trade.exchangecoupon.TradeReturnGiftExchangeRecord;
import com.netease.lofter.tango.impl.web.query.trade.exchangecoupon.TradeReturnGiftExchangeRecordQuery;
import com.netease.lofter.tango.impl.web.vo.trade.exchangecoupon.TradeReturnGiftExchangeRecordVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.util.BeanUtils;

import com.netease.mm.tk.common.util.BeanConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TradeReturnGiftExchangeRecordService {

    @Autowired
    private TradeReturnGiftExchangeRecordMapperDelegate tradeReturnGiftExchangeRecordDelegate;

    public PageResult<TradeReturnGiftExchangeRecordVO> listByQuery(TradeReturnGiftExchangeRecordQuery query) {
        PageResult<TradeReturnGiftExchangeRecordVO> pageResult = new PageResult<>(query.getPage());
        PageDO<TradeReturnGiftExchangeRecord> pageDO = tradeReturnGiftExchangeRecordDelegate.listByQuery(query.getCreateTimeBegin(), query.getCreateTimeEnd(),
            query.getId(),
            query.getUserId(),
            query.getCouponId(),
            query.getPostId(),
            query.getStatus(),
            query.getExchangeTime(),
            query.getStartIndex(),
            query.getCreateTime(),
            query.getExt(),
            query.getBlogId(),
            query.getPlanId(),
            query.getRelatedId(),
            query.getExchangeGiftId(),
            query.getType(),
            query.getParentOrderId(),
            query.getParentOrderType(),
            query.getScene(),
            query.getNeedSettle(),
            query.getSignType(),
            query.getUnitAmount(),
            query.getOffset(), query.getLimit());
       return pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));
    }


    public boolean add(TradeReturnGiftExchangeRecordVO tradeReturnGiftExchangeRecordVO) {
        TradeReturnGiftExchangeRecord tradeReturnGiftExchangeRecord = BeanConvertUtils.convertBean(tradeReturnGiftExchangeRecordVO, TradeReturnGiftExchangeRecord.class);
        tradeReturnGiftExchangeRecord.setCreateTime(System.currentTimeMillis());
        tradeReturnGiftExchangeRecordDelegate.insertValue(tradeReturnGiftExchangeRecord);
        return true;
    }

    public boolean delete(Long id) {
        AssertUtils.notNull(id, "id missing");
        int count = tradeReturnGiftExchangeRecordDelegate.deleteById(id);
        return count == 1;
    }

    public boolean update(TradeReturnGiftExchangeRecordVO tradeReturnGiftExchangeRecordVO) {
        AssertUtils.isTrue(tradeReturnGiftExchangeRecordVO != null && tradeReturnGiftExchangeRecordVO.getId() != null, "id missing");
        TradeReturnGiftExchangeRecord tradeReturnGiftExchangeRecord = tradeReturnGiftExchangeRecordDelegate.selectById(tradeReturnGiftExchangeRecordVO.getId());
        AssertUtils.notNull(tradeReturnGiftExchangeRecord, "id illegal");
        BeanUtils.copyNonNullProperties(tradeReturnGiftExchangeRecordVO, tradeReturnGiftExchangeRecord);
        return true;
    }

    private List<TradeReturnGiftExchangeRecordVO> populate2VOList(List<TradeReturnGiftExchangeRecord> list) {
        return BeanConvertUtils.convertList(list, TradeReturnGiftExchangeRecordVO.class);
    }


}
