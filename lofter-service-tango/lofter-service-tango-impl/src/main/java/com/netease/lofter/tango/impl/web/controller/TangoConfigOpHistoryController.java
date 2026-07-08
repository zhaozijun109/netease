package com.netease.lofter.tango.impl.web.controller;

import com.netease.lofter.tango.impl.service.TangoConfigOpHistoryService;
import com.netease.lofter.tango.impl.web.query.TangoConfigOpHistoryQuery;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.TangoConfigOpHistoryVO;
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
 * <p>@Create Time: 2024-6-13 18:10:40</p>
 */
@RestController
@RequestMapping("/tango/history")
public class TangoConfigOpHistoryController {

    @Autowired
    private TangoConfigOpHistoryService tangoConfigOpHistoryService;

    @PostMapping("/list")
    public Result<PageResult<TangoConfigOpHistoryVO>> listByQuery(@RequestBody @Validated TangoConfigOpHistoryQuery tangoConfigOpHistoryQuery) {
        return Result.success(tangoConfigOpHistoryService.listByQuery(tangoConfigOpHistoryQuery));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated TangoConfigOpHistoryVO tangoConfigOpHistoryVO) {
        return Result.success(tangoConfigOpHistoryService.add(tangoConfigOpHistoryVO));
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated TangoConfigOpHistoryVO tangoConfigOpHistoryVO) {
        return Result.success(tangoConfigOpHistoryService.update(tangoConfigOpHistoryVO));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated PrimiaryKey primiaryKey) {
        return Result.success(tangoConfigOpHistoryService.delete(primiaryKey.getId()));
    }

}






