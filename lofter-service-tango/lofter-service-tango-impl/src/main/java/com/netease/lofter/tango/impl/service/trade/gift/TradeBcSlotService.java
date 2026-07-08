
package com.netease.lofter.tango.impl.service.trade.gift;

import com.netease.lofter.tango.impl.delegate.trade.gift.TradeBcSlotMapperDelegate;
import com.netease.lofter.tango.impl.entity.trade.gift.TradeBcSlot;
import com.netease.lofter.tango.impl.entity.trade.returngift.TradeReturnGiftGuideRule;
import com.netease.lofter.tango.impl.web.query.trade.gift.TradeBcSlotQuery;
import com.netease.lofter.tango.impl.web.vo.trade.gift.TradeBcSlotVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.util.BeanUtils;

import com.netease.mm.tk.common.util.BeanConvertUtils;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlUpdate;
import com.netease.yaolu.lofter.core.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class TradeBcSlotService {

    @Autowired
    private TradeBcSlotMapperDelegate tradeBcSlotDelegate;

    public PageResult<TradeBcSlotVO> listByQuery(TradeBcSlotQuery query) {
        PageResult<TradeBcSlotVO> pageResult = new PageResult<>(query.getPage());
        PageDO<TradeBcSlot> pageDO = tradeBcSlotDelegate.listByQuery(query.getCreateTimeBegin(), query.getCreateTimeEnd(),
            query.getId(),
            query.getCrowdIds(),
            query.getType(),
            query.getPriority(),
            query.getBanner(),
            query.getTargetUrl(),
            query.getStartTime(),
            query.getEndTime(),
            query.getStatus(),
            query.getCreateTime(),
            query.getOffset(), query.getLimit());
       return pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));
    }


    public boolean add(TradeBcSlotVO tradeBcSlotVO) {
        TradeBcSlot tradeBcSlot = BeanConvertUtils.convertBean(tradeBcSlotVO, TradeBcSlot.class);
        tradeBcSlot.setCreateTime(System.currentTimeMillis());
        tradeBcSlot.setBanner(JsonUtils.toJsonString(tradeBcSlotVO.getBannerList()));
        tradeBcSlot.setStatus(TradeBcSlot.STATUS_INIT);
        tradeBcSlotDelegate.insertValue(tradeBcSlot);
        return true;
    }

    public boolean delete(Long id) {
        AssertUtils.notNull(id, "id missing");
        int count = tradeBcSlotDelegate.deleteById(id);
        return count == 1;
    }

    public boolean update(TradeBcSlotVO tradeBcSlotVO) {
        AssertUtils.isTrue(tradeBcSlotVO != null && tradeBcSlotVO.getId() != null, "id missing");
        TradeBcSlot tradeBcSlot = tradeBcSlotDelegate.selectById(tradeBcSlotVO.getId());
        AssertUtils.notNull(tradeBcSlot, "id illegal");
        BeanUtils.copyNonNullProperties(tradeBcSlotVO, tradeBcSlot);
        tradeBcSlot.setBanner(JsonUtils.toJsonString(tradeBcSlotVO.getBannerList()));
        tradeBcSlotDelegate.updateValue(tradeBcSlot);
        return true;
    }

    public boolean updateStatus(Long id, Integer status) {
        AssertUtils.isTrue(id != null && status != null, "id or status missing");
        TradeBcSlot slot = tradeBcSlotDelegate.selectById(id);
        AssertUtils.notNull(slot, "id illegal");

        SqlUpdate update = new SqlUpdate().setLiteral("status", status).whenEquals("id", id).andEquals("status", slot.getStatus());
        return tradeBcSlotDelegate.update(update, null) == 1;
    }


    private List<TradeBcSlotVO> populate2VOList(List<TradeBcSlot> list) {
        List<TradeBcSlotVO> vos = new ArrayList<>();
        list.forEach(item -> {
            TradeBcSlotVO vo = BeanConvertUtils.convertBean(item, TradeBcSlotVO.class);
            vo.setBannerList(JsonUtils.parseToList(item.getBanner(), TradeBcSlotVO.BannerVO.class));
            vos.add(vo);
        });
        return vos;
    }
}
