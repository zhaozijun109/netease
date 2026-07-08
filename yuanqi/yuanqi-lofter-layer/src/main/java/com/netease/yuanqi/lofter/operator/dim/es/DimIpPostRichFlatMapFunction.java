package com.netease.yuanqi.lofter.operator.dim.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import com.netease.yuanqi.lofter.pojo.DimIpPost;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;

public class DimIpPostRichFlatMapFunction extends RichFlatMapFunction<String, DimIpPost> {
    private ObjectMapper objectMapper;

    @Override
    public void open(OpenContext openContext) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void flatMap(String s, Collector<DimIpPost> collector) throws Exception {
        BinlogRow binlogRow = objectMapper.readValue(s, BinlogRow.class);
        if ("Post".equals(binlogRow.getTable())
                && (binlogRow.getOp() == 0 || binlogRow.getOp() == 2)) {
            int isPublished =
                    binlogRow.getData().get("IsPublished") != null
                            ? Integer.parseInt(binlogRow.getData().get("IsPublished").toString())
                            : 0;
            int oldIsPublished =
                    binlogRow.getOld().get("IsPublished") != null
                            ? Integer.parseInt(binlogRow.getOld().get("IsPublished").toString())
                            : 0;
            if (isPublished == 1 && oldIsPublished != 1) {
                Long postId =
                        binlogRow.getData().get("ID") != null
                                ? Long.parseLong(binlogRow.getData().get("ID").toString())
                                : null;
                Long blogId =
                        binlogRow.getData().get("BlogID") != null
                                ? Long.parseLong(binlogRow.getData().get("BlogID").toString())
                                : null;
                Long publishTime =
                        binlogRow.getData().get("PublishTime") != null
                                ? Math.min(
                                        binlogRow.getOpTime(),
                                        Long.parseLong(
                                                binlogRow.getData().get("PublishTime").toString()))
                                : null;
                String tag =
                        binlogRow.getData().get("Tag") != null
                                ? binlogRow
                                        .getData()
                                        .get("Tag")
                                        .toString()
                                        .trim()
                                        .toLowerCase()
                                        .split(",")[0]
                                : "";
                DimIpPost dimIpPost =
                        DimIpPost.builder()
                                .setPostId(postId)
                                .setBlogId(blogId)
                                .setPublishTime(publishTime)
                                .setTag(tag)
                                .setBlogLevel("")
                                .setIp("")
                                .setIsInRecommendPool(0)
                                .build();
                collector.collect(dimIpPost);
            }
        }
    }
}
