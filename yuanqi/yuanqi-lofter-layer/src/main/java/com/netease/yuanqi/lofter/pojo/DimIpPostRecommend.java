package com.netease.yuanqi.lofter.pojo;

public class DimIpPostRecommend {
    private final Long postId;
    private final Integer recommendStatus;
    private final Integer isInRecommendPool;

    public DimIpPostRecommend(Long postId, Integer recommendStatus, Integer isInRecommendPool) {
        this.postId = postId;
        this.recommendStatus = recommendStatus;
        this.isInRecommendPool = isInRecommendPool;
    }

    public Long getPostId() {
        return postId;
    }

    public Integer getRecommendStatus() {
        return recommendStatus;
    }

    public Integer getIsInRecommendPool() {
        return isInRecommendPool;
    }

    public static DimIpPostRecommendBuilder builder() {
        return new DimIpPostRecommendBuilder();
    }

    public static class DimIpPostRecommendBuilder {
        private Long postId;
        private Integer recommendStatus;
        private Integer isInRecommendPool;

        public DimIpPostRecommendBuilder setPostId(Long postId) {
            this.postId = postId;
            return this;
        }

        public DimIpPostRecommendBuilder setRecommendStatus(Integer recommendStatus) {
            this.recommendStatus = recommendStatus;
            return this;
        }

        public DimIpPostRecommendBuilder setIsInRecommendPool(Integer isInRecommendPool) {
            this.isInRecommendPool = isInRecommendPool;
            return this;
        }

        public DimIpPostRecommend build() {
            return new DimIpPostRecommend(postId, recommendStatus, isInRecommendPool);
        }
    }

    @Override
    public String toString() {
        return "{"
                + "\"postId\":"
                + postId
                + ",\"recommendStatus\":"
                + recommendStatus
                + ",\"isInRecommendPool\":"
                + isInRecommendPool
                + "}";
    }
}
