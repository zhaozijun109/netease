package com.netease.yuanqi.lofter.operator.ads.ecology.post;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.common.pojo.ods.binlog.BinlogRow;
import com.netease.yuanqi.lofter.pojo.ads.ecology.post.PostHot;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdsActTagJoinPostHotRichFlatMapFunction extends RichFlatMapFunction<String, PostHot> {
    private static final Logger LOG =
            LoggerFactory.getLogger(AdsActTagJoinPostHotRichFlatMapFunction.class);
    private ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public void flatMap(String s, Collector<PostHot> collector) throws Exception {
        BinlogRow binlogRow = objectMapper.readValue(s, BinlogRow.class);
        if ("PostHot".equals(binlogRow.get_tbl())
                && (binlogRow.getOp() == 0 || binlogRow.getOp() == 1)) {
            PostHot postHot = new PostHot();
            if (binlogRow.getOp() == 0) {
                postHot =
                        objectMapper.readValue(
                                objectMapper.writeValueAsString(binlogRow.getData()),
                                PostHot.class);
                postHot.setStatus(1);
            }
            if (binlogRow.getOp() == 1) {
                postHot =
                        objectMapper.readValue(
                                objectMapper.writeValueAsString(binlogRow.getOld()), PostHot.class);
                postHot.setStatus(-1);
            }
            postHot.setTableName(binlogRow.get_tbl());
            collector.collect(postHot);
        }
    }
}
