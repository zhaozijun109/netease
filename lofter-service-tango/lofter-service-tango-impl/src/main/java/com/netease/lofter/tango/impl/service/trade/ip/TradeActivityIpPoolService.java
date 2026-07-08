
package com.netease.lofter.tango.impl.service.trade.ip;

import com.netease.lofter.tango.impl.delegate.trade.ip.TradeActivityIpPoolMapperDelegate;
import com.netease.lofter.tango.impl.entity.trade.ip.TradeActivityIpPool;
import com.netease.lofter.tango.impl.web.query.trade.ip.TradeActivityIpPoolQuery;
import com.netease.lofter.tango.impl.web.vo.trade.ip.TradeActivityIpPoolVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.util.BeanUtils;

import com.netease.mm.tk.common.util.BeanConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TradeActivityIpPoolService {

    @Autowired
    private TradeActivityIpPoolMapperDelegate tradeActivityIpPoolDelegate;

    public PageResult<TradeActivityIpPoolVO> listByQuery(TradeActivityIpPoolQuery query) {
        PageResult<TradeActivityIpPoolVO> pageResult = new PageResult<>(query.getPage());
        PageDO<TradeActivityIpPool> pageDO = tradeActivityIpPoolDelegate.listByQuery(query.getCreateTimeBegin(), query.getCreateTimeEnd(),
            query.getId(),
            query.getActId(),
            query.getBusinessId(),
            query.getBindType(),
            query.getIps(),
            query.getStartTime(),
            query.getEndTime(),
            query.getOffset(), query.getLimit());
       return pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));
    }


    public boolean add(TradeActivityIpPoolVO tradeActivityIpPoolVO) {
        if(tradeActivityIpPoolVO.getBindType() == null) {
            tradeActivityIpPoolVO.setBindType(0);
        }
        if(tradeActivityIpPoolVO.getBusinessId() == null) {
            tradeActivityIpPoolVO.setBusinessId(0L);
        }
        TradeActivityIpPool tradeActivityIpPool = BeanConvertUtils.convertBean(tradeActivityIpPoolVO, TradeActivityIpPool.class);
        tradeActivityIpPoolDelegate.insertValue(tradeActivityIpPool);
        return true;
    }

    public boolean delete(Long id) {
        AssertUtils.notNull(id, "id missing");
        int count = tradeActivityIpPoolDelegate.deleteById(id);
        return count == 1;
    }

    public boolean update(TradeActivityIpPoolVO tradeActivityIpPoolVO) {
        AssertUtils.isTrue(tradeActivityIpPoolVO != null && tradeActivityIpPoolVO.getId() != null, "id missing");
        TradeActivityIpPool tradeActivityIpPool = tradeActivityIpPoolDelegate.selectById(tradeActivityIpPoolVO.getId());
        AssertUtils.notNull(tradeActivityIpPool, "id illegal");
        BeanUtils.copyNonNullProperties(tradeActivityIpPoolVO, tradeActivityIpPool);
        tradeActivityIpPoolDelegate.updateValue(tradeActivityIpPool);
        return true;
    }

    private List<TradeActivityIpPoolVO> populate2VOList(List<TradeActivityIpPool> list) {
        return BeanConvertUtils.convertList(list, TradeActivityIpPoolVO.class);
    }


}
