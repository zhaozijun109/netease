package com.netease.yuanqi.common.pojo.archive.ycy.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.avro.reflect.AvroName;
import org.apache.avro.reflect.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentRecognitionDto implements KafkaArchiveRecord {
    private static final long serialVersionUID = 1L;

    @Nullable
    @AvroName("comment_id")
    @JsonProperty("comment_id")
    private String commentId;

    @Nullable
    @AvroName("user_id")
    @JsonProperty("user_id")
    private String userId;

    @Nullable
    @AvroName("game_id")
    @JsonProperty("game_id")
    private String gameId;

    @Nullable
    @JsonProperty("result")
    private String result;

    @Nullable
    @AvroName("create_time")
    @JsonProperty("create_time")
    private String createTime;

    @Nullable
    @JsonProperty("response")
    private String response;

    @Nullable
    @AvroName("status_code")
    @JsonProperty("status_code")
    private String statusCode;

    @Nullable
    @JsonProperty("comment")
    private String comment;

    @Nullable
    @JsonProperty("type")
    private String type;

    @Nullable
    @JsonProperty("title")
    private String title;

    @Nullable
    @JsonProperty("images")
    private String images;

    @Nullable
    @JsonProperty("videos")
    private String videos;

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

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getVideos() {
        return videos;
    }

    public void setVideos(String videos) {
        this.videos = videos;
    }
}
