package com.netease.pojo.ecology.post;

import java.util.Set;

/** Filed definition of join post in activity tag. */
public class ActJoinTagPost {
    private Long postId;
    private Long blogId;
    private Integer status;
    private Long modifyTime;
    private Set<Long> actTaskIds;

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getBlogId() {
        return blogId;
    }

    public void setBlogId(Long blogId) {
        this.blogId = blogId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public Set<Long> getActTaskIds() {
        return actTaskIds;
    }

    public void setActTaskIds(Set<Long> actTaskIds) {
        this.actTaskIds = actTaskIds;
    }
}
