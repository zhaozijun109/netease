package com.netease.yuanqi.unified.operator.dim;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;

public class DimRecBuriedPointRichFlatMapFunction extends RichFlatMapFunction<String, BinlogRow> {
    private ObjectMapper objectMapper;

    @Override
    public void open(OpenContext openContext) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void flatMap(String s, Collector<BinlogRow> collector) throws Exception {
        BinlogRow binlogRow = objectMapper.readValue(s, BinlogRow.class);
        if ("Yq_Rec_Buried_Point".equals(binlogRow.getTable())
                && (binlogRow.getOp() == 0 || binlogRow.getOp() == 2)) {
            collector.collect(binlogRow);
        }
    }
}
