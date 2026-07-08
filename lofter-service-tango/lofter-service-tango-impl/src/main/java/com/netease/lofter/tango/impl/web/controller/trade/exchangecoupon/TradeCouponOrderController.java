package com.netease.lofter.tango.impl.web.controller.trade.exchangecoupon;

import com.netease.lofter.acl.sdk.annotation.ACLResource;
import com.netease.lofter.tango.impl.service.trade.exchangecoupon.TradeCouponOrderService;
import com.netease.lofter.tango.impl.web.query.trade.exchangecoupon.TradeCouponOrderQuery;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.trade.exchangecoupon.TradeCouponOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/tango/TradeCouponOrder")
@ACLResource(roles = "EXCHANGE_COUPON")
public class TradeCouponOrderController {

    @Autowired
    private TradeCouponOrderService TradeCouponOrderService;

    @PostMapping("/list")
    public Result<PageResult<TradeCouponOrderVO>> listByQuery(@RequestBody @Validated TradeCouponOrderQuery tradeCouponOrderQuery) {
        return Result.success(TradeCouponOrderService.listByQuery(tradeCouponOrderQuery));
    }
}






