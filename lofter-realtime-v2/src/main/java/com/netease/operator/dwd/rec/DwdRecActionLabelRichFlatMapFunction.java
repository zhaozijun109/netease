package com.netease.operator.dwd.rec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lofter.rs.basic.bean.dto.upload.ActionDto;
import rs.basic.upload.parse.dto.ActionLabeledDto;
import rs.basic.upload.parse.handler.ActionLabelHandler;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

public class DwdRecActionLabelRichFlatMapFunction extends RichFlatMapFunction<String, String> {
    private ObjectMapper objectMapper;
    private ActionLabelHandler.ActionLabelConf actionLabelConf;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
        actionLabelConf =
                objectMapper.readValue(
                        "{\"clickRatingSet\":[201,129,101],\"costTimeRatingSet\":[104,102,103],\"exposedRatingSet\":[0,200,100],\"exposedTimeRatingSet\":[125],\"itemTypeSet\":[\"COLLECTION\",\"ANSWER\",\"VIDEO\",\"RECORD\",\"SUPPLYCOLLECTION\",\"TEXT\",\"AUDIO\",\"QUESTION\",\"CARD\",\"COS_QUESTION\",\"ARTICLE\",\"COS_ANSWER\",\"PRODUCT\",\"record\",\"PHOTO\",\"TAG\",\"ACTIVITY\"],\"maxCostTime\":0,\"maxExpTime\":0,\"negActionRatingSet\":[-103,-120,-201,-203,-204,-205,-125,-110],\"negHighRatingSet\":[-124],\"negLightRatingSet\":[-202,-108,-109],\"negLikeRatingSet\":[-202,-108],\"pageClickRatingSet\":[128,129,130,131,132,101,133,126,127],\"posHighRatingSet\":[112,115,117,105,124,109],\"posLightRatingSet\":[202,107,108],\"posLikeRatingSet\":[202,107],\"relatedItemIdRatingSet\":[200,-201,201,-202,202,-203,-204],\"stayRatingSet\":[201,129,101]}",
                        ActionLabelHandler.ActionLabelConf.class);
    }

    @Override
    public void flatMap(String s, Collector<String> collector) throws Exception {
        ActionDto actionDto = objectMapper.readValue(s, ActionDto.class);
        ActionLabeledDto actionLabeledDto =
                ActionLabelHandler.actionLabel(actionDto, actionLabelConf);
        if (actionLabeledDto != null) {
            collector.collect(objectMapper.writeValueAsString(actionLabeledDto));
        }
    }
}
