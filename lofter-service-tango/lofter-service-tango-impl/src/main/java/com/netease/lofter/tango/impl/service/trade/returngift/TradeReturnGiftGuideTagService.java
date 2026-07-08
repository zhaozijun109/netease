
package com.netease.lofter.tango.impl.service.trade.returngift;

import com.netease.lofter.tango.impl.delegate.trade.returngift.TradeReturnGiftGuideTagMapperDelegate;
import com.netease.lofter.tango.impl.entity.trade.returngift.TradeReturnGiftGuideTag;
import com.netease.lofter.tango.impl.web.query.trade.returngift.TradeReturnGiftGuideTagQuery;
import com.netease.lofter.tango.impl.web.vo.trade.returngift.TradeReturnGiftGuideTagVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.util.BeanUtils;

import com.netease.mm.tk.common.util.BeanConvertUtils;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TradeReturnGiftGuideTagService {

    @Autowired
    private TradeReturnGiftGuideTagMapperDelegate tradeReturnGiftGuideTagDelegate;

    public PageResult<TradeReturnGiftGuideTagVO> listByQuery(TradeReturnGiftGuideTagQuery query) {
        PageResult<TradeReturnGiftGuideTagVO> pageResult = new PageResult<>(query.getPage());
        PageDO<TradeReturnGiftGuideTag> pageDO = tradeReturnGiftGuideTagDelegate.listByQuery(query.getCreateTimeBegin(), query.getCreateTimeEnd(),
            query.getId(),
            query.getTag(),
            query.getPromotion(),
            query.getStatus(),
            query.getPostType(),
            query.getCreateTime(),
            query.getOffset(), query.getLimit());
       return pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));
    }


    public boolean add(TradeReturnGiftGuideTagVO tradeReturnGiftGuideTagVO) {
        TradeReturnGiftGuideTag tradeReturnGiftGuideTag = BeanConvertUtils.convertBean(tradeReturnGiftGuideTagVO, TradeReturnGiftGuideTag.class);
        tradeReturnGiftGuideTag.setCreateTime(System.currentTimeMillis());
        tradeReturnGiftGuideTag.setStatus(TradeReturnGiftGuideTag.STATUS_OFFLINE);
        tradeReturnGiftGuideTagDelegate.insertValue(tradeReturnGiftGuideTag);
        return true;
    }

    public boolean delete(Long id) {
        AssertUtils.notNull(id, "id missing");
        int count = tradeReturnGiftGuideTagDelegate.deleteById(id);
        return count == 1;
    }

    public boolean update(TradeReturnGiftGuideTagVO tradeReturnGiftGuideTagVO) {
        AssertUtils.isTrue(tradeReturnGiftGuideTagVO != null && tradeReturnGiftGuideTagVO.getId() != null, "id missing");
        TradeReturnGiftGuideTag tradeReturnGiftGuideTag = tradeReturnGiftGuideTagDelegate.selectById(tradeReturnGiftGuideTagVO.getId());
        AssertUtils.notNull(tradeReturnGiftGuideTag, "id illegal");
        BeanUtils.copyNonNullProperties(tradeReturnGiftGuideTagVO, tradeReturnGiftGuideTag);
        tradeReturnGiftGuideTagDelegate.updateValue(tradeReturnGiftGuideTag);
        return true;
    }

    public boolean updateStatus(Long id, Integer status) {
        AssertUtils.isTrue(id != null && status != null, "id or status missing");
        TradeReturnGiftGuideTag tradeReturnGiftGuideTag = tradeReturnGiftGuideTagDelegate.selectById(id);
        AssertUtils.notNull(tradeReturnGiftGuideTag, "id illegal");

        SqlUpdate update = new SqlUpdate().setLiteral("status", status).whenEquals("id", id).andEquals("status", tradeReturnGiftGuideTag.getStatus());
        return tradeReturnGiftGuideTagDelegate.update(update, null) == 1;
    }

    private List<TradeReturnGiftGuideTagVO> populate2VOList(List<TradeReturnGiftGuideTag> list) {
        return BeanConvertUtils.convertList(list, TradeReturnGiftGuideTagVO.class);
    }


}
