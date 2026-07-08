package com.netease.yuanqi.common.pojo.archive.ycy.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.avro.reflect.AvroName;
import org.apache.avro.reflect.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentLogDto implements KafkaArchiveRecord {
    private static final long serialVersionUID = 1L;

    @Nullable
    @AvroName("user_id")
    @JsonProperty("user_id")
    private Long userId;

    @Nullable
    @AvroName("game_id")
    @JsonProperty("game_id")
    private Long gameId;

    @Nullable
    @AvroName("comment_id")
    @JsonProperty("comment_id")
    private Long commentId;

    @Nullable
    @JsonProperty("comment")
    private String comment;

    @Nullable
    @AvroName("update_time")
    @JsonProperty("update_time")
    private Long updateTime;

    @Nullable
    @JsonProperty("type")
    private Integer type;

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
