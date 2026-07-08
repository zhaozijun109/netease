package com.netease.lofter.tango.impl.web.controller.trade.activity;

import com.netease.lofter.acl.sdk.annotation.ACLResource;
import com.netease.lofter.tango.impl.service.trade.activity.PaidContentActivitySlotBooxService;
import com.netease.lofter.tango.impl.service.trade.slot.LuckActivityService;
import com.netease.lofter.tango.impl.service.trade.slot.LuckPrizeService;
import com.netease.lofter.tango.impl.util.BeanUtils;
import com.netease.lofter.tango.impl.web.query.trade.activity.PaidContentActivitySlotBooxQuery;
import com.netease.lofter.tango.impl.web.vo.trade.activity.PaidContentActivitySlotBooxVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;

import com.netease.lofter.tango.impl.web.vo.trade.slot.LuckActivityVO;
import lombok.Data;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;


@RestController
@RequestMapping("/tango/paidContentActivitySlotBoox")
@ACLResource(roles = "ACTIVITY")
public class PaidContentActivitySlotBooxController {

    @Autowired
    private PaidContentActivitySlotBooxService paidContentActivitySlotBooxService;
    @Autowired
    private LuckActivityService luckActivityService;

    @PostMapping("/list")
    public Result<PageResult<PaidContentActivitySlotBooxVO>> listByQuery(@RequestBody @Validated PaidContentActivitySlotBooxQuery paidContentActivitySlotBooxQuery) {
        return Result.success(paidContentActivitySlotBooxService.listByQuery(paidContentActivitySlotBooxQuery));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated PaidContentActivitySlotBooxVO paidContentActivitySlotBooxVO) {
        LuckActivityVO luckActivityVO = new LuckActivityVO();
        luckActivityVO.setAppKey("lofter");
        luckActivityVO.setActivityId(paidContentActivitySlotBooxVO.getLootboxCode());
        luckActivityVO.setStatus((byte) 0);
        luckActivityVO.setDailyInitChanceCount(0);
        luckActivityVO.setBingoCount(0);
        luckActivityVO.setFakeBingoCount(0);
        luckActivityVO.setFakeJoinCount(0);
        luckActivityVO.setFakeJoinCountRateMax(0);
        luckActivityVO.setFakeJoinCountRateMin(0);
        luckActivityVO.setBingoCount(0);
        luckActivityVO.setDailyInitChanceCount(0);
        luckActivityVO.setDailySlotLimit(-1);

        BeanUtils.copyNonNullProperties(paidContentActivitySlotBooxVO, luckActivityVO);
        boolean res = luckActivityService.add(luckActivityVO) && paidContentActivitySlotBooxService.add(paidContentActivitySlotBooxVO);
        return Result.success(res);
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated PaidContentActivitySlotBooxVO paidContentActivitySlotBooxVO) {
        return Result.success(paidContentActivitySlotBooxService.update(paidContentActivitySlotBooxVO));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated PrimiaryKey primiaryKey) {
        return Result.success(paidContentActivitySlotBooxService.delete(primiaryKey.getId()));
    }

//    @PostMapping("/copy")
//    public Result<Boolean> copy(@RequestBody @Validated CopyLootBoxReq copyLootBoxReq){
//
//    }
//


    @Data
    public static class CopyLootBoxReq implements Serializable {
        private static final long serialVersionUID = -1;
        private List<Long> ids;
    }

}






