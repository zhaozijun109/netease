package com.netease.yuanqi.lofter.pojo;

public class DimPost {
    private final Long postId;
    private final Long blogId;
    private final Long publishTime;

    public DimPost(Long postId, Long blogId, Long publishTime) {
        this.postId = postId;
        this.blogId = blogId;
        this.publishTime = publishTime;
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

    public static DimPostBuilder builder() {
        return new DimPostBuilder();
    }

    public static class DimPostBuilder {
        private Long postId;
        private Long blogId;
        private Long publishTime;

        public DimPostBuilder setPostId(Long postId) {
            this.postId = postId;
            return this;
        }

        public DimPostBuilder setBlogId(Long blogId) {
            this.blogId = blogId;
            return this;
        }

        public DimPostBuilder setPublishTime(Long publishTime) {
            this.publishTime = publishTime;
            return this;
        }

        public DimPost build() {
            return new DimPost(postId, blogId, publishTime);
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
                + "}";
    }
}
