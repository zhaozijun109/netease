package com.netease.yuanqi.lofter.pojo.ads.ecology.post;

/** 最终输出: tag + postId + blogId 维度的热度与得分统计. */
public class TagIndexScore {
    private String tag;
    private Long postId;
    private Long blogId;
    private Double newHot;
    private Long oldPostHot;
    private Double newScore;
    private Integer type; // 0:存量, 1:增量

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

    public Double getNewHot() {
        return newHot;
    }

    public void setNewHot(Double newHot) {
        this.newHot = newHot;
    }

    public Long getOldPostHot() {
        return oldPostHot;
    }

    public void setOldPostHot(Long oldPostHot) {
        this.oldPostHot = oldPostHot;
    }

    public Double getNewScore() {
        return newScore;
    }

    public void setNewScore(Double newScore) {
        this.newScore = newScore;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
