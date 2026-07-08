package com.netease.yuanqi.lofter.pojo;

public class DimBenefitProduct {
    private final Long productId;
    private final Long category1;
    private final String category1Name;
    private final Long category2;
    private final String category2Name;
    private final Long category3;
    private final String category3Name;

    public DimBenefitProduct(
            Long productId,
            Long category1,
            String category1Name,
            Long category2,
            String category2Name,
            Long category3,
            String category3Name) {
        this.productId = productId;
        this.category1 = category1;
        this.category1Name = category1Name;
        this.category2 = category2;
        this.category2Name = category2Name;
        this.category3 = category3;
        this.category3Name = category3Name;
    }

    public Long getProductId() {
        return productId;
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

    public static DimBenefitProductBuilder builder() {
        return new DimBenefitProductBuilder();
    }

    public static class DimBenefitProductBuilder {
        private Long productId;
        private Long category1;
        private String category1Name;
        private Long category2;
        private String category2Name;
        private Long category3;
        private String category3Name;

        public DimBenefitProductBuilder setProductId(Long productId) {
            this.productId = productId;
            return this;
        }

        public DimBenefitProductBuilder setCategory1(Long category1) {
            this.category1 = category1;
            return this;
        }

        public DimBenefitProductBuilder setCategory1Name(String category1Name) {
            this.category1Name = category1Name;
            return this;
        }

        public DimBenefitProductBuilder setCategory2(Long category2) {
            this.category2 = category2;
            return this;
        }

        public DimBenefitProductBuilder setCategory2Name(String category2Name) {
            this.category2Name = category2Name;
            return this;
        }

        public DimBenefitProductBuilder setCategory3(Long category3) {
            this.category3 = category3;
            return this;
        }

        public DimBenefitProductBuilder setCategory3Name(String category3Name) {
            this.category3Name = category3Name;
            return this;
        }

        public DimBenefitProduct build() {
            return new DimBenefitProduct(
                    productId,
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
                + "\"productId\":"
                + productId
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
