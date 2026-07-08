package com.netease.yuanqi.lofter.operator.dim.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import com.netease.yuanqi.lofter.pojo.DimGiftPost;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;

public class DimGiftPostRichFlatMapFunction extends RichFlatMapFunction<String, DimGiftPost> {
    private ObjectMapper objectMapper;

    @Override
    public void open(OpenContext openContext) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void flatMap(String s, Collector<DimGiftPost> collector) throws Exception {
        BinlogRow binlogRow = objectMapper.readValue(s, BinlogRow.class);
        if ("Trade_ReturnGiftPlan".equals(binlogRow.getTable())
                && (binlogRow.getOp() == 0 || binlogRow.getOp() == 2)) {
            if (binlogRow.getData().get("status") != null
                    && Integer.parseInt(binlogRow.getData().get("status").toString()) == 1
                    && binlogRow.getData().get("auditStatus") != null
                    && Integer.parseInt(binlogRow.getData().get("auditStatus").toString()) == 1) {
                Long postId =
                        binlogRow.getData().get("postId") != null
                                ? Long.parseLong(binlogRow.getData().get("postId").toString())
                                : null;
                Long blogId =
                        binlogRow.getData().get("blogId") != null
                                ? Long.parseLong(binlogRow.getData().get("blogId").toString())
                                : null;
                Long giftTime =
                        Math.min(
                                binlogRow.getOpTime(),
                                binlogRow.getData().get("createTime") != null
                                        ? Long.parseLong(
                                                binlogRow.getData().get("createTime").toString())
                                        : Long.MAX_VALUE);
                if (postId != null && blogId != null) {
                    collector.collect(
                            DimGiftPost.builder()
                                    .setPostId(postId)
                                    .setBlogId(blogId)
                                    .setGiftTime(giftTime)
                                    .build());
                }
            }
        }
    }
}
