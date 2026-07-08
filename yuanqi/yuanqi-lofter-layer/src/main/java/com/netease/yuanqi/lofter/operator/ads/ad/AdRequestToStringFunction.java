package com.netease.yuanqi.lofter.operator.ads.ad;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.lofter.pojo.ads.ad.AdRequestRecord;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;

public class AdRequestToStringFunction extends RichFlatMapFunction<AdRequestRecord, String> {
    private ObjectMapper objectMapper;

    @Override
    public void open(OpenContext openContext) throws Exception {
        super.open(openContext);
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public void flatMap(AdRequestRecord adRequestRecord, Collector<String> collector)
            throws Exception {
        collector.collect(objectMapper.writeValueAsString(adRequestRecord));
    }
}
