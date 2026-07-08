package com.netease.operator.dwd.binlog.ndc;

import com.netease.dts.common.subscribe.SubscribeEvent;
import com.netease.pojo.BinlogRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class DwdBinlogNdcTransformRichFlatMapFunction
        extends RichFlatMapFunction<SubscribeEvent, BinlogRow> {
    private static final Logger LOG =
            LoggerFactory.getLogger(DwdBinlogNdcTransformRichFlatMapFunction.class);

    @Override
    public void flatMap(SubscribeEvent subscribeEvent, Collector<BinlogRow> collector)
            throws Exception {
        Long seqNo = subscribeEvent.getSeqno();
        Long opTime = subscribeEvent.getTimestamp();
        Integer partitionId = subscribeEvent.getPartitonId();

        for (SubscribeEvent.OneRowChange oneRowChange : subscribeEvent.getRowChanges()) {
            boolean isInsertOp = SubscribeEvent.RowChangeType.INSERT == oneRowChange.getType();
            boolean isDeleteOp = SubscribeEvent.RowChangeType.DELETE == oneRowChange.getType();
            boolean isUpdateOp = SubscribeEvent.RowChangeType.UPDATE == oneRowChange.getType();

            BinlogRow binlogRow = new BinlogRow();
            binlogRow.setTableName(oneRowChange.getTableName());
            binlogRow.setOp(isInsertOp ? 0 : isDeleteOp ? 1 : isUpdateOp ? 2 : -1);
            binlogRow.setOpTime(opTime);
            binlogRow.setSeqNo(seqNo);
            binlogRow.setPartitionId(partitionId);

            Map<String, Object> after = new HashMap<>();
            Map<String, Object> before = new HashMap<>();

            oneRowChange
                    .getColumnChanges()
                    .forEach(
                            oneColumnChange -> {
                                Object newValue = oneColumnChange.getNewValue();
                                Object oldValue = oneColumnChange.getOldValue();
                                if (isInsertOp) {
                                    after.put(
                                            oneColumnChange.getColumnName().toLowerCase(),
                                            processingTimeValue(newValue));
                                }

                                if (isDeleteOp) {
                                    before.put(
                                            oneColumnChange.getColumnName().toLowerCase(),
                                            processingTimeValue(oldValue));
                                }

                                if (isUpdateOp) {
                                    after.put(
                                            oneColumnChange.getColumnName().toLowerCase(),
                                            processingTimeValue(newValue));
                                    before.put(
                                            oneColumnChange.getColumnName().toLowerCase(),
                                            processingTimeValue(oldValue));
                                }
                            });
            binlogRow.setAfter(after);
            binlogRow.setBefore(before);

            collector.collect(binlogRow);
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
