package com.netease.lofter.tango.impl.helper;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProfileEnv {

    @Value("${apollo.cluster}")
    private String cluster;

    public static final String ENV_PRO = "pro";
    public static final String ENV_PRE = "pre";
    public static final String ENV_TEST = "test";
    public static final String ENV_DEV = "dev";

    public boolean isOnline() {
        return ENV_PRO.equalsIgnoreCase(cluster);
    }

    public boolean isTest() {
        return ENV_TEST.equalsIgnoreCase(cluster);
    }

    public boolean isDev() {
        return ENV_DEV.equalsIgnoreCase(cluster);
    }

    public String getEnv() {
        return cluster;
    }

    public boolean isPre() {
        return isPre(cluster);
    }

    public boolean isPre(String env) {
        return ENV_PRE.equalsIgnoreCase(env);
    }

    public boolean isTestMode() {
        return isTestMode(cluster);
    }

    public boolean isTestMode(String env) {
        return ENV_DEV.equalsIgnoreCase(env) || ENV_TEST.equalsIgnoreCase(env);
    }

    public String env() {
        return cluster;
    }
}
