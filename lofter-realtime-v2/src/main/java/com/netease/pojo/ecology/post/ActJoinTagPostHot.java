package com.netease.pojo.ecology.post;

/** Filed definition of join post hot in activity tag. */
public class ActJoinTagPostHot {
    private String tag;
    private Long postId;
    private Long blogId;
    private Integer postStatus;
    private Long postModifyTime;
    private Long actTaskId;
    private Long hot;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

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

    public Integer getPostStatus() {
        return postStatus;
    }

    public void setPostStatus(Integer postStatus) {
        this.postStatus = postStatus;
    }

    public Long getPostModifyTime() {
        return postModifyTime;
    }

    public void setPostModifyTime(Long postModifyTime) {
        this.postModifyTime = postModifyTime;
    }

    public Long getActTaskId() {
        return actTaskId;
    }

    public void setActTaskId(Long actTaskId) {
        this.actTaskId = actTaskId;
    }

    public Long getHot() {
        return hot;
    }

    public void setHot(Long hot) {
        this.hot = hot;
    }
}
