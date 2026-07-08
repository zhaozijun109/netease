package com.netease.yuanqi.common.pojo.config;

import java.io.Serializable;

public class KerberosConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String coreSitePath;
    private final String hdfsSitePath;
    private final String krb5ConfPath;
    private final String keyTabPath;
    private final String keyTabLoginUser;
    private final Boolean isRelatedMammoth;

    public KerberosConfig(
            String coreSitePath,
            String hdfsSitePath,
            String krb5ConfPath,
            String keyTabPath,
            String keyTabLoginUser,
            Boolean isRelatedMammoth) {
        this.coreSitePath = coreSitePath;
        this.hdfsSitePath = hdfsSitePath;
        this.krb5ConfPath = krb5ConfPath;
        this.keyTabPath = keyTabPath;
        this.keyTabLoginUser = keyTabLoginUser;
        this.isRelatedMammoth = isRelatedMammoth;
    }

    public String getCoreSitePath() {
        return coreSitePath;
    }

    public String getHdfsSitePath() {
        return hdfsSitePath;
    }

    public String getKrb5ConfPath() {
        return krb5ConfPath;
    }

    public String getKeyTabPath() {
        return keyTabPath;
    }

    public String getKeyTabLoginUser() {
        return keyTabLoginUser;
    }

    public Boolean getRelatedMammoth() {
        return isRelatedMammoth;
    }

    public static KerberosConfigBuilder builder() {
        return new KerberosConfigBuilder();
    }

    public static class KerberosConfigBuilder {
        private String coreSitePath;
        private String hdfsSitePath;
        private String krb5ConfPath;
        private String keyTabPath;
        private String keyTabLoginUser;
        private final Boolean isRelatedMammoth;

        public KerberosConfigBuilder() {
            this.isRelatedMammoth = false;
        }

        public KerberosConfigBuilder withCoreSitePath(String coreSitePath) {
            this.coreSitePath = coreSitePath;
            return this;
        }

        public KerberosConfigBuilder withHdfsSitePath(String hdfsSitePath) {
            this.hdfsSitePath = hdfsSitePath;
            return this;
        }

        public KerberosConfigBuilder withKrb5ConfPath(String krb5ConfPath) {
            this.krb5ConfPath = krb5ConfPath;
            return this;
        }

        public KerberosConfigBuilder withKeyTabPath(String keyTabPath) {
            this.keyTabPath = keyTabPath;
            return this;
        }

        public KerberosConfigBuilder withKeyTabLoginUser(String keyTabLoginUser) {
            this.keyTabLoginUser = keyTabLoginUser;
            return this;
        }

        public KerberosConfig build() {
            return new KerberosConfig(
                    coreSitePath,
                    hdfsSitePath,
                    krb5ConfPath,
                    keyTabPath,
                    keyTabLoginUser,
                    isRelatedMammoth);
        }
    }

    @Override
    public String toString() {
        return "{"
                + "\"coreSitePath\":\""
                + coreSitePath
                + '\"'
                + ",\"hdfsSitePath\":\""
                + hdfsSitePath
                + '\"'
                + ",\"krb5ConfPath\":\""
                + krb5ConfPath
                + '\"'
                + ",\"keyTabPath\":\""
                + keyTabPath
                + '\"'
                + ",\"keyTabLoginUser\":\""
                + keyTabLoginUser
                + '\"'
                + ",\"isRelatedMammoth\":"
                + isRelatedMammoth
                + "}";
    }
}
