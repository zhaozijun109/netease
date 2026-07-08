package com.netease.yuanqi.unified.pojo;

public class RecParsedLogEvents {
    private final String eventId;
    private final String deviceUdid;
    private final String appVersion;
    private final String source;
    private final Integer action;

    public RecParsedLogEvents(
            String eventId, String deviceUdid, String appVersion, String source, Integer action) {
        this.eventId = eventId;
        this.deviceUdid = deviceUdid;
        this.appVersion = appVersion;
        this.source = source;
        this.action = action;
    }

    public String getEventId() {
        return eventId;
    }

    public String getDeviceUdid() {
        return deviceUdid;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getSource() {
        return source;
    }

    public Integer getAction() {
        return action;
    }

    public static RecParsedLogEventsBuilder builder() {
        return new RecParsedLogEventsBuilder();
    }

    public static class RecParsedLogEventsBuilder {
        private String eventId;
        private String deviceUdid;
        private String appVersion;
        private String source;
        private Integer action;

        public RecParsedLogEventsBuilder() {}

        public RecParsedLogEventsBuilder setEventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public RecParsedLogEventsBuilder setDeviceUdid(String deviceUdid) {
            this.deviceUdid = deviceUdid;
            return this;
        }

        public RecParsedLogEventsBuilder setAppVersion(String appVersion) {
            this.appVersion = appVersion;
            return this;
        }

        public RecParsedLogEventsBuilder setSource(String source) {
            this.source = source;
            return this;
        }

        public RecParsedLogEventsBuilder setAction(Integer action) {
            this.action = action;
            return this;
        }

        public RecParsedLogEvents build() {
            return new RecParsedLogEvents(eventId, deviceUdid, appVersion, source, action);
        }
    }

    @Override
    public String toString() {
        return "{"
                + "\"eventId\":\""
                + eventId
                + '\"'
                + ",\"deviceUdid\":\""
                + deviceUdid
                + '\"'
                + ",\"appVersion\":\""
                + appVersion
                + '\"'
                + ",\"source\":\""
                + source
                + '\"'
                + ",\"action\":"
                + action
                + "}";
    }
}
