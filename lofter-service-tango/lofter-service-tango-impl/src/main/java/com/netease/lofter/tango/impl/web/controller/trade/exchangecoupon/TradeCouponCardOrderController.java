package com.netease.lofter.tango.impl.web.controller.trade.exchangecoupon;

import com.netease.lofter.acl.sdk.annotation.ACLResource;
import com.netease.lofter.tango.impl.service.trade.exchangecoupon.TradeCouponCardOrderService;
import com.netease.lofter.tango.impl.web.query.trade.exchangecoupon.TradeCouponCardOrderQuery;
import com.netease.lofter.tango.impl.web.vo.trade.exchangecoupon.TradeCouponCardOrderVO;

import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/tango/tradeCouponCardOrder")
@ACLResource(roles = "EXCHANGE_COUPON")
public class TradeCouponCardOrderController {

    @Autowired
    private TradeCouponCardOrderService tradeCouponCardOrderService;

    @PostMapping("/list")
    public Result<PageResult<TradeCouponCardOrderVO>> listByQuery(@RequestBody @Validated TradeCouponCardOrderQuery tradeCouponCardOrderQuery) {
        return Result.success(tradeCouponCardOrderService.listByQuery(tradeCouponCardOrderQuery));
    }
}






