package com.netease.operator.ads.pve;

import com.netease.pojo.pve.PveDialogueHourStatistics;
import com.netease.pojo.pve.PveRoleDialogueHourStatistics;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class AdsPveUserChatsStatisticsProcessWindowFunction
        extends ProcessWindowFunction<
                PveRoleDialogueHourStatistics, PveDialogueHourStatistics, String, TimeWindow> {

    @Override
    public void process(
            String s,
            ProcessWindowFunction<
                                    PveRoleDialogueHourStatistics,
                                    PveDialogueHourStatistics,
                                    String,
                                    TimeWindow>
                            .Context
                    context,
            Iterable<PveRoleDialogueHourStatistics> iterable,
            Collector<PveDialogueHourStatistics> collector)
            throws Exception {
        PveRoleDialogueHourStatistics pveRoleDialogueHourStatistics = iterable.iterator().next();

        PveDialogueHourStatistics pveDialogueHourStatistics = new PveDialogueHourStatistics();
        pveDialogueHourStatistics.setDt(pveRoleDialogueHourStatistics.getDt());
        pveDialogueHourStatistics.setHour(pveRoleDialogueHourStatistics.getHour());
        pveDialogueHourStatistics.setDialoguePv(pveRoleDialogueHourStatistics.getDialoguePv());
        pveDialogueHourStatistics.setDialogueUv(
                pveRoleDialogueHourStatistics.getDialogueUserIdBitmap().getLongCardinality());
        pveDialogueHourStatistics.setMessageType(pveRoleDialogueHourStatistics.getMessageType());
        collector.collect(pveDialogueHourStatistics);
    }
}
