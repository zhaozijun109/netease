package com.netease.yuanqi.lofter.pojo;

public class DimDevice {
    private final String deviceUdid;
    private final Long latestDayFirstTime;

    public DimDevice(String deviceUdid, Long latestDayFirstTime) {
        this.deviceUdid = deviceUdid;
        this.latestDayFirstTime = latestDayFirstTime;
    }

    public static DimDeviceBuilder builder() {
        return new DimDeviceBuilder();
    }

    public String getDeviceUdid() {
        return deviceUdid;
    }

    public Long getLatestDayFirstTime() {
        return latestDayFirstTime;
    }

    public static class DimDeviceBuilder {
        private String deviceUdid;
        private Long latestDayFirstTime;

        public DimDeviceBuilder setDeviceUdid(String deviceUdid) {
            this.deviceUdid = deviceUdid;
            return this;
        }

        public DimDeviceBuilder setLatestDayFirstTime(Long latestDayFirstTime) {
            this.latestDayFirstTime = latestDayFirstTime;
            return this;
        }

        public DimDevice build() {
            return new DimDevice(deviceUdid, latestDayFirstTime);
        }
    }

    @Override
    public String toString() {
        return "{"
                + "deviceUdid:\""
                + deviceUdid
                + "\", latestDayFirstTime:"
                + latestDayFirstTime
                + '}';
    }
}
