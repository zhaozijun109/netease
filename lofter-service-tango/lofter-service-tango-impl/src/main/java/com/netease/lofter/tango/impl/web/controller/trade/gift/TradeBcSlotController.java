package com.netease.lofter.tango.impl.web.controller.trade.gift;

import com.netease.lofter.acl.sdk.annotation.ACLResource;
import com.netease.lofter.tango.impl.service.trade.gift.TradeBcSlotService;
import com.netease.lofter.tango.impl.web.query.trade.gift.TradeBcSlotQuery;
import com.netease.lofter.tango.impl.web.vo.trade.gift.TradeBcSlotVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;

import com.netease.lofter.tango.impl.web.vo.trade.returngift.TradeReturnGiftGuideRuleVO;
import com.netease.yaolu.commons.utils.exception.ErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/tango/tradeBcSlot")
@ACLResource(roles = "GIFT")
public class TradeBcSlotController {

    @Autowired
    private TradeBcSlotService tradeBcSlotService;

    @PostMapping("/list")
    public Result<PageResult<TradeBcSlotVO>> listByQuery(@RequestBody @Validated TradeBcSlotQuery tradeBcSlotQuery) {
        return Result.success(tradeBcSlotService.listByQuery(tradeBcSlotQuery));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated TradeBcSlotVO tradeBcSlotVO) {
        if(StringUtils.isBlank(tradeBcSlotVO.getCrowdIds())) {
            return Result.error(ErrorCode.SERVER_ERROR,"人群包id必填");
        }
        return Result.success(tradeBcSlotService.add(tradeBcSlotVO));
    }

    @PostMapping("/updateStatus")
    public Result<Boolean> updateStatus(@RequestBody @Validated TradeBcSlotVO tradeBcSlotVO) {
        return Result.success(tradeBcSlotService.updateStatus(tradeBcSlotVO.getId(), tradeBcSlotVO.getStatus()));
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated TradeBcSlotVO tradeBcSlotVO) {
        if(StringUtils.isBlank(tradeBcSlotVO.getCrowdIds())) {
            return Result.error(ErrorCode.SERVER_ERROR,"人群包id必填");
        }
        return Result.success(tradeBcSlotService.update(tradeBcSlotVO));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated PrimiaryKey primiaryKey) {
        return Result.success(tradeBcSlotService.delete(primiaryKey.getId()));
    }

}






