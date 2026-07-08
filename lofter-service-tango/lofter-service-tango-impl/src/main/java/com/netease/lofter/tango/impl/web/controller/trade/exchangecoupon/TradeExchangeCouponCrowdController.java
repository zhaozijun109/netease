package com.netease.lofter.tango.impl.web.controller.trade.exchangecoupon;

import com.netease.lofter.acl.sdk.annotation.ACLResource;
import com.netease.lofter.tango.impl.service.trade.exchangecoupon.TradeExchangeCouponCrowdService;
import com.netease.lofter.tango.impl.web.query.trade.exchangecoupon.TradeExchangeCouponCrowdQuery;
import com.netease.lofter.tango.impl.web.vo.trade.exchangecoupon.TradeExchangeCouponCrowdVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/tango/tradeExchangeCouponCrowd")
@ACLResource(roles = "ACTIVITY")
public class TradeExchangeCouponCrowdController {

    @Autowired
    private TradeExchangeCouponCrowdService tradeExchangeCouponCrowdService;

    @PostMapping("/list")
    public Result<PageResult<TradeExchangeCouponCrowdVO>> listByQuery(@RequestBody @Validated TradeExchangeCouponCrowdQuery tradeExchangeCouponCrowdQuery) {
        return Result.success(tradeExchangeCouponCrowdService.listByQuery(tradeExchangeCouponCrowdQuery));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated TradeExchangeCouponCrowdVO tradeExchangeCouponCrowdVO) {
        return Result.success(tradeExchangeCouponCrowdService.add(tradeExchangeCouponCrowdVO));
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated TradeExchangeCouponCrowdVO tradeExchangeCouponCrowdVO) {
        return Result.success(tradeExchangeCouponCrowdService.update(tradeExchangeCouponCrowdVO));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated PrimiaryKey primiaryKey) {
        return Result.success(tradeExchangeCouponCrowdService.delete(primiaryKey.getId()));
    }

}






