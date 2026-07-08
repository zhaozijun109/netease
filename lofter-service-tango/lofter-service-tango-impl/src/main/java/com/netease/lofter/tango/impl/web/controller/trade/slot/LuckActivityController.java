
package com.netease.lofter.tango.impl.web.controller.trade.slot;

import com.netease.lofter.acl.sdk.annotation.ACLResource;
import com.netease.lofter.tango.impl.web.query.trade.slot.LuckPrizeQuery;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;
import com.netease.lofter.tango.impl.service.trade.slot.LuckActivityService;
import com.netease.lofter.tango.impl.web.query.trade.slot.LuckActivityQuery;
import com.netease.lofter.tango.impl.web.vo.trade.slot.LuckActivityVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
* <p>Title: </p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2019</p>
<p>@author: jetbi</p>
* <p>@Create Time: 2024-7-23 10:14:10</p>
*/
@RestController
@RequestMapping("/tango/luckactivity")
@ACLResource(roles = "ACTIVITY")
public class LuckActivityController {

    @Autowired
    private LuckActivityService luckActivityService;

    @PostMapping("/detail")
    public LuckActivityVO getDetail(@RequestBody @Validated LuckPrizeQuery luckPrizeQuery) {
        return luckActivityService.getDetail(luckPrizeQuery.getActivityId());
    }

    @PostMapping("/list")
    public Result<PageResult<LuckActivityVO>> listByQuery(@RequestBody @Validated LuckActivityQuery luckActivityQuery) {
        return Result.success(luckActivityService.listByQuery(luckActivityQuery));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated LuckActivityVO luckActivityVO) {
        return Result.success(luckActivityService.add(luckActivityVO));
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated LuckActivityVO luckActivityVO) {
        return Result.success(luckActivityService.update(luckActivityVO));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated PrimiaryKey primiaryKey) {
        return Result.success(luckActivityService.delete(primiaryKey.getId()));
    }

}






