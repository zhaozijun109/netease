package com.netease.yuanqi.common.utils;

import java.io.IOException;
import org.apache.flink.api.java.utils.ParameterTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Autoload config.properties in resource. */
public class LoadConfigurationUtils {

    private static final Logger LOG = LoggerFactory.getLogger(LoadConfigurationUtils.class);

    public ParameterTool loadConfiguration(String[] args) {
        ParameterTool params = null;
        if (args.length == 0) {
            LOG.info("Load local configuration...");
            try {
                params =
                        ParameterTool.fromPropertiesFile(
                                LoadConfigurationUtils.class
                                        .getClassLoader()
                                        .getResourceAsStream("config.properties"));
            } catch (IOException e) {
                LOG.error("Load local configuration error: {}", e.getMessage());
            }
        } else {
            LOG.info("Load external configuration...");
            try {
                params = ParameterTool.fromPropertiesFile(args[0]);
            } catch (IOException e) {
                LOG.error("Load external configuration error: {}", e.getMessage());
            }
        }
        return params;
    }
}
