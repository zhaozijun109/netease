package com.netease.yuanqi.unified.operator.archive;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.pojo.archive.ArchiveFormatRow;
import com.netease.yuanqi.common.utils.DateTimeFormatterUtils;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchiveBackendLogToHdfsRichMapFunction
        extends RichMapFunction<String, ArchiveFormatRow> {
    private static final Logger LOG =
            LoggerFactory.getLogger(ArchiveBackendLogToHdfsRichMapFunction.class);
    private ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public ArchiveFormatRow map(String s) throws Exception {
        try {
            JsonNode jsonNode = objectMapper.readTree(s);
            String tableName =
                    jsonNode.has("logType") ? jsonNode.get("logType").asText() : "logTypeUndefine";
            Long logTime =
                    jsonNode.has("logTime")
                            ? DateTimeFormatterUtils.dateTimeFormatTransformTime(
                                    jsonNode.get("logTime").asText())
                            : System.currentTimeMillis();
            return ArchiveFormatRow.builder()
                    .setArchiveDir(tableName)
                    .setArchiveTime(logTime)
                    .setData(s)
                    .build();
        } catch (Exception e) {
            // LOG.info("Failed to parse backend log: {}", s, e);
            return ArchiveFormatRow.builder()
                    .setArchiveDir("logTypeUndefine")
                    .setArchiveTime(System.currentTimeMillis())
                    .setData(s)
                    .build();
        }
    }
}
