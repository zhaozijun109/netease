package com.netease.lofter.tango.impl.web.controller.trade.exchangecoupon;

import com.netease.lofter.tango.impl.service.trade.exchangecoupon.TradeExchangeCouponScopeResourceService;
import com.netease.lofter.tango.impl.web.query.trade.exchangecoupon.TradeExchangeCouponScopeResourceQuery;
import com.netease.lofter.tango.impl.web.vo.trade.exchangecoupon.TradeExchangeCouponScopeResourceVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;

import com.netease.yaolu.lofter.core.vo.web.JsonResponse;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/tango/tradeExchangeCouponScopeResource")
public class TradeExchangeCouponScopeResourceController {

    @Autowired
    private TradeExchangeCouponScopeResourceService tradeExchangeCouponScopeResourceService;

    @PostMapping("/list")
    public Result<PageResult<TradeExchangeCouponScopeResourceVO>> listByQuery(@RequestBody @Validated TradeExchangeCouponScopeResourceQuery tradeExchangeCouponScopeResourceQuery) {
        return Result.success(tradeExchangeCouponScopeResourceService.listByQuery(tradeExchangeCouponScopeResourceQuery));
    }

    @PostMapping("/importResource")
    public Result<Boolean> importResource() {
        return Result.success(true);
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated TradeExchangeCouponScopeResourceVO tradeExchangeCouponScopeResourceVO) {
        return Result.success(tradeExchangeCouponScopeResourceService.add(tradeExchangeCouponScopeResourceVO));
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated TradeExchangeCouponScopeResourceVO tradeExchangeCouponScopeResourceVO) {
        return Result.success(tradeExchangeCouponScopeResourceService.update(tradeExchangeCouponScopeResourceVO));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated PrimiaryKey primiaryKey) {
        return Result.success(tradeExchangeCouponScopeResourceService.delete(primiaryKey.getId()));
    }
}






