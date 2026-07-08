package com.netease.yuanqi.lofter.operator.dim.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.data.binary.BinaryRowData;
import org.apache.flink.table.data.writer.BinaryRowWriter;
import org.apache.flink.util.Collector;

public class DimRedisDcRichFlatMapFunction extends RichFlatMapFunction<String, BinaryRowData> {
    private ObjectMapper objectMapper;

    @Override
    public void open(OpenContext openContext) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void flatMap(String s, Collector<BinaryRowData> collector) throws Exception {
        BinlogRow binlogRow = objectMapper.readValue(s, BinlogRow.class);
        if ("benefit_cardactivity".equals(binlogRow.getTable()) && binlogRow.getOp() == 0) {
            BinaryRowData binaryRowData = new BinaryRowData(2);
            BinaryRowWriter binaryRowWriter = new BinaryRowWriter(binaryRowData);
            binaryRowWriter.writeString(
                    0, StringData.fromString("dc_" + binlogRow.getData().get("code").toString()));
            binaryRowWriter.writeLong(1, Long.parseLong(binlogRow.getData().get("id").toString()));
            collector.collect(binaryRowData);
        }
    }
}
