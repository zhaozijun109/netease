package com.netease.lofter.tango.impl.web.controller;

import com.netease.lofter.tango.impl.service.OperatorLogService;
import com.netease.lofter.tango.impl.web.query.OperatorLogQuery;
import com.netease.lofter.tango.impl.web.vo.OperatorLogVO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;
import com.netease.lofter.tango.impl.web.vo.Result;
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
 * <p>@Create Time: 2024-6-5 20:34:55</p>
 */
@RestController
@RequestMapping("/tango/operatorlog")
public class OperatorLogController {

    @Autowired
    private OperatorLogService service;

    @PostMapping("/list")
    public Result<PageResult<OperatorLogVO>> listByQuery(@RequestBody @Validated OperatorLogQuery operatorLogQuery) {
        return Result.success(service.listByQuery(operatorLogQuery));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated OperatorLogVO operatorLogVO) {
        return Result.success(service.add(operatorLogVO));
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated OperatorLogVO operatorLogVO) {
        return Result.success(service.update(operatorLogVO));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated PrimiaryKey primiaryKey) {
        return Result.success(service.delete(primiaryKey.getId()));
    }

}






