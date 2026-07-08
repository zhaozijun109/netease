package com.netease.yuanqi.lofter.pojo.ads.ecology.post;

/**
 * 离线 SQL ads_ecology_tag_index_score_di 实时化的"上游 → 下游窗口"中间事件.
 *
 * <p>每条 binlog（PostHot 或 PostResponse）经过 FlatMap 过滤 + 维度补齐后产生一条本对象：6 个 cnt 中只有"对应那一个"为 1，其余为 0； tag
 * 取自 explode 后的单 tag；shipNum 取自 Tag_UserShipInfo 维表（命中白名单且查到才下发）。
 *
 * <p>下游 ProcessWindowFunction 对 6 个 cnt 做 sum 聚合，再按公式计算 newPostHot/oldPostHot/newUserScore.
 */
public class TagPostUserHotEvent {
    private String tag;
    private Long postId;
    private Long blogId;
    private Long userId;
    private Long shipNum;
    private Long likeCnt;
    private Long reproduceCnt;
    private Long recommendCnt;
    private Long collectCnt;
    private Long commentCnt;
    private Long underscoreCircleCommentCnt;

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getShipNum() {
        return shipNum;
    }

    public void setShipNum(Long shipNum) {
        this.shipNum = shipNum;
    }

    public Long getLikeCnt() {
        return likeCnt;
    }

    public void setLikeCnt(Long likeCnt) {
        this.likeCnt = likeCnt;
    }

    public Long getReproduceCnt() {
        return reproduceCnt;
    }

    public void setReproduceCnt(Long reproduceCnt) {
        this.reproduceCnt = reproduceCnt;
    }

    public Long getRecommendCnt() {
        return recommendCnt;
    }

    public void setRecommendCnt(Long recommendCnt) {
        this.recommendCnt = recommendCnt;
    }

    public Long getCollectCnt() {
        return collectCnt;
    }

    public void setCollectCnt(Long collectCnt) {
        this.collectCnt = collectCnt;
    }

    public Long getCommentCnt() {
        return commentCnt;
    }

    public void setCommentCnt(Long commentCnt) {
        this.commentCnt = commentCnt;
    }

    public Long getUnderscoreCircleCommentCnt() {
        return underscoreCircleCommentCnt;
    }

    public void setUnderscoreCircleCommentCnt(Long underscoreCircleCommentCnt) {
        this.underscoreCircleCommentCnt = underscoreCircleCommentCnt;
    }
}
