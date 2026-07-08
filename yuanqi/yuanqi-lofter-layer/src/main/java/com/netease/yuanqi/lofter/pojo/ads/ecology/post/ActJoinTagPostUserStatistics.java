package com.netease.yuanqi.lofter.pojo.ads.ecology.post;

import java.util.Set;

/** Filed definition of join post user statistics in activity tag. */
public class ActJoinTagPostUserStatistics {
    private String tag;
    private Long taskId;
    private Long userId;
    private Set<Long> validPostIds;
    private Set<Long> qualityPostIds;
    private Set<Long> masterpiecePostIds;

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

    public Set<Long> getValidPostIds() {
        return validPostIds;
    }

    public void setValidPostIds(Set<Long> validPostIds) {
        this.validPostIds = validPostIds;
    }

    public Set<Long> getQualityPostIds() {
        return qualityPostIds;
    }

    public void setQualityPostIds(Set<Long> qualityPostIds) {
        this.qualityPostIds = qualityPostIds;
    }

    public Set<Long> getMasterpiecePostIds() {
        return masterpiecePostIds;
    }

    public void setMasterpiecePostIds(Set<Long> masterpiecePostIds) {
        this.masterpiecePostIds = masterpiecePostIds;
    }
}
