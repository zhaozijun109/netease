package com.netease.lofter.tango.impl.web.controller;

import com.netease.lofter.acl.sdk.meta.UserInfo;
import com.netease.lofter.tango.api.consts.TangoConfigOpType;
import com.netease.lofter.tango.api.dto.config.TangoConfigDTO;
import com.netease.lofter.tango.impl.dubbo.TangoConfigRpcServiceImpl;
import com.netease.lofter.tango.impl.helper.ProfileEnv;
import com.netease.lofter.tango.impl.service.TangoConfigService;
import com.netease.lofter.tango.impl.web.query.TangoConfigQuery;
import com.netease.lofter.tango.impl.web.ro.ConfigGetRO;
import com.netease.lofter.tango.impl.web.ro.ConfigGrantRO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.SelectVO;
import com.netease.lofter.tango.impl.web.vo.config.TangoConfigUpdateVO;
import com.netease.lofter.tango.impl.web.vo.config.TangoConfigVO;
import com.netease.mm.tk.common.util.lang.CollectionUtils3;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/tango/config")
public class TangoConfigController {

    @Autowired
    private TangoConfigService tangoConfigService;
    @Autowired
    private TangoConfigRpcServiceImpl tangoConfigRpcServiceImpl;
    @Autowired
    private ProfileEnv profileEnv;

    @PostMapping("/list")
    public Result<PageResult<TangoConfigVO>> listByQuery(@RequestBody @Validated TangoConfigQuery tangoConfigQuery) {

        return Result.success(tangoConfigService.listByQuery(tangoConfigQuery));
    }

    @GetMapping("/detail")
    public Result<TangoConfigVO> getByConfigKey(@RequestParam("configKey") String configKey, @RequestParam("appId") String appId) {
        return Result.success(tangoConfigService.getByConfigKey(appId, configKey));
    }

    /**
     * 内部服务启动初始化调用
     *
     * @param appId
     * @param token
     * @param env
     * @return
     */
    @RequestMapping("/list/internal")
    public List<TangoConfigDTO> listByAppId(@RequestParam("appId") String appId,
                                            @RequestParam("token") String token,
                                            @RequestParam("env") String env) {
        return tangoConfigRpcServiceImpl.listByAppIdAndToken(env, appId, token);
    }

    /**
     * 开放前端接口
     *
     * @param configGetRO
     * @return
     */
    @PostMapping("/list/pub")
    public Map<String, String> listByAppIdPublic(@RequestHeader(required = false, value = "referer") String referer,
            @RequestBody @Validated ConfigGetRO configGetRO) {
        if (StringUtils.isNotBlank(configGetRO.getReferer())) {
            referer = configGetRO.getReferer();
        }
        return tangoConfigService.listByAppIdPublic(referer, configGetRO);
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated TangoConfigVO tangoConfigVO, UserInfo userInfo) {
        tangoConfigVO.setOperator(userInfo.getEmail());
        return Result.success(tangoConfigService.add(tangoConfigVO));
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated TangoConfigUpdateVO updateVO, UserInfo userInfo) {
        updateVO.setOperator(userInfo.getEmail());
        return Result.success(tangoConfigService.update(updateVO));
    }

    @PostMapping("/syncOnline")
    public Result<Boolean> update(@RequestBody @Validated PrimiaryKey pk, UserInfo userInfo) {
        return Result.success(tangoConfigService.syncOnline(pk, userInfo.getEmail()));
    }

    @PostMapping("/grant")
    public Result<Boolean> grant(@RequestBody @Validated ConfigGrantRO configGrantRO) {
        return Result.success(tangoConfigService.grant(configGrantRO));
    }

    @GetMapping("/env/selector")
    public Result<List<SelectVO>> listEnvSelector() {
        List<SelectVO> selectVOS = new ArrayList<>();
        if (profileEnv.isTestMode()) {
            selectVOS.add(new SelectVO("测试", "test"));
            selectVOS.add(new SelectVO("开发", "dev"));
        } else {
            selectVOS.add(new SelectVO("线上", "pro"));
            selectVOS.add(new SelectVO("预发", "pre"));
        }
        return Result.success(selectVOS);
    }

    @GetMapping("/optype/selector")
    public Result<List<SelectVO>> listOpTypeSelector() {
        List<SelectVO> selectVOS = CollectionUtils3.mapper(Arrays.asList(TangoConfigOpType.values()), (op) -> new SelectVO(op.DESC, op.DESC));
        return Result.success(selectVOS);
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated PrimiaryKey primiaryKey, UserInfo userInfo) {
        return Result.success(tangoConfigService.delete(primiaryKey.getId(), userInfo.getEmail()));
    }
}
