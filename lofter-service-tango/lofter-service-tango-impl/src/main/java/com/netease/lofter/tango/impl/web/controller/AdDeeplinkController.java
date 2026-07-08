package com.netease.lofter.tango.impl.web.controller;

import com.netease.lofter.acl.sdk.annotation.ACLResource;
import com.netease.lofter.acl.sdk.meta.UserInfo;
import com.netease.lofter.tango.impl.service.AdDeepLinkService;
import com.netease.lofter.tango.impl.web.query.AdDeepLinkQuery;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.SelectVO;
import com.netease.lofter.tango.impl.web.vo.ad.AdDeepLinkVO;
import com.netease.mm.tk.mybatis.model.entity.PrimaryKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * 全局前缀：/lofter/tango
 */
@RestController
@RequestMapping("/tango/deeplink")
public class AdDeeplinkController {

    @Autowired
    private AdDeepLinkService adDeepLinkService;

    @PostMapping("/list")
    public Result<PageResult<AdDeepLinkVO>> listByQuery(@RequestBody AdDeepLinkQuery adDeepLinkQuery) {
        PageResult<AdDeepLinkVO> tangoConfigVOPageResult = adDeepLinkService.listByQuery(adDeepLinkQuery);
        return Result.success(tangoConfigVOPageResult);
    }

    @GetMapping("/channel/list")
    public Result<List<SelectVO>> listAllChannel() {
        return Result.success(adDeepLinkService.listAllChannel());
    }

    @ACLResource(roles = "DEEPLINK")
    @PostMapping("/add")
    public Result<Boolean> add(@Validated @RequestBody AdDeepLinkVO adDeepLinkVO, UserInfo userInfo) {
        adDeepLinkVO.setOperator(userInfo.getEmail());
        adDeepLinkService.add(adDeepLinkVO);
        return Result.success();
    }

    @PostMapping("/upload")
    public Result<Boolean> upload(@RequestParam("file") MultipartFile multipartFile) {
        try {
            InputStream is = multipartFile.getInputStream();
            is.close();
        } catch (Exception e) {
            return Result.genericFail("上传失败");
        }
        return Result.success(true);
    }

    @ACLResource(roles = "DEEPLINK")
    @PostMapping("/update")
    public Result<Boolean> update(@Validated @RequestBody AdDeepLinkVO deepLinkVO, UserInfo userInfo) {
        deepLinkVO.setOperator(userInfo.getEmail());
        adDeepLinkService.update(deepLinkVO);
        return Result.success(true);
    }

    @ACLResource(roles = "DEEPLINK")
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated PrimaryKey primaryKey) {
        adDeepLinkService.delete(primaryKey.getId());
        return Result.success(true);
    }
}
