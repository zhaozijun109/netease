package com.netease.operator.dws.rec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.pojo.rec.RecActionLabel;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;

public class DwsRARActionLabelRichMapFunction extends RichMapFunction<String, RecActionLabel> {
    private ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public RecActionLabel map(String s) throws Exception {
        return objectMapper.readValue(s, RecActionLabel.class);
    }
}
