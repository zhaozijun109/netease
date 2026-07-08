package com.netease.yuanqi.lofter.operator.dim.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import com.netease.yuanqi.lofter.pojo.DimBenefitProduct;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;

public class DimBenefitProductRichFlatMapFunction
        extends RichFlatMapFunction<String, DimBenefitProduct> {
    private ObjectMapper objectMapper;

    @Override
    public void open(OpenContext openContext) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void flatMap(String s, Collector<DimBenefitProduct> collector) throws Exception {
        BinlogRow binlogRow = objectMapper.readValue(s, BinlogRow.class);
        if ("benefit_category_product_relation".equals(binlogRow.getTable())
                && (binlogRow.getOp() == 0 || binlogRow.getOp() == 2)) {
            DimBenefitProduct dimBenefitProduct =
                    DimBenefitProduct.builder()
                            .setProductId(
                                    binlogRow.getData().get("productId") != null
                                            ? Long.parseLong(
                                                    binlogRow.getData().get("productId").toString())
                                            : 0)
                            .setCategory3(
                                    binlogRow.getData().get("categoryId") != null
                                            ? Long.parseLong(
                                                    binlogRow
                                                            .getData()
                                                            .get("categoryId")
                                                            .toString())
                                            : 0)
                            .build();
            collector.collect(dimBenefitProduct);
        }
    }
}
