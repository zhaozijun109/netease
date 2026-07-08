package com.netease.yuanqi.lofter.operator.dim.es;

import com.netease.yuanqi.lofter.pojo.DimGiftPost;
import com.netease.yuanqi.lofter.pojo.DimGiftPostPgc;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;

public class DimGiftPostBroadcastProcessFunction
        extends BroadcastProcessFunction<DimGiftPost, DimGiftPostPgc, DimGiftPost> {
    private static final MapStateDescriptor<Long, Long> DIM_GIFT_POST_PGC_BROADCAST_DESC =
            new MapStateDescriptor<>(
                    "DimGiftPostPgc", BasicTypeInfo.LONG_TYPE_INFO, BasicTypeInfo.LONG_TYPE_INFO);

    @Override
    public void open(OpenContext openContext) throws Exception {
        super.open(openContext);
    }

    @Override
    public void processElement(
            DimGiftPost dimGiftPost,
            BroadcastProcessFunction<DimGiftPost, DimGiftPostPgc, DimGiftPost>.ReadOnlyContext
                    readOnlyContext,
            Collector<DimGiftPost> collector)
            throws Exception {
        ReadOnlyBroadcastState<Long, Long> dimGiftPgcReadOnlyBroadcastState =
                readOnlyContext.getBroadcastState(DIM_GIFT_POST_PGC_BROADCAST_DESC);
        collector.collect(
                DimGiftPost.builder()
                        .setPostId(dimGiftPost.getPostId())
                        .setBlogId(dimGiftPost.getBlogId())
                        .setGiftTime(dimGiftPost.getGiftTime())
                        .setIsPgc(
                                dimGiftPgcReadOnlyBroadcastState.get(dimGiftPost.getBlogId())
                                                != null
                                        ? 1
                                        : 0)
                        .build());
    }

    @Override
    public void processBroadcastElement(
            DimGiftPostPgc dimGiftPostPgc,
            BroadcastProcessFunction<DimGiftPost, DimGiftPostPgc, DimGiftPost>.Context context,
            Collector<DimGiftPost> collector)
            throws Exception {
        BroadcastState<Long, Long> dimPostPgcBroadcastState =
                context.getBroadcastState(DIM_GIFT_POST_PGC_BROADCAST_DESC);
        dimPostPgcBroadcastState.put(dimGiftPostPgc.getBlogId(), dimGiftPostPgc.getUserId());
    }
}
