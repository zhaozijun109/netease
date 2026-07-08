package com.netease.lofter.tango.impl.web.controller;

import com.netease.lofter.tango.impl.service.TangoUserService;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.SelectVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tango/user")
public class TangoUserController {

    @Autowired
    private TangoUserService tangoUserService;

    @GetMapping("/admin")
    public Result<Boolean> admin() {
        return Result.success(tangoUserService.isAdmin());
    }

    @GetMapping("/list/all")
    public Result<List<String>> listAll() {
        return Result.success(tangoUserService.listAll());
    }

    @GetMapping("/list/selector")
    public Result<List<SelectVO>> listSelect() {
        return Result.success(tangoUserService.listSelector());
    }
}
