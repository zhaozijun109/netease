package com.netease.lofter.tango.impl.web.controller.clickhouse;

import com.netease.lofter.tango.impl.service.clickhouse.ClickHouseIpService;
import com.netease.lofter.tango.impl.web.query.clickhouse.ClickHouseIpQuery;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.clickhouse.ClickhouseIpVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/tango/clickhouse/ip")
public class ClickhouseIpController {
    @Autowired
    private ClickHouseIpService clickHouseIpService;

    @PostMapping("/list")
    public Result<PageResult<ClickhouseIpVO>> listByQuery(@Validated @RequestBody ClickHouseIpQuery clickHouseIpQuery) {
        PageResult<ClickhouseIpVO> tangoConfigVOPageResult = clickHouseIpService.listByQuery(clickHouseIpQuery);
        return Result.success(tangoConfigVOPageResult);
    }

    @PostMapping("/import")
    public Result<Boolean> importFile(@RequestParam("file") MultipartFile multipartFile) {

        return Result.success(true);
    }

    @PostMapping("/export")
    public void exportFile(@Validated @RequestBody ClickHouseIpQuery clickHouseIpQuery, HttpServletResponse response) {
        clickHouseIpService.download(clickHouseIpQuery, response);
    }
}
