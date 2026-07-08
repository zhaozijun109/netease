package com.netease.yuanqi.vc.operator.dim.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;

public class DimVcSimulatorInfoRichFlatMapFunction extends RichFlatMapFunction<String, BinlogRow> {
    private ObjectMapper objectMapper;

    @Override
    public void open(OpenContext openContext) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void flatMap(String s, Collector<BinlogRow> collector) throws Exception {
        BinlogRow binlogRow = objectMapper.readValue(s, BinlogRow.class);
        if ("vc_simulator_info".equalsIgnoreCase(binlogRow.getTable())
                && (binlogRow.getOp() == 0 || binlogRow.getOp() == 2)) {
            collector.collect(binlogRow);
        }
    }
}
