package com.netease.lofter.tango.impl.web.controller;

import com.netease.lofter.acl.sdk.meta.UserInfo;
import com.netease.lofter.tango.impl.service.TangoAppService;
import com.netease.lofter.tango.impl.web.query.TangoAppQuery;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.config.TangoAppAddVO;
import com.netease.lofter.tango.impl.web.vo.config.TangoAppVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tango/app")
public class TangoAppController {

    @Autowired
    private TangoAppService tangoAppService;

    @GetMapping("/list")
    public Result<List<TangoAppVO>> listAll() {
        List<TangoAppVO> tangoAppVOS = tangoAppService.listAll();
        return Result.success(tangoAppVOS);
    }

    @PostMapping("/list/page")
    public Result<PageResult<TangoAppVO>> list(@Validated @RequestBody TangoAppQuery tangoAppQuery) {
        return Result.success(tangoAppService.list(tangoAppQuery));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated TangoAppAddVO tangoAppAddVO, UserInfo userInfo) {
        tangoAppAddVO.setOperator(userInfo.getEmail());
        tangoAppService.add(tangoAppAddVO);
        return Result.success();
    }
}
