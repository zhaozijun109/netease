package com.netease.yuanqi.lofter.operator.ads.pve;

import com.netease.yuanqi.lofter.pojo.ads.pve.PveRoleDialogueHourStatistics;
import com.netease.yuanqi.lofter.pojo.ads.pve.PveUserRoleDialogueLogs;
import java.time.Duration;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.roaringbitmap.longlong.Roaring64Bitmap;

public class AdsPveUserRoleChatsStatisticsProcessWindowFunction
        extends ProcessWindowFunction<
                PveUserRoleDialogueLogs, PveRoleDialogueHourStatistics, String, TimeWindow> {
    private ValueState<Long> pvState;
    private ValueState<Roaring64Bitmap> uvState;

    @Override
    public void open(Configuration parameters) throws Exception {
        StateTtlConfig userDialogueStateTtlConfig =
                StateTtlConfig.newBuilder(Duration.ofMinutes(65))
                        .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                        .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                        .cleanupInRocksdbCompactFilter(100000)
                        .build();

        ValueStateDescriptor<Long> pvStateDescriptor =
                new ValueStateDescriptor<>("AdsPveUserRoleChatsStatisticsPvState", Long.class);
        pvState = getRuntimeContext().getState(pvStateDescriptor);

        ValueStateDescriptor<Roaring64Bitmap> uvStateDescriptor =
                new ValueStateDescriptor<>(
                        "AdsPveUserRoleChatsStatisticsUvState",
                        TypeInformation.of(Roaring64Bitmap.class));
        uvStateDescriptor.enableTimeToLive(userDialogueStateTtlConfig);
        uvState = getRuntimeContext().getState(uvStateDescriptor);
    }

    @Override
    public void process(
            String key,
            ProcessWindowFunction<
                                    PveUserRoleDialogueLogs,
                                    PveRoleDialogueHourStatistics,
                                    String,
                                    TimeWindow>
                            .Context
                    context,
            Iterable<PveUserRoleDialogueLogs> iterable,
            Collector<PveRoleDialogueHourStatistics> collector)
            throws Exception {
        Long pv = pvState.value();
        Roaring64Bitmap uv = uvState.value();

        if (uv == null) {
            pv = 0L;
            uv = new Roaring64Bitmap();
        }

        for (PveUserRoleDialogueLogs pveUserRoleDialogueLogs : iterable) {
            pv++;
            uv.add(pveUserRoleDialogueLogs.getUserId());
        }

        pvState.update(pv);
        uvState.update(uv);

        PveRoleDialogueHourStatistics pveRoleDialogueHourStatistics =
                new PveRoleDialogueHourStatistics();
        String[] keys = key.split("_");
        pveRoleDialogueHourStatistics.setDt(keys[0]);
        pveRoleDialogueHourStatistics.setHour(Integer.valueOf(keys[1]));
        pveRoleDialogueHourStatistics.setRoleId(Long.parseLong(keys[2]));
        pveRoleDialogueHourStatistics.setRoleType(iterable.iterator().next().getRoleType());
        pveRoleDialogueHourStatistics.setDialoguePv(pvState.value());
        pveRoleDialogueHourStatistics.setDialogueUserIdBitmap(uvState.value());
        pveRoleDialogueHourStatistics.setMessageType(0);

        collector.collect(pveRoleDialogueHourStatistics);
    }
}
