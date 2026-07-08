
package com.netease.lofter.tango.impl.web.controller.trade.activity;

import com.netease.lofter.acl.sdk.annotation.ACLResource;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;
import com.netease.lofter.tango.impl.service.trade.activity.PaidContentActivityService;
import com.netease.lofter.tango.impl.web.query.trade.activity.PaidContentActivityQuery;
import com.netease.lofter.tango.impl.web.vo.trade.activity.PaidContentActivityVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
* <p>Title: </p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2019</p>
<p>@author: guleiyang</p>
* <p>@Create Time: 2024-7-12 19:28:00</p>
*/
@RestController
@RequestMapping("/tango/paidcontentactivity")
@ACLResource(roles = "ACTIVITY")
public class PaidContentActivityController {

    @Autowired
    private PaidContentActivityService paidContentActivityService;

    @PostMapping("/list")
    public Result<PageResult<PaidContentActivityVO>> listByQuery(@RequestBody @Validated PaidContentActivityQuery paidContentActivityQuery) {
        return Result.success(paidContentActivityService.listByQuery(paidContentActivityQuery));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated PaidContentActivityVO paidContentActivityVO) {
        return Result.success(paidContentActivityService.add(paidContentActivityVO));
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated PaidContentActivityVO paidContentActivityVO) {
        return Result.success(paidContentActivityService.update(paidContentActivityVO));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated PrimiaryKey primiaryKey) {
        return Result.success(paidContentActivityService.delete(primiaryKey.getId()));
    }

}






