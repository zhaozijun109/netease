package com.netease.operator.ads.pve;

import com.netease.pojo.pve.PveRoleDialogueHourStatistics;
import org.roaringbitmap.longlong.Roaring64Bitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.flink.api.common.functions.ReduceFunction;

public class AdsPveUserChatsStatisticsReduceFunction
        implements ReduceFunction<PveRoleDialogueHourStatistics> {
    private static final Logger LOG =
            LoggerFactory.getLogger(AdsPveUserChatsStatisticsReduceFunction.class);

    @Override
    public PveRoleDialogueHourStatistics reduce(
            PveRoleDialogueHourStatistics p1, PveRoleDialogueHourStatistics p2) throws Exception {
        Long sumPv = p1.getDialoguePv() + p2.getDialoguePv();
        Roaring64Bitmap unionBitmap =
                Roaring64Bitmap.or(p1.getDialogueUserIdBitmap(), p2.getDialogueUserIdBitmap());
        p1.setDialoguePv(sumPv);
        p1.setDialogueUserIdBitmap(unionBitmap);
        return p1;
    }
}
