package com.netease.yuanqi.unified.operator.ods.binlog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.dts.common.subscribe.SubscribeEvent;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

public class NdcBinlogRichFlatMapFunction extends RichFlatMapFunction<SubscribeEvent, String> {
    private ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void flatMap(SubscribeEvent subscribeEvent, Collector<String> collector)
            throws Exception {
        Long seqno = subscribeEvent.getSeqno();
        Long opTime = subscribeEvent.getTimestamp();
        Integer partitionId = subscribeEvent.getPartitonId();

        for (SubscribeEvent.OneRowChange oneRowChange : subscribeEvent.getRowChanges()) {
            boolean isInsertOp = SubscribeEvent.RowChangeType.INSERT == oneRowChange.getType();
            boolean isDeleteOp = SubscribeEvent.RowChangeType.DELETE == oneRowChange.getType();
            boolean isUpdateOp = SubscribeEvent.RowChangeType.UPDATE == oneRowChange.getType();

            Map<String, Object> data = new HashMap<>();
            Map<String, Object> old = new HashMap<>();
            oneRowChange
                    .getColumnChanges()
                    .forEach(
                            columnChange -> {
                                data.put(
                                        columnChange.getColumnName(),
                                        isDeleteOp
                                                ? processingTimeValue(columnChange.getOldValue())
                                                : processingTimeValue(columnChange.getNewValue()));

                                if (isUpdateOp
                                        && columnChange.getNewValue()
                                                != columnChange.getOldValue()) {
                                    old.put(
                                            columnChange.getColumnName(),
                                            processingTimeValue(columnChange.getOldValue()));
                                }
                            });

            BinlogRow binlogRow =
                    BinlogRow.builder()
                            .setTable(oneRowChange.getTableName())
                            .setOp(isInsertOp ? 0 : isDeleteOp ? 1 : isUpdateOp ? 2 : -1)
                            .setOpTime(opTime)
                            .setSeqno(seqno)
                            .setPartitionId(partitionId)
                            .setData(data)
                            .setOld(old)
                            .build();
            collector.collect(objectMapper.writeValueAsString(binlogRow));
        }
    }

    private static Object processingTimeValue(Object value) {
        Object currentValue = null;
        if (value != null) {
            if (value instanceof Timestamp) {
                currentValue = ((Timestamp) value).getTime();
            } else if (value instanceof Time) {
                currentValue = ((Time) value).getTime();
            } else if (value instanceof Date) {
                currentValue = ((Date) value).getTime();
            } else {
                currentValue = value;
            }
        }
        return currentValue;
    }
}
