package com.netease.lofter.tango.impl.web.controller.nos;

import com.netease.lofter.acl.sdk.meta.UserInfo;
import com.netease.lofter.tango.impl.service.TangoUserService;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.yaolu.commons.spring.web.bind.annotation.ClientIp;
import com.netease.yaolu.lofter.nos.bo.GenUploadTokenReq;
import com.netease.yaolu.lofter.nos.meta.constant.UploadAccessSource;
import com.netease.yaolu.lofter.nos.meta.dto.TokenWrapperDTO;
import com.netease.yaolu.lofter.nos.service.NosToolRPCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2019</p>
 * <p>@author: guleiyang</p>
 * <p>@Create Time: 2024-6-5 20:34:55</p>
 */
@RestController
@RequestMapping("/tango/nos")
public class NosController {

    @Autowired
    private NosToolRPCService nosToolRPCService;
    @Autowired
    private TangoUserService tangoUserService;

    @GetMapping("/token")
    public Result<TokenWrapperDTO> genUploadToken(@RequestParam("ext") String fileExt,
                                                  @RequestParam(value = "scene") long scene,
                                                  UserInfo userInfo,
                                                  @ClientIp String ip) {
        Long uid = tangoUserService.getByEmail(userInfo.getEmail()).getId();
        GenUploadTokenReq req = new GenUploadTokenReq();
        req.setUserId(uid);
        req.setType(UploadAccessSource.PC.ordinal());
        req.setFileExt(fileExt);
        req.setScene(scene);
        req.setNeedReview(false);
        req.setIp(ip);
        TokenWrapperDTO token = nosToolRPCService.genUploadToken(req);
        return Result.success(token);
    }

}






