package com.netease.yuanqi.unified.archive.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lofter.rs.basic.bean.dto.upload.ActionDto;
import java.util.Map;
import rs.basic.upload.parse.dto.ActionHiveDto;
import rs.basic.upload.parse.enums.DataType;
import rs.basic.upload.parse.handler.ActionMessageHandler;

public class ActionHiveDtoTest {
    public static void main(String[] args) throws JsonProcessingException {
        String s =
                "{\"appName\":\"lofter\",\"logFile\":true,\"scene\":\"search_fill\",\"account\":\"1264140715\",\"itemId\":\"l月\",\"itemType\":\"WORD\",\"rating\":0,\"text\":\"l月\",\"time\":1777000330273,\"cost\":0,\"progress\":null,\"platform\":1,\"algInfo\":\"{\\\"adjustPlan\\\":0,\\\"algName\\\":\\\"USER_FOLLOW_TAG2QUERY\\\",\\\"flowName\\\":\\\"SuggestLikeSearch\\\",\\\"markMatchUser\\\":0,\\\"newPool\\\":0,\\\"ss\\\":true,\\\"upFlag\\\":0,\\\"w\\\":\\\"9.4112\\\"}\",\"algInfoExtDto\":{\"algName\":\"USER_FOLLOW_TAG2QUERY\",\"taskId\":null,\"tcId\":null,\"colTaskId\":null,\"realStatisticId\":null,\"firstIp\":null},\"recId\":\"a849b7aa80474fa9823cbb63b24fd594\",\"extraData\":\"{\\\"oac\\\":0,\\\"oct\\\":1777000316208,\\\"eventId\\\":\\\"k1-19\\\",\\\"pageScene\\\":\\\"discovery\\\",\\\"handlerTime\\\":1777000330373,\\\"repeat\\\":1,\\\"ip\\\":\\\"39.180.82.135\\\",\\\"deviceModel\\\":\\\"STG-AL00\\\",\\\"page\\\":\\\"search_fill\\\",\\\"customId\\\":\\\"b25d281d3a661b48\\\",\\\"preSource\\\":\\\"{\\\\\\\"ext_itemId\\\\\\\":\\\\\\\"11768260332\\\\\\\",\\\\\\\"ext_itemType\\\\\\\":\\\\\\\"TEXT\\\\\\\",\\\\\\\"itemId\\\\\\\":\\\\\\\"11768261333\\\\\\\",\\\\\\\"itemType\\\\\\\":\\\\\\\"TEXT\\\\\\\",\\\\\\\"module\\\\\\\":\\\\\\\"note_collection\\\\\\\",\\\\\\\"scene\\\\\\\":\\\\\\\"note\\\\\\\"}\\\"}\",\"extData\":{\"relatedItemType\":null,\"repeat\":1,\"eventId\":\"k1-19\",\"ad\":null,\"queryName\":null,\"maskPhone\":null,\"customId\":\"b25d281d3a661b48\",\"ip\":\"39.180.82.135\",\"deviceModel\":\"STG-AL00\",\"tab\":null,\"itemRecType\":null,\"itemRecId\":null,\"pageScene\":\"discovery\",\"extInfoType\":null,\"showType\":null,\"qaId\":null,\"qaType\":null,\"unlockActionType\":null},\"itemExtInfo\":null,\"appVersion\":\"8.3.40\",\"source\":null,\"refer\":null,\"sourceLink\":\"[{\\\"ext\\\":\\\"p_u4tm4x1be0o\\\",\\\"itemType\\\":\\\"TEXT\\\",\\\"module\\\":\\\"note_collection\\\",\\\"ext_itemId\\\":\\\"11768261333\\\",\\\"ext_itemType\\\":\\\"TEXT\\\",\\\"scene\\\":\\\"note\\\",\\\"itemId\\\":\\\"11768255469\\\",\\\"text\\\":\\\"2\\\",\\\"pagename\\\":\\\"PostDetailCommonAdapterFragment\\\"},{\\\"ext\\\":\\\"p_uaom4x0wpbm\\\",\\\"itemType\\\":\\\"TEXT\\\",\\\"module\\\":\\\"note_collection\\\",\\\"ext_itemId\\\":\\\"11768260332\\\",\\\"ext_itemType\\\":\\\"TEXT\\\",\\\"scene\\\":\\\"note\\\",\\\"itemId\\\":\\\"11768261333\\\",\\\"text\\\":\\\"2\\\",\\\"pagename\\\":\\\"PostDetailCommonAdapterFragment\\\"},{\\\"ext\\\":\\\"p_u6vm4x0rdxy\\\",\\\"itemType\\\":\\\"TEXT\\\",\\\"module\\\":\\\"note_collection\\\",\\\"ext_itemId\\\":\\\"11768259289\\\",\\\"ext_itemType\\\":\\\"TEXT\\\",\\\"scene\\\":\\\"note\\\",\\\"itemId\\\":\\\"11768260332\\\",\\\"text\\\":\\\"2\\\",\\\"pagename\\\":\\\"PostDetailCommonAdapterFragment\\\"}]\",\"eventId\":\"k1-19\",\"unlockType\":null,\"originMsg\":null,\"deviceId\":null}";
        ObjectMapper objectMapper = new ObjectMapper();
        ActionDto actionDto = objectMapper.readValue(s, ActionDto.class);
        Map<DataType, ActionHiveDto> actionHiveDtoMap =
                ActionMessageHandler.parseHiveActionDto(actionDto);
        if (actionHiveDtoMap != null && !actionHiveDtoMap.isEmpty()) {
            if (actionHiveDtoMap.get(DataType.Blog) != null) {
                System.out.println(
                        "----1: "
                                + objectMapper.writeValueAsString(
                                        actionHiveDtoMap.get(DataType.Blog)));
            }

            if (actionHiveDtoMap.get(DataType.NoBlog) != null) {
                // ====2:
                // {"lf":"true","ri":"a849b7aa80474fa9823cbb63b24fd594","a":"lofter","ac":"1264140715","an":"","ii":"l月","it":"WORD","alg":"{\"adjustPlan\":0,\"algName\":\"USER_FOLLOW_TAG2QUERY\",\"flowName\":\"SuggestLikeSearch\",\"markMatchUser\":0,\"newPool\":0,\"ss\":true,\"upFlag\":0,\"w\":\"9.4112\"}","p":"1","r":"0","s":"search_fill","tx":"l月","cst":"0","og":"","ext":"{\"oac\":0,\"oct\":1777000316208,\"eventId\":\"k1-19\",\"pageScene\":\"discovery\",\"handlerTime\":1777000330373,\"repeat\":1,\"ip\":\"39.180.82.135\",\"deviceModel\":\"STG-AL00\",\"page\":\"search_fill\",\"customId\":\"b25d281d3a661b48\",\"preSource\":\"{\\\"ext_itemId\\\":\\\"11768260332\\\",\\\"ext_itemType\\\":\\\"TEXT\\\",\\\"itemId\\\":\\\"11768261333\\\",\\\"itemType\\\":\\\"TEXT\\\",\\\"module\\\":\\\"note_collection\\\",\\\"scene\\\":\\\"note\\\"}\"}","t":"1777000330273","av":"8.3.40","day":"2026-04-24","ref":""}
                ActionHiveDto actionHiveDto = actionHiveDtoMap.get(DataType.NoBlog);
                System.out.println("====2: " + objectMapper.writeValueAsString(actionHiveDto));
            }
        }
    }
}
