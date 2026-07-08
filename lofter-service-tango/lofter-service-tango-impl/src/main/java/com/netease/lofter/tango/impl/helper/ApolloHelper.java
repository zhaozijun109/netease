package com.netease.lofter.tango.impl.helper;

import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenEnvClusterDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.netease.lofter.tango.api.consts.TangoConfigOpType;
import com.netease.lofter.tango.impl.consts.CommonProperties;
import com.netease.lofter.tango.impl.web.vo.TangoConfigOpHistoryVO;
import com.netease.lofter.tango.integration.apollo.meta.TangoChangeEvent;
import com.netease.lofter.tango.integration.consts.TangoIntegrationConsts;
import com.netease.yaolu.lofter.core.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author baizhichao
 */
@Component
public class ApolloHelper {

    private static final String PROXY = "guleiyang";
    private final ApolloOpenApiClient apolloOpenApiClient;
    @Value("${app.id:${spring.application.name}}")
    private String appId;
    @Value("${apollo.cluster}")
    private String currentCluster;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public ApolloHelper(CommonProperties commonProperties) {
        this.apolloOpenApiClient = ApolloOpenApiClient.newBuilder().withPortalUrl(commonProperties.getApolloOpenApi().getPortalUrl())
                .withToken(commonProperties.getApolloOpenApi().getToken()).withConnectTimeout(3000)
                .withReadTimeout(3000).build();
    }


    public void publish(TangoConfigOpHistoryVO tangoConfigOpHistory, TangoConfigOpType tangoConfigOpType) {
        TangoChangeEvent tangoChangeEvent = new TangoChangeEvent();
        tangoChangeEvent.setConfigKey(tangoConfigOpHistory.getConfigKey());
        tangoChangeEvent.setConfigValue(tangoConfigOpHistory.getNewValue());
        tangoChangeEvent.setConfigOldValue(tangoConfigOpHistory.getOldValue());
        tangoChangeEvent.setAppId(tangoConfigOpHistory.getAppId());
        tangoChangeEvent.setEventType(tangoConfigOpType.name());
        Long increment = Optional
                .ofNullable(stringRedisTemplate.opsForValue().increment("tango:configcenter:seq", 1))
                .orElse(ThreadLocalRandom.current().nextLong(0, 100));
        int seq = (int) (increment % 200);
        String key = TangoIntegrationConsts.TANGO_CONFIG_CHANEGE_EVENT_KEY_PATTERN + "." + seq;
        String value = JsonUtils.toJsonString(tangoChangeEvent);
        persist(key, value);
    }


    public void persist(String key, String value) {
        List<OpenEnvClusterDTO> clusterDTOS = apolloOpenApiClient.getEnvClusterInfo(appId);
        for (OpenEnvClusterDTO clusterDTO : clusterDTOS) {
            for (String cluster : clusterDTO.getClusters()) {
                if (!cluster.equalsIgnoreCase(currentCluster)) {
                    continue;
                }
                OpenItemDTO itemDTO = new OpenItemDTO();
                itemDTO.setDataChangeCreatedBy(PROXY);
                itemDTO.setDataChangeLastModifiedBy(PROXY);
                itemDTO.setDataChangeLastModifiedTime(new Date());
                itemDTO.setKey(key);
                itemDTO.setValue(value);
                itemDTO.setComment("Tango配置后台修改");
                apolloOpenApiClient.createOrUpdateItem(appId, clusterDTO.getEnv(), currentCluster, TangoIntegrationConsts.TANGO_NAMESPACE, itemDTO);

                NamespaceReleaseDTO releaseDTO = new NamespaceReleaseDTO();
                releaseDTO.setReleaseTitle(System.currentTimeMillis() + "-release");
                releaseDTO.setReleasedBy(PROXY);
                releaseDTO.setEmergencyPublish(true);
                apolloOpenApiClient.publishNamespace(appId, clusterDTO.getEnv(), currentCluster, TangoIntegrationConsts.TANGO_NAMESPACE, releaseDTO);
            }

        }
    }
}
