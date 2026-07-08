package com.netease.lofter.tango.impl.entity.trade.activity;

import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.netease.lofter.acl.sdk.annotation.ACLResource;
import com.netease.lofter.acl.sdk.meta.UserInfo;
import com.netease.lofter.devops.api.NotifyFacade;
import com.netease.lofter.tango.impl.consts.CommonProperties;
import com.netease.lofter.tango.impl.dubbo.TangoConfigRpcServiceImpl;
import com.netease.lofter.tango.impl.helper.ProfileEnv;
import com.netease.lofter.tango.impl.service.TangoConfigService;
import com.netease.lofter.tango.impl.web.query.trade.activity.ActConfigQuery;
import com.netease.lofter.tango.impl.web.query.trade.activity.ActConfigVO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.config.TangoConfigVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequestMapping("/tango/trade/act/config")
@ACLResource(roles = "ACTIVITY_APOLLO")
public class ActivityApolloConfigController {

    private ApolloOpenApiClient apolloOpenApiClient;

    public static final Set<String> ACT_APOLLO_CONFIGS = Sets.newHashSet("paid.content.processing.act.config", "paid.content.act.ip.crowd");

    @Autowired
    private ProfileEnv profileEnv;
    @Autowired
    private TangoConfigService tangoConfigService;
    @Autowired
    private TangoConfigRpcServiceImpl tangoConfigRpcServiceImpl;
    @Autowired
    private NotifyFacade notifyFacade;

    public ActivityApolloConfigController(CommonProperties commonProperties) {
        this.apolloOpenApiClient = ApolloOpenApiClient.newBuilder().withPortalUrl(commonProperties.getApolloOpenApi().getPortalUrl())
                .withToken(commonProperties.getApolloOpenApi().getTradeAppToken()).withConnectTimeout(3000)
                .withReadTimeout(3000).build();
    }


    @PostMapping("/list")
    public Result<PageResult<TangoConfigVO>> listByQuery(@RequestBody @Validated ActConfigQuery tangoConfigQuery) {
        PageResult<TangoConfigVO> pageResult = new PageResult<>(tangoConfigQuery.getPage());
        if(StringUtils.isEmpty(tangoConfigQuery.getConfigKey())) {
            return Result.success(pageResult.total(1).list(Lists.newArrayList()));
        }
        String env = "PRO";
        if (profileEnv.isTest()) {
            env = "DEV";
        }
        OpenItemDTO openItemDTO = apolloOpenApiClient.getItem("lofter-trade", env, profileEnv.getEnv() , "lofter.trade.summer.act", tangoConfigQuery.getConfigKey());
        TangoConfigVO configVO = new TangoConfigVO();
        configVO.setConfigKey(openItemDTO.getKey());
        configVO.setConfigValue(openItemDTO.getValue());
        configVO.setDescription(openItemDTO.getComment());

        return Result.success(pageResult.total(1).list(Lists.newArrayList(configVO)));
    }


    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated ActConfigVO configVO, UserInfo userInfo) {
        if (!ACT_APOLLO_CONFIGS.contains(configVO.getConfigKey())){
            return Result.genericFail("禁止修改");
        }
        String env = "PRO";
        if (profileEnv.isTest()) {
            env = "DEV";
        }
        OpenItemDTO openItemDTO = new OpenItemDTO();
        openItemDTO.setKey(configVO.getConfigKey());
        openItemDTO.setComment("tango配置后台修改");
        openItemDTO.setDataChangeCreatedBy("shiliang1");
        openItemDTO.setDataChangeLastModifiedBy("shiliang1");
        openItemDTO.setDataChangeLastModifiedTime(new Date());
        openItemDTO.setValue(configVO.getConfigValue());
        apolloOpenApiClient.createOrUpdateItem("lofter-trade", env, profileEnv.getEnv(), "lofter.trade.summer.act", openItemDTO);

        NamespaceReleaseDTO releaseDTO = new NamespaceReleaseDTO();
        releaseDTO.setReleaseTitle("tango配置后台修改apollo活动配置");
        releaseDTO.setReleasedBy("shiliang1");
        releaseDTO.setEmergencyPublish(true);
        releaseDTO.setReleaseComment("tango配置后台修改apollo活动配置");
        apolloOpenApiClient.publishNamespace("lofter-trade", env, profileEnv.getEnv(), "lofter.trade.summer.act", releaseDTO);
        if(profileEnv.isOnline() || profileEnv.isPre()){
            String message = "tango配置后台修改apollo配置: \n" +
                    "        配置人: " + userInfo.getNickName() + "\n" +
                    "        配置环境: " + profileEnv.getEnv() + "\n" +
                    "        配置key: " + configVO.getConfigKey() + "\n" +
                    "        配置value: " + configVO.getConfigValue() + "\n";

            notifyFacade.sendTeamMessage("5644945", message);
        }
        return Result.success();
    }
}
