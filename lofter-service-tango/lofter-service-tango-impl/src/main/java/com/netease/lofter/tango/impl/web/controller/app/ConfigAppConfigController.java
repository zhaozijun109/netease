package com.netease.lofter.tango.impl.web.controller.app;

import com.netease.lofter.acl.sdk.annotation.ACLResource;
import com.netease.lofter.tango.impl.service.app.ConfigAppConfigService;
import com.netease.lofter.tango.impl.web.query.app.ConfigAppConfigQuery;
import com.netease.lofter.tango.impl.web.vo.app.ConfigAppConfigVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;

import com.netease.lofter.tango.impl.web.vo.trade.gift.TradeBcSlotVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/tango/configAppConfig")
@ACLResource(roles = "APP_CONFIG")
public class ConfigAppConfigController {

    @Autowired
    private ConfigAppConfigService configAppConfigService;

    @PostMapping("/list")
    public Result<PageResult<ConfigAppConfigVO>> listByQuery(@RequestBody @Validated ConfigAppConfigQuery configAppConfigQuery) {
        return Result.success(configAppConfigService.listByQuery(configAppConfigQuery));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated ConfigAppConfigVO configAppConfigVO) {
        return Result.success(configAppConfigService.add(configAppConfigVO));
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated ConfigAppConfigVO configAppConfigVO) {
        return Result.success(configAppConfigService.update(configAppConfigVO));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated PrimiaryKey primiaryKey) {
        return Result.success(configAppConfigService.delete(primiaryKey.getId()));
    }

    @PostMapping("/updateStatus")
    public Result<Boolean> updateStatus(@RequestBody @Validated ConfigAppConfigVO configAppConfigVO) {
        return Result.success(configAppConfigService.updateStatus(configAppConfigVO.getId(), configAppConfigVO.getStatus()));
    }
}






