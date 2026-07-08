package com.netease.lofter.tango.impl.web.controller.clickhouse;

import com.netease.lofter.tango.impl.service.clickhouse.ClickHousePostService;
import com.netease.lofter.tango.impl.web.query.clickhouse.ClickHousePostQuery;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.clickhouse.ClickhousePostVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/tango/clickhouse/post")
public class ClickhousePostController {

    @Autowired
    private ClickHousePostService clickHousePostService;

    @PostMapping("/list")
    public Result<PageResult<ClickhousePostVO>> listByQuery(@Validated @RequestBody ClickHousePostQuery clickHousePostQuery) {
        PageResult<ClickhousePostVO> tangoConfigVOPageResult = clickHousePostService.listByQuery(clickHousePostQuery);
        return Result.success(tangoConfigVOPageResult);
    }

    @PostMapping("/import")
    public Result<Boolean> importFile(@RequestParam("file") MultipartFile multipartFile) {

        return Result.success(true);
    }

    @PostMapping("/export")
    public void exportFile(@Validated @RequestBody ClickHousePostQuery clickHousePostQuery, HttpServletResponse response) {
        clickHousePostService.download(clickHousePostQuery, response);
    }


}
