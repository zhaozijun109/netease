package com.netease.yuanqi.unified.operator.ods.rec;

import com.lofter.rs.basic.bean.dto.upload.ActionDto;
import com.netease.yuanqi.unified.pojo.RecParsedLogEvents;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.basic.upload.parse.handler.ActionMessageHandler;

public class ActionDtoRichFlatMapFunction
        extends RichFlatMapFunction<RecParsedLogEvents, ActionDto> {
    private static final Logger LOG = LoggerFactory.getLogger(ActionDtoRichFlatMapFunction.class);

    @Override
    public void flatMap(RecParsedLogEvents recParsedLogEvents, Collector<ActionDto> collector)
            throws Exception {
        if (!("a2-7".equals(recParsedLogEvents.getEventId())
                && recParsedLogEvents.getSource() != null
                && recParsedLogEvents.getSource().contains("blogId ="))) {
            try {
                ActionDto actionDto =
                        ActionMessageHandler.parseActionDto(
                                recParsedLogEvents.getSource(),
                                recParsedLogEvents.getAction() != null
                                        ? recParsedLogEvents.getAction()
                                        : -9999);
                if (actionDto != null) {
                    collector.collect(actionDto);
                }
            } catch (Exception e) {
                // LOG.error("Failed to parse mda event to rec event ActionDto: {}",
                // recParsedLogEvents.getSource(), e);
            }
        }
    }
}
