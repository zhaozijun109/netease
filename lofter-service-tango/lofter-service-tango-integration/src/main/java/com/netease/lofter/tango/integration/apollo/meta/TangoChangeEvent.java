package com.netease.lofter.tango.integration.apollo.meta;

import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.netease.lofter.tango.api.consts.TangoConfigOpType;
import com.netease.lofter.tango.api.dto.config.TangoConfigDTO;
import com.netease.lofter.tango.integration.consts.TangoIntegrationConsts;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TangoChangeEvent extends TangoConfigDTO {
    private static final long serialVersionUID = -2837338817608800926L;

    private String eventType;

    private String configOldValue;

    public static TangoChangeEvent from(ConfigChange configChange) {
        TangoChangeEvent tangoChangeEvent = new TangoChangeEvent();
        tangoChangeEvent.setEventType(configChange.getChangeType().name());
        tangoChangeEvent.setConfigKey(configChange.getPropertyName());
        tangoChangeEvent.setConfigValue(configChange.getNewValue());
        tangoChangeEvent.setConfigOldValue(configChange.getOldValue());
        return tangoChangeEvent;
    }

    public ConfigChange adaptApolloConfig() {
        ConfigChange configChange = new ConfigChange(TangoIntegrationConsts.TANGO_NAMESPACE, getConfigKey(), getConfigOldValue(), getConfigValue(), adaptApolloPropertyChangeType());
        if (configChange.getChangeType() == PropertyChangeType.DELETED) {
            configChange.setNewValue(null);
        }
        return configChange;
    }

    private PropertyChangeType adaptApolloPropertyChangeType() {
        TangoConfigOpType tangoConfigOpType = TangoConfigOpType.valueOf(getEventType());
        switch (tangoConfigOpType) {
            case ADD:
                return PropertyChangeType.ADDED;
            case UPDATE:
            case ROLLBACK:
                return PropertyChangeType.MODIFIED;
            case DELETE:
                return PropertyChangeType.DELETED;
        }
        throw new IllegalArgumentException("Unknown event type: " + getEventType());
    }


}
