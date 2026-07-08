package com.netease.lofter.tango.impl.web.controller.clickhouse;

import com.netease.lofter.tango.impl.service.clickhouse.ClickHouseIpService;
import com.netease.lofter.tango.impl.service.clickhouse.ClickHouseTagService;
import com.netease.lofter.tango.impl.web.query.clickhouse.ClickHouseIpQuery;
import com.netease.lofter.tango.impl.web.query.clickhouse.ClickHousePostQuery;
import com.netease.lofter.tango.impl.web.query.clickhouse.ClickHouseTagQuery;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.clickhouse.ClickhouseIpVO;
import com.netease.lofter.tango.impl.web.vo.clickhouse.ClickhouseTagVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/tango/clickhouse/tag")
public class ClickhouseTagController {
    @Autowired
    private ClickHouseTagService clickHouseTagService;

    @PostMapping("/list")
    public Result<PageResult<ClickhouseTagVO>> listByQuery(@Validated @RequestBody ClickHouseTagQuery clickHouseTagQuery) {
        PageResult<ClickhouseTagVO> tangoConfigVOPageResult = clickHouseTagService.listByQuery(clickHouseTagQuery);
        return Result.success(tangoConfigVOPageResult);
    }

    @PostMapping("/import")
    public Result<Boolean> importFile(@RequestParam("file") MultipartFile multipartFile) {

        return Result.success(true);
    }

    @PostMapping("/export")
    public void exportFile(@Validated @RequestBody ClickHouseTagQuery clickHouseTagQuery, HttpServletResponse response) {
        clickHouseTagService.download(clickHouseTagQuery, response);
    }
}
