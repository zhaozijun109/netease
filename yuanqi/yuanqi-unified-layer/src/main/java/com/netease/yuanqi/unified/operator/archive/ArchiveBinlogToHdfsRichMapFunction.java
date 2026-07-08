package com.netease.yuanqi.unified.operator.archive;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.pojo.archive.ArchiveFormatRow;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import java.util.Map;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;

public class ArchiveBinlogToHdfsRichMapFunction extends RichMapFunction<String, ArchiveFormatRow> {
    private ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public ArchiveFormatRow map(String s) throws Exception {
        BinlogRow binlogRow = objectMapper.readValue(s, BinlogRow.class);

        Map<String, Object> archiveData = binlogRow.getData();
        archiveData.put("_tbl", binlogRow.get_tbl());
        archiveData.put("_bin_op", binlogRow.get_bin_op());
        archiveData.put("_bin_op_time", binlogRow.get_bin_op_time());
        archiveData.put("_bin_op_seqno", binlogRow.get_bin_op_seqno());
        archiveData.put("_bin_old", binlogRow.get_bin_old());

        return ArchiveFormatRow.builder()
                .setArchiveDir(binlogRow.getTable())
                .setArchiveTime(binlogRow.getOpTime())
                .setData(objectMapper.writeValueAsString(archiveData))
                .build();
    }
}
