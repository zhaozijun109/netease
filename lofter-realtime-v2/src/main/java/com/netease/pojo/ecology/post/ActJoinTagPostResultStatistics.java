package com.netease.pojo.ecology.post;

/** Filed definition of join post user statistics in activity tag. */
public class ActJoinTagPostResultStatistics {
    private String tag;
    private Long taskId;
    private Long userId;
    private Integer validPostCount;
    private Integer qualityPostCount;
    private Integer masterpiecePostCount;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getValidPostCount() {
        return validPostCount;
    }

    public void setValidPostCount(Integer validPostCount) {
        this.validPostCount = validPostCount;
    }

    public Integer getQualityPostCount() {
        return qualityPostCount;
    }

    public void setQualityPostCount(Integer qualityPostCount) {
        this.qualityPostCount = qualityPostCount;
    }

    public Integer getMasterpiecePostCount() {
        return masterpiecePostCount;
    }

    public void setMasterpiecePostCount(Integer masterpiecePostCount) {
        this.masterpiecePostCount = masterpiecePostCount;
    }
}
