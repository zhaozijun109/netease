package com.netease.yuanqi.common.pojo.archive.ycy.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.avro.reflect.AvroName;
import org.apache.avro.reflect.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AbPlatformSdkLogDto implements KafkaArchiveRecord {
    private static final long serialVersionUID = 1L;

    @Nullable
    @AvroName("ablogversion")
    @JsonProperty("abLogVersion")
    private String abLogVersion;

    @Nullable
    @AvroName("appversion")
    @JsonProperty("appVersion")
    private String appVersion;

    @Nullable
    @AvroName("deviceid")
    @JsonProperty("deviceId")
    private String deviceId;

    @Nullable
    @AvroName("expid")
    @JsonProperty("expId")
    private Long expId;

    @Nullable
    @AvroName("groupid")
    @JsonProperty("groupId")
    private Long groupId;

    @Nullable private String os;

    @Nullable
    @AvroName("sceneid")
    @JsonProperty("sceneId")
    private Integer sceneId;

    @Nullable private Long time;

    @Nullable
    @AvroName("traceid")
    @JsonProperty("traceId")
    private String traceId;

    @Nullable
    @AvroName("userid")
    @JsonProperty("userId")
    private String userId;

    @Nullable
    @AvroName("backend_deviceid")
    @JsonProperty("backend_deviceid")
    private String backendDeviceId;

    @Nullable
    @AvroName("kafka_time")
    private Long kafkaTime;

    @Override
    public Long getKafkaTime() {
        return kafkaTime;
    }

    public void setKafkaTime(Long kafkaTime) {
        this.kafkaTime = kafkaTime;
    }

    public String getAbLogVersion() {
        return abLogVersion;
    }

    public void setAbLogVersion(String abLogVersion) {
        this.abLogVersion = abLogVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Long getExpId() {
        return expId;
    }

    public void setExpId(Long expId) {
        this.expId = expId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public Integer getSceneId() {
        return sceneId;
    }

    public void setSceneId(Integer sceneId) {
        this.sceneId = sceneId;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBackendDeviceId() {
        return backendDeviceId;
    }

    public void setBackendDeviceId(String backendDeviceId) {
        this.backendDeviceId = backendDeviceId;
    }
}
