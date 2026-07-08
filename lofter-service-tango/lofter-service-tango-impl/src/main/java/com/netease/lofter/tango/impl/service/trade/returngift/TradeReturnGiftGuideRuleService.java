
package com.netease.lofter.tango.impl.service.trade.returngift;

import com.netease.lofter.tango.impl.delegate.trade.returngift.TradeReturnGiftGuideRuleMapperDelegate;
import com.netease.lofter.tango.impl.entity.trade.returngift.TradeReturnGiftGuideRule;
import com.netease.lofter.tango.impl.entity.trade.returngift.TradeReturnGiftGuideTag;
import com.netease.lofter.tango.impl.web.query.trade.returngift.TradeReturnGiftGuideRuleQuery;
import com.netease.lofter.tango.impl.web.vo.trade.returngift.TradeReturnGiftGuideRuleVO;

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
public class TradeReturnGiftGuideRuleService {

    @Autowired
    private TradeReturnGiftGuideRuleMapperDelegate tradeReturnGiftGuideRuleDelegate;

    public PageResult<TradeReturnGiftGuideRuleVO> listByQuery(TradeReturnGiftGuideRuleQuery query) {
        PageResult<TradeReturnGiftGuideRuleVO> pageResult = new PageResult<>(query.getPage());
        PageDO<TradeReturnGiftGuideRule> pageDO = tradeReturnGiftGuideRuleDelegate.listByQuery(query.getCreateTimeBegin(), query.getCreateTimeEnd(),
            query.getId(),
            query.getRuleType(),
            query.getPostType(),
            query.getTip(),
            query.getTipImg(),
            query.getRule(),
            query.getStatus(),
            query.getCreateTime(),
            query.getOffset(), query.getLimit());
       return pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));
    }


    public boolean add(TradeReturnGiftGuideRuleVO tradeReturnGiftGuideRuleVO) {
        TradeReturnGiftGuideRule tradeReturnGiftGuideRule = BeanConvertUtils.convertBean(tradeReturnGiftGuideRuleVO, TradeReturnGiftGuideRule.class);
        tradeReturnGiftGuideRule.setCreateTime(System.currentTimeMillis());
        tradeReturnGiftGuideRule.setStatus(TradeReturnGiftGuideRule.STATUS_OFFLINE);
        tradeReturnGiftGuideRule.setPostType(0);

        if (tradeReturnGiftGuideRule.getJudgmentResult() == null) {
            tradeReturnGiftGuideRule.setJudgmentResult("");
        }

        if (tradeReturnGiftGuideRule.getTipImg() == null) {
            tradeReturnGiftGuideRule.setTipImg("");
        }

        if (tradeReturnGiftGuideRule.getRuleDesc() == null) {
            tradeReturnGiftGuideRule.setRuleDesc("");
        }

        tradeReturnGiftGuideRuleDelegate.insertValue(tradeReturnGiftGuideRule);
        return true;
    }

    public boolean delete(Long id) {
        AssertUtils.notNull(id, "id missing");
        int count = tradeReturnGiftGuideRuleDelegate.deleteById(id);
        return count == 1;
    }

    public boolean update(TradeReturnGiftGuideRuleVO tradeReturnGiftGuideRuleVO) {
        AssertUtils.isTrue(tradeReturnGiftGuideRuleVO != null && tradeReturnGiftGuideRuleVO.getId() != null, "id missing");
        TradeReturnGiftGuideRule tradeReturnGiftGuideRule = tradeReturnGiftGuideRuleDelegate.selectById(tradeReturnGiftGuideRuleVO.getId());
        AssertUtils.notNull(tradeReturnGiftGuideRule, "id illegal");
        BeanUtils.copyNonNullProperties(tradeReturnGiftGuideRuleVO, tradeReturnGiftGuideRule);
        tradeReturnGiftGuideRule.setStatus(TradeReturnGiftGuideRule.STATUS_OFFLINE);

        if (tradeReturnGiftGuideRule.getJudgmentResult() == null) {
            tradeReturnGiftGuideRule.setJudgmentResult("");
        }

        if (tradeReturnGiftGuideRule.getTipImg() == null) {
            tradeReturnGiftGuideRule.setTipImg("");
        }

        if (tradeReturnGiftGuideRule.getRuleDesc() == null) {
            tradeReturnGiftGuideRule.setRuleDesc("");
        }

        tradeReturnGiftGuideRuleDelegate.updateValue(tradeReturnGiftGuideRule);
        return true;
    }

    private List<TradeReturnGiftGuideRuleVO> populate2VOList(List<TradeReturnGiftGuideRule> list) {
        return BeanConvertUtils.convertList(list, TradeReturnGiftGuideRuleVO.class);
    }


    public boolean updateStatus(Long id, Integer status) {
        AssertUtils.isTrue(id != null && status != null, "id or status missing");
        TradeReturnGiftGuideRule rule = tradeReturnGiftGuideRuleDelegate.selectById(id);
        AssertUtils.notNull(rule, "id illegal");

        SqlUpdate update = new SqlUpdate().setLiteral("status", status).whenEquals("id", id).andEquals("status", rule.getStatus());
        return tradeReturnGiftGuideRuleDelegate.update(update, null) == 1;
    }


}
