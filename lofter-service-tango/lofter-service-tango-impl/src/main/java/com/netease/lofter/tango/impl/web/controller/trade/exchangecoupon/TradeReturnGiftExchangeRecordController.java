package com.netease.lofter.tango.impl.web.controller.trade.exchangecoupon;

import com.netease.lofter.tango.impl.service.trade.exchangecoupon.TradeReturnGiftExchangeRecordService;
import com.netease.lofter.tango.impl.web.query.trade.exchangecoupon.TradeReturnGiftExchangeRecordQuery;
import com.netease.lofter.tango.impl.web.vo.trade.exchangecoupon.TradeReturnGiftExchangeRecordVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/tango/tradeReturnGiftExchangeRecord")
public class TradeReturnGiftExchangeRecordController {

    @Autowired
    private TradeReturnGiftExchangeRecordService tradeReturnGiftExchangeRecordService;

    @PostMapping("/list")
    public Result<PageResult<TradeReturnGiftExchangeRecordVO>> listByQuery(@RequestBody @Validated TradeReturnGiftExchangeRecordQuery tradeReturnGiftExchangeRecordQuery) {
        return Result.success(tradeReturnGiftExchangeRecordService.listByQuery(tradeReturnGiftExchangeRecordQuery));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated TradeReturnGiftExchangeRecordVO tradeReturnGiftExchangeRecordVO) {
        return Result.success(tradeReturnGiftExchangeRecordService.add(tradeReturnGiftExchangeRecordVO));
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated TradeReturnGiftExchangeRecordVO tradeReturnGiftExchangeRecordVO) {
        return Result.success(tradeReturnGiftExchangeRecordService.update(tradeReturnGiftExchangeRecordVO));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated PrimiaryKey primiaryKey) {
        return Result.success(tradeReturnGiftExchangeRecordService.delete(primiaryKey.getId()));
    }

}






