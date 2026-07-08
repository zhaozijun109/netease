package com.netease.lofter.tango.impl.helper;

import com.netease.lofter.tango.api.dto.config.TangoConfigDTO;
import com.netease.lofter.tango.impl.delegate.TangoConfigDelegate;
import com.netease.lofter.tango.impl.entity.TangoConfigDO;
import com.netease.mm.tk.common.util.BeanConvertUtils;
import com.netease.mm.tk.common.util.lang.CollectionUtils3;
import com.netease.yaolu.commons.spring.cache.annotation.CacheKeys;
import com.netease.yaolu.commons.spring.cache.annotation.MultiCache;
import com.netease.yaolu.commons.spring.cache.annotation.SingleInvalidCache;
import groovy.lang.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class TangoConfigHelper {

    @Autowired
    private TangoConfigDelegate tangoConfigDelegate;

    @Autowired
    @Lazy
    private TangoConfigHelper tangoConfigHelper;

    @Autowired
    private ProfileEnv profileEnv;


    public Map<String, TangoConfigDTO> listByAppIdAndKey(String appId, @CacheKeys Collection<String> keys) {
        return tangoConfigHelper.listFromCache(profileEnv.env(), appId, keys);
    }


    @MultiCache(useClassName = false,
            namespace = "#cluster+#appId",
            namespaceExpression = true,
            expireSeconds = 3600)
    public Map<String, TangoConfigDTO> listFromCache(String cluster, String appId, @CacheKeys Collection<String> keys) {
        List<TangoConfigDO> tangoConfigDOS = tangoConfigDelegate.listByAppIdAndKey(appId, cluster, keys);
        return CollectionUtils3.toMap(tangoConfigDOS,
                TangoConfigDO::getConfigKey,
                item -> BeanConvertUtils.convertBean(item, TangoConfigDTO.class));
    }


    public void invalidateCache(String appId, String key) {
        if (!isFrontend(appId)) {
            return;
        }
        tangoConfigHelper.invalidateCache(profileEnv.env(), appId, key);
    }

    @SingleInvalidCache(useClassName = false, namespace = "#cluster+#appId", namespaceExpression = true, key = "#key")
    public void invalidateCache(String cluster, String appId, String key) {

    }

    public boolean isFrontend(String appId) {
        return appId.startsWith("lofter-frontend-");
    }
}
