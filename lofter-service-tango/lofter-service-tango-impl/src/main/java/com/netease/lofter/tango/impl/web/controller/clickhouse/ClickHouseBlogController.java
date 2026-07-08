package com.netease.lofter.tango.impl.web.controller.clickhouse;

import com.netease.lofter.tango.impl.service.clickhouse.ClickHouseBlogService;
import com.netease.lofter.tango.impl.web.query.clickhouse.ClickHouseBlogQuery;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.clickhouse.ClickhouseBlogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/tango/clickhouse/blog")
public class ClickHouseBlogController {

    @Autowired
    private ClickHouseBlogService clickHouseBlogService;

    @PostMapping("/list")
    public Result<PageResult<ClickhouseBlogVO>> listByQuery(@Validated @RequestBody ClickHouseBlogQuery clickHouseBlogQuery) {
        PageResult<ClickhouseBlogVO> tangoConfigVOPageResult = clickHouseBlogService.listByQuery(clickHouseBlogQuery);
        return Result.success(tangoConfigVOPageResult);
    }

    @PostMapping("/import")
    public Result<Boolean> importFile(@RequestParam("file") MultipartFile multipartFile) {
        return Result.success(true);
    }

    @PostMapping("/export")
    public void exportFile(@Validated @RequestBody ClickHouseBlogQuery clickHouseBlogQuery, HttpServletResponse response) {
        clickHouseBlogService.download(clickHouseBlogQuery, response);
    }


}
