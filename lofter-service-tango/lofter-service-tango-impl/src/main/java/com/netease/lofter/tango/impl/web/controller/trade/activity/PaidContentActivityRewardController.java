package com.netease.lofter.tango.impl.web.controller.trade.activity;

import com.netease.lofter.acl.sdk.annotation.ACLResource;
import com.netease.lofter.tango.impl.service.trade.activity.PaidContentActivityRewardService;
import com.netease.lofter.tango.impl.web.query.trade.activity.PaidContentActivityRewardQuery;
import com.netease.lofter.tango.impl.web.vo.trade.activity.PaidContentActivityRewardVO;

import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/tango/paidContentActivityReward")
@ACLResource(roles = "ACTIVITY")
public class PaidContentActivityRewardController {

    @Autowired
    private PaidContentActivityRewardService paidContentActivityRewardService;

    @PostMapping("/list")
    public Result<PageResult<PaidContentActivityRewardVO>> listByQuery(@RequestBody @Validated PaidContentActivityRewardQuery paidContentActivityRewardQuery) {
        return Result.success(paidContentActivityRewardService.listByQuery(paidContentActivityRewardQuery));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated PaidContentActivityRewardVO paidContentActivityRewardVO) {
        return Result.success(paidContentActivityRewardService.add(paidContentActivityRewardVO));
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated PaidContentActivityRewardVO paidContentActivityRewardVO) {
        return Result.success(paidContentActivityRewardService.update(paidContentActivityRewardVO));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated PrimiaryKey primiaryKey) {
        return Result.success(paidContentActivityRewardService.delete(primiaryKey.getId()));
    }

}






