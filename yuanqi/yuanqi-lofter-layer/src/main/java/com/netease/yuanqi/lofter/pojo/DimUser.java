package com.netease.yuanqi.lofter.pojo;

public class DimUser {
    private final Long userId;
    private final Long createTime;
    private final Integer isAnonymous;

    public DimUser(Long userId, Long createTime, Integer isAnonymous) {
        this.userId = userId;
        this.createTime = createTime;
        this.isAnonymous = isAnonymous;
    }

    public static DimUserBuilder builder() {
        return new DimUserBuilder();
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

    public static class DimUserBuilder {
        private Long userId;
        private Long createTime;
        private Integer isAnonymous;

        public DimUserBuilder setUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public DimUserBuilder setCreateTime(Long createTime) {
            this.createTime = createTime;
            return this;
        }

        public DimUserBuilder setIsAnonymous(Integer isAnonymous) {
            this.isAnonymous = isAnonymous;
            return this;
        }

        public DimUser build() {
            return new DimUser(userId, createTime, isAnonymous);
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
