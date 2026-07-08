package com.netease.lofter.tango.impl.web.controller.trade.ip;

import com.netease.lofter.tango.impl.service.trade.ip.TradeActivityIpPoolService;
import com.netease.lofter.tango.impl.web.query.trade.ip.TradeActivityIpPoolQuery;
import com.netease.lofter.tango.impl.web.vo.trade.ip.TradeActivityIpPoolVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/tango/tradeActivityIpPool")
public class TradeActivityIpPoolController {

    @Autowired
    private TradeActivityIpPoolService tradeActivityIpPoolService;

    @PostMapping("/list")
    public Result<PageResult<TradeActivityIpPoolVO>> listByQuery(@RequestBody @Validated TradeActivityIpPoolQuery tradeActivityIpPoolQuery) {
        return Result.success(tradeActivityIpPoolService.listByQuery(tradeActivityIpPoolQuery));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated TradeActivityIpPoolVO tradeActivityIpPoolVO) {
        return Result.success(tradeActivityIpPoolService.add(tradeActivityIpPoolVO));
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated TradeActivityIpPoolVO tradeActivityIpPoolVO) {
        return Result.success(tradeActivityIpPoolService.update(tradeActivityIpPoolVO));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated PrimiaryKey primiaryKey) {
        return Result.success(tradeActivityIpPoolService.delete(primiaryKey.getId()));
    }

}






