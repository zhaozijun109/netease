package com.netease.yuanqi.common.pojo.archive.ycy.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.avro.reflect.AvroName;
import org.apache.avro.reflect.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GameClickRecordDto implements KafkaArchiveRecord {
    private static final long serialVersionUID = 1L;

    @Nullable
    @JsonProperty("id")
    private Long id;

    @Nullable
    @AvroName("game_id")
    @JsonProperty("gameId")
    private Long gameId;

    @Nullable
    @AvroName("user_id")
    @JsonProperty("userId")
    private Long userId;

    @Nullable
    @JsonProperty("multiple")
    private Integer multiple;

    @Nullable
    @JsonProperty("weight")
    private Double weight;

    @Nullable
    @AvroName("os_name")
    @JsonProperty("osName")
    private String osName;

    @Nullable
    @AvroName("create_time")
    @JsonProperty("createTime")
    private Long createTime;

    @Nullable
    @JsonProperty("date")
    private String date;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getMultiple() {
        return multiple;
    }

    public void setMultiple(Integer multiple) {
        this.multiple = multiple;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
