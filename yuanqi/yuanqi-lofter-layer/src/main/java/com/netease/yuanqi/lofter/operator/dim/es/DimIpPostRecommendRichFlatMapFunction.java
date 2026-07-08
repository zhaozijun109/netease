package com.netease.yuanqi.lofter.operator.dim.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import com.netease.yuanqi.lofter.pojo.DimIpPostRecommend;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;

public class DimIpPostRecommendRichFlatMapFunction
        extends RichFlatMapFunction<String, DimIpPostRecommend> {
    private ObjectMapper objectMapper;

    @Override
    public void open(OpenContext openContext) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void flatMap(String s, Collector<DimIpPostRecommend> collector) throws Exception {
        BinlogRow binlogRow = objectMapper.readValue(s, BinlogRow.class);
        if ("RecommendReviewPost".equals(binlogRow.getTable())
                && (binlogRow.getOp() == 0 || binlogRow.getOp() == 2)) {
            Long postId =
                    binlogRow.getData().get("postId") != null
                            ? Long.parseLong(binlogRow.getData().get("postId").toString())
                            : null;
            Integer recommendStatus =
                    binlogRow.getData().get("recomStatus") != null
                            ? Integer.parseInt(binlogRow.getData().get("recomStatus").toString())
                            : null;
            Integer isInRecommendPool =
                    recommendStatus != null && recommendStatus > 0 ? recommendStatus + 1 : 1;
            DimIpPostRecommend dimIpPostRecommend =
                    DimIpPostRecommend.builder()
                            .setPostId(postId)
                            .setRecommendStatus(recommendStatus)
                            .setIsInRecommendPool(isInRecommendPool)
                            .build();
            collector.collect(dimIpPostRecommend);
        }
    }
}
