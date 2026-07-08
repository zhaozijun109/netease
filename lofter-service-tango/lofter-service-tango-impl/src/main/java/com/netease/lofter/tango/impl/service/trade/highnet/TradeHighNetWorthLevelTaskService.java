
package com.netease.lofter.tango.impl.service.trade.highnet;

import com.netease.lofter.tango.impl.delegate.trade.highnet.TradeHighNetWorthLevelTaskMapperDelegate;
import com.netease.lofter.tango.impl.entity.trade.highnet.TradeHighNetWorthLevelTask;
import com.netease.lofter.tango.impl.web.query.trade.highnet.TradeHighNetWorthLevelTaskQuery;
import com.netease.lofter.tango.impl.web.vo.trade.highnet.TradeHighNetWorthLevelTaskVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.util.BeanUtils;

import com.netease.mm.tk.common.util.BeanConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TradeHighNetWorthLevelTaskService {

    @Autowired
    private TradeHighNetWorthLevelTaskMapperDelegate tradeHighNetWorthLevelTaskDelegate;

    public PageResult<TradeHighNetWorthLevelTaskVO> listByQuery(TradeHighNetWorthLevelTaskQuery query) {
        PageResult<TradeHighNetWorthLevelTaskVO> pageResult = new PageResult<>(query.getPage());
        PageDO<TradeHighNetWorthLevelTask> pageDO = tradeHighNetWorthLevelTaskDelegate.listByQuery(query.getCreateTimeBegin(), query.getCreateTimeEnd(),
            query.getId(),
            query.getCrownId(),
            query.getType(),
            query.getLevel(),
            query.getTargetValue(),
            query.getRightInfoJson(),
            query.getStatus(),
            query.getCreateTime(),
            query.getOffset(), query.getLimit());
       return pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));
    }


    public boolean add(TradeHighNetWorthLevelTaskVO tradeHighNetWorthLevelTaskVO) {
        TradeHighNetWorthLevelTask tradeHighNetWorthLevelTask = BeanConvertUtils.convertBean(tradeHighNetWorthLevelTaskVO, TradeHighNetWorthLevelTask.class);
        tradeHighNetWorthLevelTask.setCreateTime(System.currentTimeMillis());
        tradeHighNetWorthLevelTaskDelegate.insertValue(tradeHighNetWorthLevelTask);
        return true;
    }

    public boolean delete(Long id) {
        AssertUtils.notNull(id, "id missing");
        int count = tradeHighNetWorthLevelTaskDelegate.updateStatus(id,-1);
        return count == 1;
    }

    public boolean update(TradeHighNetWorthLevelTaskVO tradeHighNetWorthLevelTaskVO) {
        AssertUtils.isTrue(tradeHighNetWorthLevelTaskVO != null && tradeHighNetWorthLevelTaskVO.getId() != null, "id missing");
        TradeHighNetWorthLevelTask tradeHighNetWorthLevelTask = tradeHighNetWorthLevelTaskDelegate.selectById(tradeHighNetWorthLevelTaskVO.getId());
        AssertUtils.notNull(tradeHighNetWorthLevelTask, "id illegal");
        BeanUtils.copyNonNullProperties(tradeHighNetWorthLevelTaskVO, tradeHighNetWorthLevelTask);
        tradeHighNetWorthLevelTaskDelegate.updateValue(tradeHighNetWorthLevelTask);
        return true;
    }

    private List<TradeHighNetWorthLevelTaskVO> populate2VOList(List<TradeHighNetWorthLevelTask> list) {
        return BeanConvertUtils.convertList(list, TradeHighNetWorthLevelTaskVO.class);
    }


}
