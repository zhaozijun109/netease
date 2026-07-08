package com.netease.yuanqi.common.pojo.archive.ycy.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.avro.reflect.AvroName;
import org.apache.avro.reflect.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorUpdateDetailsDto implements KafkaArchiveRecord {
    private static final long serialVersionUID = 1L;

    @Nullable
    @AvroName("game_id")
    @JsonProperty("gameId")
    private Long gameId;

    @Nullable
    @AvroName("user_id")
    @JsonProperty("userId")
    private Long userId;

    @Nullable
    @AvroName("code_num_delta")
    @JsonProperty("codeNumDelta")
    private Long codeNumDelta;

    @Nullable
    @AvroName("content_num_delta")
    @JsonProperty("contentNumDelta")
    private Long contentNumDelta;

    @Nullable
    @AvroName("scene_id")
    @JsonProperty("sceneId")
    private Long sceneId;

    @Nullable
    @JsonProperty("source")
    private Long source;

    @Nullable
    @JsonProperty("time")
    private Long occurtime;

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

    public Long getCodeNumDelta() {
        return codeNumDelta;
    }

    public void setCodeNumDelta(Long codeNumDelta) {
        this.codeNumDelta = codeNumDelta;
    }

    public Long getContentNumDelta() {
        return contentNumDelta;
    }

    public void setContentNumDelta(Long contentNumDelta) {
        this.contentNumDelta = contentNumDelta;
    }

    public Long getSceneId() {
        return sceneId;
    }

    public void setSceneId(Long sceneId) {
        this.sceneId = sceneId;
    }

    public Long getSource() {
        return source;
    }

    public void setSource(Long source) {
        this.source = source;
    }

    public Long getOccurtime() {
        return occurtime;
    }

    public void setOccurtime(Long occurtime) {
        this.occurtime = occurtime;
    }
}
