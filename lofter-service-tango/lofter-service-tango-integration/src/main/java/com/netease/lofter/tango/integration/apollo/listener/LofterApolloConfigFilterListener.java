package com.netease.lofter.tango.integration.apollo.listener;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.netease.lofter.tango.integration.consts.TangoIntegrationConsts;

public class LofterApolloConfigFilterListener implements ConfigChangeListener {

    private ConfigChangeListener delagete;

    public LofterApolloConfigFilterListener(ConfigChangeListener delagete) {
        this.delagete = delagete;
    }

    public ConfigChangeListener getDelagete() {
        return delagete;
    }

    @Override
    public void onChange(ConfigChangeEvent changeEvent) {
        if (TangoIntegrationConsts.TANGO_NAMESPACE.equalsIgnoreCase(changeEvent.getNamespace())) {
            if (!(delagete instanceof TangoConfigListener)) {
                return;
            }
        }
        delagete.onChange(changeEvent);
    }
}
