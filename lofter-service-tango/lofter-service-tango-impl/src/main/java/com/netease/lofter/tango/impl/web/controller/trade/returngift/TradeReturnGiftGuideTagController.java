package com.netease.lofter.tango.impl.web.controller.trade.returngift;

import com.netease.lofter.acl.sdk.annotation.ACLResource;
import com.netease.lofter.tango.impl.service.trade.returngift.TradeReturnGiftGuideTagService;
import com.netease.lofter.tango.impl.web.query.trade.returngift.TradeReturnGiftGuideTagQuery;
import com.netease.lofter.tango.impl.web.vo.trade.returngift.TradeReturnGiftGuideTagVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/tango/tradeReturnGiftGuideTag")
@ACLResource(roles = "GIFT")
public class TradeReturnGiftGuideTagController {

    @Autowired
    private TradeReturnGiftGuideTagService tradeReturnGiftGuideTagService;

    @PostMapping("/list")
    public Result<PageResult<TradeReturnGiftGuideTagVO>> listByQuery(@RequestBody @Validated TradeReturnGiftGuideTagQuery tradeReturnGiftGuideTagQuery) {
        return Result.success(tradeReturnGiftGuideTagService.listByQuery(tradeReturnGiftGuideTagQuery));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated TradeReturnGiftGuideTagVO tradeReturnGiftGuideTagVO) {
        return Result.success(tradeReturnGiftGuideTagService.add(tradeReturnGiftGuideTagVO));
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated TradeReturnGiftGuideTagVO tradeReturnGiftGuideTagVO) {
        return Result.success(tradeReturnGiftGuideTagService.update(tradeReturnGiftGuideTagVO));
    }

    @PostMapping("/updateStatus")
    public Result<Boolean> updateStatus(@RequestBody @Validated TradeReturnGiftGuideTagVO tradeReturnGiftGuideTagVO) {
        return Result.success(tradeReturnGiftGuideTagService.updateStatus(tradeReturnGiftGuideTagVO.getId(), tradeReturnGiftGuideTagVO.getStatus()));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated PrimiaryKey primiaryKey) {
        return Result.success(tradeReturnGiftGuideTagService.delete(primiaryKey.getId()));
    }

}






