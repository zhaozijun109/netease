package com.netease.lofter.tango.impl.dubbo;

import com.netease.lofter.tango.api.client.deeplink.DeepLinkService;
import com.netease.lofter.tango.api.dto.deeplink.AdDeeplinkDTO;
import com.netease.lofter.tango.impl.delegate.AdDeepLinkDelegate;
import com.netease.mm.tk.common.util.BeanConvertUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DubboService(group = "${dubbo.group.lofter:lofter}", version = "1.0.0")
public class DeepLinkServiceImpl implements DeepLinkService {

    @Autowired
    private AdDeepLinkDelegate adDeepLinkDelegate;

    @Override
    public List<AdDeeplinkDTO> listAll() {
        return BeanConvertUtils.convertList(adDeepLinkDelegate.listAll(), AdDeeplinkDTO.class);
    }
}
