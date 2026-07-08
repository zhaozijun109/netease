package com.netease.yuanqi.lofter.pojo;

public class DimVcUser {
    private final Long userId;
    private final Long createTime;
    private final Integer isAnonymous;

    public DimVcUser(Long userId, Long createTime, Integer isAnonymous) {
        this.userId = userId;
        this.createTime = createTime;
        this.isAnonymous = isAnonymous;
    }

    public static DimVcUserBuilder builder() {
        return new DimVcUserBuilder();
    }

    public Integer getIsAnonymous() {
        return isAnonymous;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public Long getUserId() {
        return userId;
    }

    public static class DimVcUserBuilder {
        private Long userId;
        private Long createTime;
        private Integer isAnonymous;

        public DimVcUserBuilder setUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public DimVcUserBuilder setCreateTime(Long createTime) {
            this.createTime = createTime;
            return this;
        }

        public DimVcUserBuilder setIsAnonymous(Integer isAnonymous) {
            this.isAnonymous = isAnonymous;
            return this;
        }

        public DimVcUser build() {
            return new DimVcUser(userId, createTime, isAnonymous);
        }
    }

    @Override
    public String toString() {
        return "{"
                + "userId:"
                + userId
                + ", createTime:"
                + createTime
                + ", isAnonymous:"
                + isAnonymous
                + '}';
    }
}
