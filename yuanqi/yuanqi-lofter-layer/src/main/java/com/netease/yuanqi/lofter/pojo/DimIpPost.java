package com.netease.yuanqi.lofter.pojo;

public class DimIpPost {
    private final Long postId;
    private final Long blogId;
    private final Long publishTime;
    private final String tag;
    private final String blogLevel;
    private final String ip;
    private final Integer isInRecommendPool;

    public DimIpPost(
            Long postId,
            Long blogId,
            Long publishTime,
            String tag,
            String blogLevel,
            String ip,
            Integer isInRecommendPool) {
        this.postId = postId;
        this.blogId = blogId;
        this.publishTime = publishTime;
        this.tag = tag;
        this.blogLevel = blogLevel;
        this.ip = ip;
        this.isInRecommendPool = isInRecommendPool;
    }

    public Long getPostId() {
        return postId;
    }

    public Long getBlogId() {
        return blogId;
    }

    public Long getPublishTime() {
        return publishTime;
    }

    public String getTag() {
        return tag;
    }

    public String getBlogLevel() {
        return blogLevel;
    }

    public String getIp() {
        return ip;
    }

    public Integer getIsInRecommendPool() {
        return isInRecommendPool;
    }

    public static DimIpPostBuilder builder() {
        return new DimIpPostBuilder();
    }

    public static class DimIpPostBuilder {
        private Long postId;
        private Long blogId;
        private Long publishTime;
        private String tag;
        private String blogLevel;
        private String ip;
        private Integer isInRecommendPool;

        public DimIpPostBuilder setPostId(Long postId) {
            this.postId = postId;
            return this;
        }

        public DimIpPostBuilder setBlogId(Long blogId) {
            this.blogId = blogId;
            return this;
        }

        public DimIpPostBuilder setPublishTime(Long publishTime) {
            this.publishTime = publishTime;
            return this;
        }

        public DimIpPostBuilder setTag(String tag) {
            this.tag = tag;
            return this;
        }

        public DimIpPostBuilder setBlogLevel(String blogLevel) {
            this.blogLevel = blogLevel;
            return this;
        }

        public DimIpPostBuilder setIp(String ip) {
            this.ip = ip;
            return this;
        }

        public DimIpPostBuilder setIsInRecommendPool(Integer isInRecommendPool) {
            this.isInRecommendPool = isInRecommendPool;
            return this;
        }

        public DimIpPost build() {
            return new DimIpPost(
                    postId, blogId, publishTime, tag, blogLevel, ip, isInRecommendPool);
        }
    }

    @Override
    public String toString() {
        return "{"
                + "\"postId\":"
                + postId
                + ",\"blogId\":"
                + blogId
                + ",\"publishTime\":"
                + publishTime
                + ",\"tag\":\""
                + tag
                + '\"'
                + ",\"blogLevel\":\""
                + blogLevel
                + '\"'
                + ",\"ip\":\""
                + ip
                + '\"'
                + ",\"isInRecommendPool\":"
                + isInRecommendPool
                + "}";
    }
}
