package com.netease.lofter.tango.impl.web.controller.statis;

import com.netease.lofter.acl.sdk.annotation.ACLResource;
import com.netease.lofter.tango.impl.service.statis.AdChannelConfigService;
import com.netease.lofter.tango.impl.web.query.statis.AdChannelConfigQuery;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.statis.AdChannelConfigVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2019</p>
 * <p>@author: guleiyang</p>
 * <p>@Create Time: 2024-6-12 18:44:45</p>
 */
@RestController
@RequestMapping("/tango/adchannelconfig")
public class AdChannelConfigController {

    @Autowired
    private AdChannelConfigService service;

    @PostMapping("/list")
    public Result<PageResult<AdChannelConfigVO>> listByQuery(@RequestBody @Validated AdChannelConfigQuery adChannelConfigQuery) {
        return Result.success(service.listByQuery(adChannelConfigQuery));
    }

    @ACLResource(roles = "ADCHANNELCONFIG")
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated AdChannelConfigVO adChannelConfigVO) {
        return Result.success(service.add(adChannelConfigVO));
    }

    @ACLResource(roles = "ADCHANNELCONFIG")
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated AdChannelConfigVO adChannelConfigVO) {
        return Result.success(service.update(adChannelConfigVO));
    }

    @ACLResource(roles = "ADCHANNELCONFIG")
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated PrimiaryKey primiaryKey) {
        return Result.success(service.delete(primiaryKey.getId()));
    }

}






