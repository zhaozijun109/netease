
package com.netease.lofter.tango.impl.web.controller.trade.slot;

import com.netease.lofter.acl.sdk.annotation.ACLResource;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;
import com.netease.lofter.tango.impl.service.trade.slot.LuckProductPrizeTypeService;
import com.netease.lofter.tango.impl.web.query.trade.slot.LuckProductPrizeTypeQuery;
import com.netease.lofter.tango.impl.web.vo.trade.slot.LuckProductPrizeTypeVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
* <p>Title: </p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2019</p>
<p>@author: jetbi</p>
* <p>@Create Time: 2024-7-23 10:18:28</p>
*/
@RestController
@RequestMapping("/tango/luckproductprizetype")
@ACLResource(roles = "ACTIVITY")
public class LuckProductPrizeTypeController {

    @Autowired
    private LuckProductPrizeTypeService luckProductPrizeTypeService;

    @PostMapping("/list")
    public Result<PageResult<LuckProductPrizeTypeVO>> listByQuery(@RequestBody @Validated LuckProductPrizeTypeQuery luckProductPrizeTypeQuery) {
        return Result.success(luckProductPrizeTypeService.listByQuery(luckProductPrizeTypeQuery));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated LuckProductPrizeTypeVO luckProductPrizeTypeVO) {
        return Result.success(luckProductPrizeTypeService.add(luckProductPrizeTypeVO));
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated LuckProductPrizeTypeVO luckProductPrizeTypeVO) {
        return Result.success(luckProductPrizeTypeService.update(luckProductPrizeTypeVO));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated PrimiaryKey primiaryKey) {
        return Result.success(luckProductPrizeTypeService.delete(primiaryKey.getId()));
    }

}






