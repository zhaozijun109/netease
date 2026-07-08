package com.netease.lofter.tango.impl.web.controller.trade.exchangecoupon;

import com.netease.lofter.acl.sdk.annotation.ACLResource;
import com.netease.lofter.tango.impl.service.trade.exchangecoupon.TradeExchangeCouponSKUService;
import com.netease.lofter.tango.impl.web.query.trade.exchangecoupon.TradeExchangeCouponSKUQuery;
import com.netease.lofter.tango.impl.web.vo.trade.exchangecoupon.TradeExchangeCouponSKUVO;

import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/tango/tradeExchangeCouponSKU")
@ACLResource(roles = "ACTIVITY")
public class TradeExchangeCouponSKUController {

    @Autowired
    private TradeExchangeCouponSKUService tradeExchangeCouponSKUService;

    @PostMapping("/list")
    public Result<PageResult<TradeExchangeCouponSKUVO>> queryActSkus(@RequestBody @Validated TradeExchangeCouponSKUQuery tradeExchangeCouponSKUQuery) {
        return Result.success(tradeExchangeCouponSKUService.queryActSkus(tradeExchangeCouponSKUQuery));
    }

    @PostMapping("/normal/list")
    public Result<PageResult<TradeExchangeCouponSKUVO>> querySkus(@RequestBody @Validated TradeExchangeCouponSKUQuery tradeExchangeCouponSKUQuery) {
        return Result.success(tradeExchangeCouponSKUService.listByQuery(tradeExchangeCouponSKUQuery));
    }


    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated TradeExchangeCouponSKUVO tradeExchangeCouponSKUVO) {
        return Result.success(tradeExchangeCouponSKUService.add(tradeExchangeCouponSKUVO));
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated TradeExchangeCouponSKUVO tradeExchangeCouponSKUVO) {
        return Result.success(tradeExchangeCouponSKUService.update(tradeExchangeCouponSKUVO));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated PrimiaryKey primiaryKey) {
        return Result.success(tradeExchangeCouponSKUService.delete(primiaryKey.getId()));
    }

    @PostMapping("/updateGrantStrategy")
    public Result<Boolean> updateGrantStrategy(@RequestBody @Validated TradeExchangeCouponSKUVO sku) {
        return Result.success(tradeExchangeCouponSKUService.updateGrantStrategy(sku.getId(), sku.getGrantStrategy()));
    }
    @PostMapping("/updateSettleStrategy")
    public Result<Boolean> updateSettleStrategy(@RequestBody @Validated TradeExchangeCouponSKUVO sku) {
        return Result.success(tradeExchangeCouponSKUService.updateSettleStrategy(sku.getId(), sku.getSettleStrategy()));
    }
    @PostMapping("/updateShowCondition")
    public Result<Boolean> updateShowCondition(@RequestBody @Validated TradeExchangeCouponSKUVO sku) {
        return Result.success(tradeExchangeCouponSKUService.updateShowCondition(sku.getId(), sku.getShowCondition()));
    }


}






