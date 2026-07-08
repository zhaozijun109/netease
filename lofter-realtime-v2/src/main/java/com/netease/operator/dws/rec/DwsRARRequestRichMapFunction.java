package com.netease.operator.dws.rec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.pojo.rec.RecRequest;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;

public class DwsRARRequestRichMapFunction extends RichMapFunction<String, RecRequest> {

    private ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public RecRequest map(String s) throws Exception {
        return objectMapper.readValue(s, RecRequest.class);
    }
}
