package com.netease.operator.dws.rec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.pojo.rec.RecRequestAndRecActionLabel;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;

public class DwsRARRichMapFunction extends RichMapFunction<RecRequestAndRecActionLabel, String> {
    private ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public String map(RecRequestAndRecActionLabel recRequestAndActionLabel) throws Exception {
        return objectMapper.writeValueAsString(recRequestAndActionLabel);
    }
}
