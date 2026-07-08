package com.netease.lofter.tango.impl.config;

import com.netease.lofter.devops.api.NotifyFacade;
import com.netease.yaolu.lofter.api.service.UserCenterService;
import com.netease.yaolu.lofter.nos.service.NosToolRPCService;
import com.netease.yaolu.lofter.post.biz.service.PostCenterService;
import com.netease.yuedu.service.validator.api.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration(value = DubboReferenceConfig.BEAN_NAME, proxyBeanMethods = false)
public class DubboReferenceConfig implements BeanPostProcessor {

    public static final String BEAN_NAME = "dubboReferenceConfig";
    @DubboReference(group = "${dubbo.group.lofter:lofter}", version = "1.0.0", check = false, mock = "false")
    private SmsService smsService;
    @DubboReference(group = "${dubbo.group.lofter:lofter}", version = "1.0.0", check = false, mock = "false")
    private UserCenterService userCenterService;
    @DubboReference(group = "${dubbo.group.lofter:lofter}", version = "1.0.0", check = false, mock = "false")
    private PostCenterService postCenterService;
    @DubboReference(group = "${dubbo.group.lofter:lofter}", version = "1.0.0", check = false, mock = "false")
    private NosToolRPCService nosToolRPCService;
    @DubboReference(group = "lofter", version = "1.0.0", check = false, mock = "false")
    private NotifyFacade notifyFacade;
}
