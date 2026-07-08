
package com.netease.lofter.tango.impl.web.controller.trade.slot;

import com.netease.lofter.acl.sdk.annotation.ACLResource;
import com.netease.lofter.tango.impl.web.query.trade.slot.LuckPrizeQuery;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;
import com.netease.lofter.tango.impl.service.trade.slot.LuckPrizeProbabilityStrategyService;
import com.netease.lofter.tango.impl.web.query.trade.slot.LuckPrizeProbabilityStrategyQuery;
import com.netease.lofter.tango.impl.web.vo.trade.slot.LuckPrizeProbabilityStrategyVO;

import com.netease.lofter.tango.impl.web.vo.trade.slot.LuckPrizeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
* <p>Title: </p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2019</p>
<p>@author: jetbi</p>
* <p>@Create Time: 2024-7-23 10:20:37</p>
*/
@RestController
@RequestMapping("/tango/luckprizeprobabilitystrategy")
@ACLResource(roles = "ACTIVITY")
public class LuckPrizeProbabilityStrategyController {

    @Autowired
    private LuckPrizeProbabilityStrategyService luckPrizeProbabilityStrategyService;

    @PostMapping("/detail")
    public Result<LuckPrizeProbabilityStrategyVO> getDetail(@RequestBody @Validated LuckPrizeQuery luckPrizeQuery) {
        return Result.success(luckPrizeProbabilityStrategyService.getDetail(luckPrizeQuery.getActivityId()));
    }

    @PostMapping("/list")
    public Result<PageResult<LuckPrizeProbabilityStrategyVO>> listByQuery(@RequestBody @Validated LuckPrizeProbabilityStrategyQuery luckPrizeProbabilityStrategyQuery) {
        return Result.success(luckPrizeProbabilityStrategyService.listByQuery(luckPrizeProbabilityStrategyQuery));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated LuckPrizeProbabilityStrategyVO luckPrizeProbabilityStrategyVO) {
        return Result.success(luckPrizeProbabilityStrategyService.add(luckPrizeProbabilityStrategyVO));
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated LuckPrizeProbabilityStrategyVO luckPrizeProbabilityStrategyVO) {
        return Result.success(luckPrizeProbabilityStrategyService.update(luckPrizeProbabilityStrategyVO));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated PrimiaryKey primiaryKey) {
        return Result.success(luckPrizeProbabilityStrategyService.delete(primiaryKey.getId()));
    }

}






