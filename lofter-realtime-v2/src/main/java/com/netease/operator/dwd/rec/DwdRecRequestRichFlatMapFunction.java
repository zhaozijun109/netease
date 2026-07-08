package com.netease.operator.dwd.rec;

import com.fasterxml.jackson.databind.ObjectMapper;
import rs.basic.upload.parse.dto.SimpleRecItemDto;
import rs.basic.upload.parse.handler.SimpleRecDataHandler;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

import java.util.List;

public class DwdRecRequestRichFlatMapFunction extends RichFlatMapFunction<String, String> {
    private ObjectMapper objectMapper;
    private SimpleRecDataHandler.Conf simpleRecDataHandlerconf;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
        simpleRecDataHandlerconf =
                objectMapper.readValue(
                        "{\"handlerSceneSet\":[\"feed_rec\"],\"needReqExtParam\":[\"multiAbTestLayer\",\"reBackType\"]}",
                        SimpleRecDataHandler.Conf.class);
    }

    @Override
    public void flatMap(String s, Collector<String> collector) throws Exception {
        List<SimpleRecItemDto> recRequestList =
                SimpleRecDataHandler.parseRecData(s, simpleRecDataHandlerconf);
        if (recRequestList != null && !recRequestList.isEmpty()) {
            for (SimpleRecItemDto simpleRecItemDto : recRequestList) {
                collector.collect(objectMapper.writeValueAsString(simpleRecItemDto));
            }
        }
    }
}
