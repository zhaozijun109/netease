package com.netease.yuanqi.lofter.operator.dim.es;

import com.netease.yuanqi.common.pojo.avro.ods.ClientMdaLogAvro;
import com.netease.yuanqi.lofter.pojo.DimDevice;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DimDeviceRichFlatMapFunction extends RichFlatMapFunction<ClientMdaLogAvro, DimDevice> {
    private static final Logger LOG = LoggerFactory.getLogger(DimDeviceRichFlatMapFunction.class);
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    // state stores latest day epoch (yyyy-mm-dd as epochDay) for dedup
    private ValueState<Long> latestDayState;

    @Override
    public void open(Configuration parameters) throws Exception {
        StateTtlConfig stateTtlConfig =
                StateTtlConfig.newBuilder(Duration.ofHours(36))
                        .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                        .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                        .cleanupInRocksdbCompactFilter(100000)
                        .build();
        ValueStateDescriptor<Long> latestDayStateDescriptor =
                new ValueStateDescriptor<>(
                        "DimDeviceLatestDayState", TypeInformation.of(Long.class));
        latestDayStateDescriptor.enableTimeToLive(stateTtlConfig);
        latestDayState = getRuntimeContext().getState(latestDayStateDescriptor);
    }

    @Override
    public void flatMap(ClientMdaLogAvro event, Collector<DimDevice> collector) throws Exception {
        if (event.getDeviceUdid() == null || event.getOccurTime() == null) {
            return;
        }
        String deviceUdid = event.getDeviceUdid().toString();
        if (deviceUdid.isEmpty()) {
            return;
        }
        long occurTime = event.getOccurTime();
        long currentDay =
                Instant.ofEpochMilli(occurTime).atZone(ZONE_ID).toLocalDate().toEpochDay();

        Long stateDay = latestDayState.value();
        if (stateDay != null && stateDay >= currentDay) {
            // already emitted for this day (or a later day due to out-of-order)
            return;
        }

        latestDayState.update(currentDay);
        collector.collect(
                DimDevice.builder()
                        .setDeviceUdid(deviceUdid)
                        .setLatestDayFirstTime(occurTime)
                        .build());
    }
}
