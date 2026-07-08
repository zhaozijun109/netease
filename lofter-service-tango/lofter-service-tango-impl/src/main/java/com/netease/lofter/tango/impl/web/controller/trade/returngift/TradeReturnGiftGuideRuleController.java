package com.netease.lofter.tango.impl.web.controller.trade.returngift;

import com.netease.lofter.acl.sdk.annotation.ACLResource;
import com.netease.lofter.tango.impl.service.trade.returngift.TradeReturnGiftGuideRuleService;
import com.netease.lofter.tango.impl.web.query.trade.returngift.TradeReturnGiftGuideRuleQuery;
import com.netease.lofter.tango.impl.web.vo.trade.returngift.TradeReturnGiftGuideRuleVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;

import com.netease.lofter.tango.impl.web.vo.trade.returngift.TradeReturnGiftGuideTagVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/tango/tradeReturnGiftGuideRule")
@ACLResource(roles = "GIFT")
public class TradeReturnGiftGuideRuleController {

    @Autowired
    private TradeReturnGiftGuideRuleService tradeReturnGiftGuideRuleService;

    @PostMapping("/list")
    public Result<PageResult<TradeReturnGiftGuideRuleVO>> listByQuery(@RequestBody @Validated TradeReturnGiftGuideRuleQuery tradeReturnGiftGuideRuleQuery) {
        return Result.success(tradeReturnGiftGuideRuleService.listByQuery(tradeReturnGiftGuideRuleQuery));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated TradeReturnGiftGuideRuleVO tradeReturnGiftGuideRuleVO) {
        return Result.success(tradeReturnGiftGuideRuleService.add(tradeReturnGiftGuideRuleVO));
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated TradeReturnGiftGuideRuleVO tradeReturnGiftGuideRuleVO) {
        return Result.success(tradeReturnGiftGuideRuleService.update(tradeReturnGiftGuideRuleVO));
    }

    @PostMapping("/updateStatus")
    public Result<Boolean> updateStatus(@RequestBody @Validated TradeReturnGiftGuideRuleVO ruleVO) {
        return Result.success(tradeReturnGiftGuideRuleService.updateStatus(ruleVO.getId(), ruleVO.getStatus()));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated PrimiaryKey primiaryKey) {
        return Result.success(tradeReturnGiftGuideRuleService.delete(primiaryKey.getId()));
    }

}






