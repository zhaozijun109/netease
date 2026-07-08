package com.netease.yuanqi.lofter.operator.dim.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import com.netease.yuanqi.lofter.pojo.DimBenefitCategory;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;

public class DimBenefitProductCategoryRichFlatMapFunction
        extends RichFlatMapFunction<String, DimBenefitCategory> {
    private ObjectMapper objectMapper;

    @Override
    public void open(OpenContext openContext) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void flatMap(String s, Collector<DimBenefitCategory> collector) throws Exception {
        BinlogRow binlogRow = objectMapper.readValue(s, BinlogRow.class);
        if ("benefit_category".equals(binlogRow.getTable())
                && (binlogRow.getOp() == 0 || binlogRow.getOp() == 2)) {
            Long categoryId =
                    binlogRow.getData().get("id") != null
                            ? Long.parseLong(binlogRow.getData().get("id").toString())
                            : null;
            String name = binlogRow.getData().get("name").toString();
            Integer level =
                    binlogRow.getData().get("level") != null
                            ? Integer.parseInt(binlogRow.getData().get("level").toString())
                            : null;
            Long parentId =
                    binlogRow.getData().get("parentId") != null
                            ? Long.parseLong(binlogRow.getData().get("parentId").toString())
                            : null;
            if (level != null) {
                if (level == 1 || level == -1) {
                    collector.collect(
                            DimBenefitCategory.builder()
                                    .setCategoryId(categoryId)
                                    .setName(name)
                                    .setLevel(level)
                                    .setParentId(parentId)
                                    .setCategory1(categoryId)
                                    .setCategory1Name(name)
                                    .build());
                }

                if (level == 2) {
                    collector.collect(
                            DimBenefitCategory.builder()
                                    .setCategoryId(categoryId)
                                    .setName(name)
                                    .setLevel(level)
                                    .setParentId(parentId)
                                    .setCategory2(categoryId)
                                    .setCategory2Name(name)
                                    .build());
                }

                if (level == 3) {
                    collector.collect(
                            DimBenefitCategory.builder()
                                    .setCategoryId(categoryId)
                                    .setName(name)
                                    .setLevel(level)
                                    .setParentId(parentId)
                                    .setCategory3(categoryId)
                                    .setCategory3Name(name)
                                    .build());
                }
            }
        }
    }
}
