package com.netease.yuanqi.unified.pojo;

public class DimRecBuriedPoint {
    private final Long id;
    private final String businessName;
    private final String dataSource;
    private final String eventId;
    private final String appVersion;
    private final Integer actionCode;
    private final Integer enable;

    public DimRecBuriedPoint(
            Long id,
            String businessName,
            String dataSource,
            String eventId,
            String appVersion,
            Integer actionCode,
            Integer enable) {
        this.id = id;
        this.businessName = businessName;
        this.dataSource = dataSource;
        this.eventId = eventId;
        this.appVersion = appVersion;
        this.actionCode = actionCode;
        this.enable = enable;
    }

    public Long getId() {
        return id;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getDataSource() {
        return dataSource;
    }

    public String getEventId() {
        return eventId;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public Integer getActionCode() {
        return actionCode;
    }

    public Integer getEnable() {
        return enable;
    }

    public static DimRecBuriedPointBuilder builder() {
        return new DimRecBuriedPointBuilder();
    }

    public static class DimRecBuriedPointBuilder {
        private Long id;
        private String businessName;
        private String dataSource;
        private String eventId;
        private String appVersion;
        private Integer actionCode;
        private Integer enable;

        public DimRecBuriedPointBuilder setId(Long id) {
            this.id = id;
            return this;
        }

        public DimRecBuriedPointBuilder setBusinessName(String businessName) {
            this.businessName = businessName;
            return this;
        }

        public DimRecBuriedPointBuilder setDataSource(String dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        public DimRecBuriedPointBuilder setEventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public DimRecBuriedPointBuilder setAppVersion(String appVersion) {
            this.appVersion = appVersion;
            return this;
        }

        public DimRecBuriedPointBuilder setActionCode(Integer actionCode) {
            this.actionCode = actionCode;
            return this;
        }

        public DimRecBuriedPointBuilder setEnable(Integer enable) {
            this.enable = enable;
            return this;
        }

        public DimRecBuriedPoint build() {
            return new DimRecBuriedPoint(
                    id, businessName, dataSource, eventId, appVersion, actionCode, enable);
        }
    }

    @Override
    public String toString() {
        return "{"
                + "\"id\":"
                + id
                + ",\"businessName\":\""
                + businessName
                + '\"'
                + ",\"dataSource\":\""
                + dataSource
                + '\"'
                + ",\"eventId\":\""
                + eventId
                + '\"'
                + ",\"appVersion\":\""
                + appVersion
                + '\"'
                + ",\"actionCode\":"
                + actionCode
                + ",\"enable\":"
                + enable
                + "}";
    }
}
