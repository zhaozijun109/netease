package com.netease.operator.ads.rec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.pojo.rec.RecRequestAndRecActionLabel;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;

public class AdsRARToHiveRichMapFunction
        extends RichMapFunction<String, RecRequestAndRecActionLabel> {
    private ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public RecRequestAndRecActionLabel map(String s) throws Exception {
        return objectMapper.readValue(s, RecRequestAndRecActionLabel.class);
    }
}
