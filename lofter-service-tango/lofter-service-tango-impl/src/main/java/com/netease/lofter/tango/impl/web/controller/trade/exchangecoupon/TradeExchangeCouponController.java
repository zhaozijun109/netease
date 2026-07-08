package com.netease.lofter.tango.impl.web.controller.trade.exchangecoupon;

import com.netease.lofter.tango.impl.service.trade.exchangecoupon.TradeExchangeCouponService;
import com.netease.lofter.tango.impl.web.query.trade.exchangecoupon.TradeExchangeCouponQuery;
import com.netease.lofter.tango.impl.web.vo.trade.exchangecoupon.TradeExchangeCouponVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/tango/tradeExchangeCoupon")
public class TradeExchangeCouponController {

    @Autowired
    private TradeExchangeCouponService tradeExchangeCouponService;

    @PostMapping("/list")
    public Result<PageResult<TradeExchangeCouponVO>> listByQuery(@RequestBody @Validated TradeExchangeCouponQuery tradeExchangeCouponQuery) {
        return Result.success(tradeExchangeCouponService.listByQuery(tradeExchangeCouponQuery));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated TradeExchangeCouponVO tradeExchangeCouponVO) {
        return Result.success(tradeExchangeCouponService.add(tradeExchangeCouponVO));
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated TradeExchangeCouponVO tradeExchangeCouponVO) {
        return Result.success(tradeExchangeCouponService.update(tradeExchangeCouponVO));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated PrimiaryKey primiaryKey) {
        return Result.success(tradeExchangeCouponService.delete(primiaryKey.getId()));
    }

}






