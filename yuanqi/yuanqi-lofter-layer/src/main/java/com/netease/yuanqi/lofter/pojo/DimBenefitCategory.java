package com.netease.yuanqi.lofter.pojo;

public class DimBenefitCategory {
    private final Long categoryId;
    private final Integer level;
    private final String name;
    private final Long parentId;
    private final Long category1;
    private final String category1Name;
    private final Long category2;
    private final String category2Name;
    private final Long category3;
    private final String category3Name;

    public DimBenefitCategory(
            Long categoryId,
            Integer level,
            String name,
            Long parentId,
            Long category1,
            String category1Name,
            Long category2,
            String category2Name,
            Long category3,
            String category3Name) {
        this.categoryId = categoryId;
        this.level = level;
        this.name = name;
        this.parentId = parentId;
        this.category1 = category1;
        this.category1Name = category1Name;
        this.category2 = category2;
        this.category2Name = category2Name;
        this.category3 = category3;
        this.category3Name = category3Name;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Integer getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public Long getParentId() {
        return parentId;
    }

    public Long getCategory1() {
        return category1;
    }

    public String getCategory1Name() {
        return category1Name;
    }

    public Long getCategory2() {
        return category2;
    }

    public String getCategory2Name() {
        return category2Name;
    }

    public Long getCategory3() {
        return category3;
    }

    public String getCategory3Name() {
        return category3Name;
    }

    public static DimBenefitCategoryBuilder builder() {
        return new DimBenefitCategoryBuilder();
    }

    public static class DimBenefitCategoryBuilder {
        private Long categoryId;
        private Integer level;
        private String name;
        private Long parentId;
        private Long category1;
        private String category1Name;
        private Long category2;
        private String category2Name;
        private Long category3;
        private String category3Name;

        public DimBenefitCategoryBuilder setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public DimBenefitCategoryBuilder setLevel(Integer level) {
            this.level = level;
            return this;
        }

        public DimBenefitCategoryBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public DimBenefitCategoryBuilder setParentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public DimBenefitCategoryBuilder setCategory1(Long category1) {
            this.category1 = category1;
            return this;
        }

        public DimBenefitCategoryBuilder setCategory1Name(String category1Name) {
            this.category1Name = category1Name;
            return this;
        }

        public DimBenefitCategoryBuilder setCategory2(Long category2) {
            this.category2 = category2;
            return this;
        }

        public DimBenefitCategoryBuilder setCategory2Name(String category2Name) {
            this.category2Name = category2Name;
            return this;
        }

        public DimBenefitCategoryBuilder setCategory3(Long category3) {
            this.category3 = category3;
            return this;
        }

        public DimBenefitCategoryBuilder setCategory3Name(String category3Name) {
            this.category3Name = category3Name;
            return this;
        }

        public DimBenefitCategory build() {
            return new DimBenefitCategory(
                    categoryId,
                    level,
                    name,
                    parentId,
                    category1,
                    category1Name,
                    category2,
                    category2Name,
                    category3,
                    category3Name);
        }
    }

    @Override
    public String toString() {
        return "{"
                + "\"categoryId\":"
                + categoryId
                + ",\"level\":"
                + level
                + ",\"name\":\""
                + name
                + '\"'
                + ",\"parentId\":"
                + parentId
                + ",\"category1\":"
                + category1
                + ",\"category1Name\":\""
                + category1Name
                + '\"'
                + ",\"category2\":"
                + category2
                + ",\"category2Name\":\""
                + category2Name
                + '\"'
                + ",\"category3\":"
                + category3
                + ",\"category3Name\":\""
                + category3Name
                + '\"'
                + "}";
    }
}
