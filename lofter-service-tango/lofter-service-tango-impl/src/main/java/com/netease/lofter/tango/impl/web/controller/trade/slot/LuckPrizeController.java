
package com.netease.lofter.tango.impl.web.controller.trade.slot;

import com.netease.lofter.acl.sdk.annotation.ACLResource;
import com.netease.lofter.tango.impl.delegate.trade.activity.PaidContentActivitySlotBooxMapperDelegate;
import com.netease.lofter.tango.impl.delegate.trade.ip.TradeActivityIpPoolMapperDelegate;
import com.netease.lofter.tango.impl.entity.trade.activity.PaidContentActivityReward;
import com.netease.lofter.tango.impl.entity.trade.ip.TradeActivityIpPool;
import com.netease.lofter.tango.impl.service.trade.slot.LuckActivityService;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;
import com.netease.lofter.tango.impl.service.trade.slot.LuckPrizeService;
import com.netease.lofter.tango.impl.web.query.trade.slot.LuckPrizeQuery;
import com.netease.lofter.tango.impl.web.vo.trade.slot.LuckActivityVO;
import com.netease.lofter.tango.impl.web.vo.trade.slot.LuckPrizeVO;

import com.netease.yaolu.commons.utils.exception.BizCode;
import com.netease.yaolu.commons.utils.exception.ErrorCode;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
* <p>Title: </p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2019</p>
<p>@author: jetbi</p>
* <p>@Create Time: 2024-7-22 21:05:49</p>
*/
@RestController
@RequestMapping("/tango/luckprize")
@ACLResource(roles = "ACTIVITY")
public class LuckPrizeController {

    @Autowired
    private LuckPrizeService luckPrizeService;
    @Autowired
    private TradeActivityIpPoolMapperDelegate ipPoolMapperDelegate;
    @Autowired
    private PaidContentActivitySlotBooxMapperDelegate slotBooxMapperDelegate;

    @PostMapping("/detail")
    public Result<LuckPrizeVO> getDetail(@RequestBody @Validated LuckPrizeQuery luckPrizeQuery) {
        return Result.success(luckPrizeService.getDetail(luckPrizeQuery.getId()));
    }

    @PostMapping("/list")
    public Result<PageResult<LuckPrizeVO>> listByQuery(@RequestBody @Validated LuckPrizeQuery luckPrizeQuery) {
        return Result.success(luckPrizeService.listByQuery(luckPrizeQuery));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated LuckPrizeVO luckPrizeVO) {
        if (luckPrizeVO.getRewardType() == PaidContentActivityReward.TYPE_IP_COUPON) {
            long actId = slotBooxMapperDelegate.getActIdByLootboxCode(luckPrizeVO.getActivityId());
            luckPrizeVO.setTargetId(2L);
            TradeActivityIpPool activityIpPool = ipPoolMapperDelegate.queryByActId(actId);
            if(activityIpPool == null) {
                return Result.error(new BizCode(),"未配置IP池");
            }
        }
        return Result.success(luckPrizeService.add(luckPrizeVO));
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated LuckPrizeVO luckPrizeVO) {
        return Result.success(luckPrizeService.update(luckPrizeVO));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated PrimiaryKey primiaryKey) {
        return Result.success(luckPrizeService.delete(primiaryKey.getId()));
    }
}






