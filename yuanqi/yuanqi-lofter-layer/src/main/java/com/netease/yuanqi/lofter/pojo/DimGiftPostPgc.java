package com.netease.yuanqi.lofter.pojo;

public class DimGiftPostPgc {
    private final Long userId;
    private final Long blogId;

    public DimGiftPostPgc(Long userId, Long blogId) {
        this.userId = userId;
        this.blogId = blogId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getBlogId() {
        return blogId;
    }

    public static DimGiftPostPgcBuilder builder() {
        return new DimGiftPostPgcBuilder();
    }

    public static class DimGiftPostPgcBuilder {
        private Long userId;
        private Long blogId;

        public DimGiftPostPgcBuilder setUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public DimGiftPostPgcBuilder setBlogId(Long blogId) {
            this.blogId = blogId;
            return this;
        }

        public DimGiftPostPgc build() {
            return new DimGiftPostPgc(userId, blogId);
        }
    }

    @Override
    public String toString() {
        return "{" + "\"userId\":" + userId + ",\"blogId\":" + blogId + "}";
    }
}
