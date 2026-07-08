package com.netease.yuanqi.lofter.pojo;

public class DimCardActivity {
    private final Long activityId;
    private final String activityCode;

    public DimCardActivity(Long activityId, String activityCode) {
        this.activityId = activityId;
        this.activityCode = activityCode;
    }

    public Long getActivityId() {
        return activityId;
    }

    public String getActivityCode() {
        return activityCode;
    }

    public static DimCardActivityBuilder builder() {
        return new DimCardActivityBuilder();
    }

    public static class DimCardActivityBuilder {
        public Long activityId;
        public String activityCode;

        public DimCardActivityBuilder() {}

        public DimCardActivityBuilder setActivityId(Long activityId) {
            this.activityId = activityId;
            return this;
        }

        public DimCardActivityBuilder setActivityCode(String activityCode) {
            this.activityCode = activityCode;
            return this;
        }

        public DimCardActivity build() {
            return new DimCardActivity(activityId, activityCode);
        }
    }

    @Override
    public String toString() {
        return "{"
                + "\"activityId\":"
                + activityId
                + ",\"activityCode\":\""
                + activityCode
                + '\"'
                + "}";
    }
}
