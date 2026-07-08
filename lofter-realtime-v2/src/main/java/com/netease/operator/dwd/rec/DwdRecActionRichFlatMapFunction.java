package com.netease.operator.dwd.rec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lofter.rs.basic.bean.dto.upload.ActionDto;
import rs.basic.upload.parse.handler.ActionMessageHandler;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

public class DwdRecActionRichFlatMapFunction extends RichFlatMapFunction<String, String> {
    private ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void flatMap(String s, Collector<String> collector) throws Exception {
        ActionDto actionDto = ActionMessageHandler.parseActionDto(s);
        collector.collect(objectMapper.writeValueAsString(actionDto));
    }
}
