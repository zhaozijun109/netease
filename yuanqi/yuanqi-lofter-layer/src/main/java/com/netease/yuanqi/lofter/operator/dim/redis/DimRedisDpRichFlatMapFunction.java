package com.netease.yuanqi.lofter.operator.dim.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.data.binary.BinaryRowData;
import org.apache.flink.table.data.writer.BinaryRowWriter;
import org.apache.flink.util.Collector;

public class DimRedisDpRichFlatMapFunction extends RichFlatMapFunction<String, BinaryRowData> {
    private ObjectMapper objectMapper;

    @Override
    public void open(OpenContext openContext) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void flatMap(String s, Collector<BinaryRowData> collector) throws Exception {
        BinlogRow binlogRow = objectMapper.readValue(s, BinlogRow.class);
        if ("Post".equals(binlogRow.getTable()) && binlogRow.getOp() == 0) {
            BinaryRowData binaryRowData = new BinaryRowData(2);
            BinaryRowWriter binaryRowWriter = new BinaryRowWriter(binaryRowData);
            binaryRowWriter.writeString(
                    0,
                    StringData.fromString(
                            "dp_"
                                    + Long.toHexString(
                                                    Long.parseLong(
                                                            binlogRow
                                                                    .getData()
                                                                    .get("ID")
                                                                    .toString()))
                                            .toLowerCase()));
            binaryRowWriter.writeString(
                    1,
                    StringData.fromString(
                            (Long.toHexString(
                                                    Long.parseLong(
                                                            binlogRow
                                                                    .getData()
                                                                    .get("BlogID")
                                                                    .toString()))
                                            + "_"
                                            + Long.toHexString(
                                                    Long.parseLong(
                                                            binlogRow
                                                                    .getData()
                                                                    .get("ID")
                                                                    .toString())))
                                    .toLowerCase()));
            collector.collect(binaryRowData);
        }
    }
}
