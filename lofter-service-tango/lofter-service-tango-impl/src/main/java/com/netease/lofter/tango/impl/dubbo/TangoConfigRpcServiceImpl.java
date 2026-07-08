package com.netease.lofter.tango.impl.dubbo;

import com.netease.lofter.tango.api.client.config.TangoConfigRpcService;
import com.netease.lofter.tango.api.dto.config.TangoConfigDTO;
import com.netease.lofter.tango.impl.delegate.TangoConfigDelegate;
import com.netease.lofter.tango.impl.entity.TangoConfigDO;
import com.netease.lofter.tango.impl.helper.TangoConfigHelper;
import com.netease.mm.tk.common.util.BeanConvertUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.List;

@DubboService(group = "${dubbo.group.lofter:lofter}", version = "1.0.0")
public class TangoConfigRpcServiceImpl implements TangoConfigRpcService {

    @Value("${tango.config.access.token}")
    private String token;

    @Autowired
    private TangoConfigDelegate tangoConfigDelegate;
    @Autowired
    private TangoConfigHelper tangoConfigHelper;

    @Override
    public List<TangoConfigDTO> listByAppId(String env, String appId) {
        return BeanConvertUtils.convertList(tangoConfigDelegate.listByAppId(appId, env), this::populate2DTO);
    }

    @Override
    public TangoConfigDTO getByAppIdAndKey(String env, String appId, String key) {
        return populate2DTO(tangoConfigDelegate.selectByAppIdAndKey(appId, key, env));
    }

    public List<TangoConfigDTO> listByAppIdAndToken(String env, String appId, String token) {
        if (!this.token.equalsIgnoreCase(token)) {
            return Collections.emptyList();
        }
        return BeanConvertUtils.convertList(tangoConfigDelegate.listByAppId(appId, env), this::populate2DTO);
    }

    private TangoConfigDTO populate2DTO(TangoConfigDO tangoConfigDO) {
        TangoConfigDTO tangoConfigDTO = BeanConvertUtils.convertBean(tangoConfigDO, TangoConfigDTO.class);
        return tangoConfigDTO;
    }

}
