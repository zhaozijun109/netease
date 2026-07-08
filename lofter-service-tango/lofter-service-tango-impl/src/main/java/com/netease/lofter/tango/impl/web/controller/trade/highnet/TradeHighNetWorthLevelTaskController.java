package com.netease.lofter.tango.impl.web.controller.trade.highnet;

import com.netease.lofter.tango.impl.service.trade.highnet.TradeHighNetWorthLevelTaskService;
import com.netease.lofter.tango.impl.web.query.trade.highnet.TradeHighNetWorthLevelTaskQuery;
import com.netease.lofter.tango.impl.web.vo.trade.highnet.TradeHighNetWorthLevelTaskVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/tango/tradeHighNetWorthLevelTask")
public class TradeHighNetWorthLevelTaskController {

    @Autowired
    private TradeHighNetWorthLevelTaskService tradeHighNetWorthLevelTaskService;

    @PostMapping("/list")
    public Result<PageResult<TradeHighNetWorthLevelTaskVO>> listByQuery(@RequestBody @Validated TradeHighNetWorthLevelTaskQuery tradeHighNetWorthLevelTaskQuery) {
        return Result.success(tradeHighNetWorthLevelTaskService.listByQuery(tradeHighNetWorthLevelTaskQuery));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated TradeHighNetWorthLevelTaskVO tradeHighNetWorthLevelTaskVO) {
        return Result.success(tradeHighNetWorthLevelTaskService.add(tradeHighNetWorthLevelTaskVO));
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated TradeHighNetWorthLevelTaskVO tradeHighNetWorthLevelTaskVO) {
        return Result.success(tradeHighNetWorthLevelTaskService.update(tradeHighNetWorthLevelTaskVO));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated PrimiaryKey primiaryKey) {
        return Result.success(tradeHighNetWorthLevelTaskService.delete(primiaryKey.getId()));
    }

}






