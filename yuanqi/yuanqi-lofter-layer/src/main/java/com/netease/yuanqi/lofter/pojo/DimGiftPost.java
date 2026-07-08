package com.netease.yuanqi.lofter.pojo;

public class DimGiftPost {
    private final Long postId;
    private final Long blogId;
    private final Long giftTime;
    private final Integer isPgc;

    public DimGiftPost(Long postId, Long blogId, Long giftTime, Integer isPgc) {
        this.postId = postId;
        this.blogId = blogId;
        this.giftTime = giftTime;
        this.isPgc = isPgc;
    }

    public Long getPostId() {
        return postId;
    }

    public Long getBlogId() {
        return blogId;
    }

    public Long getGiftTime() {
        return giftTime;
    }

    public Integer getIsPgc() {
        return isPgc;
    }

    public static DimGiftPostBuilder builder() {
        return new DimGiftPostBuilder();
    }

    public static class DimGiftPostBuilder {
        private Long postId;
        private Long blogId;
        private Long giftTime;
        private Integer isPgc;

        public DimGiftPostBuilder setPostId(Long postId) {
            this.postId = postId;
            return this;
        }

        public DimGiftPostBuilder setBlogId(Long blogId) {
            this.blogId = blogId;
            return this;
        }

        public DimGiftPostBuilder setGiftTime(Long giftTime) {
            this.giftTime = giftTime;
            return this;
        }

        public DimGiftPostBuilder setIsPgc(Integer isPgc) {
            this.isPgc = isPgc;
            return this;
        }

        public DimGiftPost build() {
            return new DimGiftPost(postId, blogId, giftTime, isPgc);
        }
    }

    @Override
    public String toString() {
        return "{"
                + "\"postId\":"
                + postId
                + ",\"blogId\":"
                + blogId
                + ",\"giftTime\":"
                + giftTime
                + ",\"isPgc\":"
                + isPgc
                + "}";
    }
}
