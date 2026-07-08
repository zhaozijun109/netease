package com.netease.lofter.tango.integration.dubbo;

import com.netease.lofter.tango.integration.util.AssertUtils;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.utils.ReferenceConfigCache;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.springframework.core.env.Environment;

import static com.netease.lofter.tango.integration.consts.TangoIntegrationConsts.DUBBO_GROUP_LOFTER_KEY;
import static com.netease.lofter.tango.integration.consts.TangoIntegrationConsts.DUBBO_REGISTRY_KEY;

public class DubboUtils {

    /**
     * 手动注册dubbo，bean原因初始化较早，不影响其它正常注册的dubbo bean
     *
     * @param serviceClass
     * @param environment
     * @param <T>
     * @return
     */
    public static <T> T getManualRegisterReference(Class<T> serviceClass, Environment environment) {
        T reference = ReferenceConfigCache.getCache().get(serviceClass);
        if (null != reference) {
            return reference;
        }
        ApplicationConfig applicationConfig = ApplicationModel.getApplicationConfig();
        AssertUtils.isTrue(applicationConfig.getName() != null, "application name is null");
        RegistryConfig registry = new RegistryConfig();
        registry.setAddress(environment.getRequiredProperty(DUBBO_REGISTRY_KEY));

        ReferenceConfig<T> referenceConfig = new ReferenceConfig<>(); // 此实例很重，封装了与注册中心的连接以及与提供者的连接，请自行缓存，否则可能造成内存和连接泄漏
        referenceConfig.setInterface(serviceClass);
        referenceConfig.setGroup(environment.getProperty(DUBBO_GROUP_LOFTER_KEY, "lofter"));
        referenceConfig.setVersion("1.0.0");
        referenceConfig.setRegistry(registry);

        return ReferenceConfigCache.getCache().get(referenceConfig);
    }

}
