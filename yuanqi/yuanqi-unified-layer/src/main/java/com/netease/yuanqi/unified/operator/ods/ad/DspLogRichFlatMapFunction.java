package com.netease.yuanqi.unified.operator.ods.ad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.wm.hubble.avro.AdxDspEvent;
import com.netease.yuanqi.unified.pojo.DspEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DspLogRichFlatMapFunction extends RichFlatMapFunction<String, AdxDspEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(DspLogRichFlatMapFunction.class);

    private ObjectMapper objectMapper;

    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void flatMap(String input, Collector<AdxDspEvent> collector) throws Exception {
        try {
            DspEvent dspEvent = objectMapper.readValue(input, DspEvent.class);
            AdxDspEvent result = new AdxDspEvent();
            result.setAdId(dspEvent.getAdId());
            result.setAppId(dspEvent.getAppId());
            result.setOs(dspEvent.getOs());
            result.setPositionId(dspEvent.getPositionId());
            result.setPositionName(dspEvent.getPositionName());
            result.setSuccess(dspEvent.getSuccess());
            result.setRequestTime(dspEvent.getRequestTime());
            result.setResponseTime(dspEvent.getResponseTime());
            result.setMsg(dspEvent.getMsg());
            result.setExternalAdId(dspEvent.getExternalAdId());
            result.setIp(dspEvent.getIp());
            result.setWakeupBoot(dspEvent.getWakeupBoot());
            result.setWinFlag(dspEvent.getWinFlag());
            result.setLa(dspEvent.getLa());
            result.setLo(dspEvent.getLo());
            result.setVersion(dspEvent.getVersion());
            result.setUuid(dspEvent.getUuid());
            result.setBanwords(dspEvent.getBanwords());
            result.setReqId(dspEvent.getReqid());
            result.setIndustryId(dspEvent.getIndustryId());
            result.setSlotId(dspEvent.getSlotId());
            result.setAdvertiserType(dspEvent.getAdvertiserType());
            result.setPrice(dspEvent.getPrice());
            result.setBlogId(dspEvent.getBlogId());
            result.setBidFactor(dspEvent.getBidFactor());
            result.setDspId(dspEvent.getDspId());

            result.setExt(processMap(dspEvent.getExt()));
            collector.collect(result);
        } catch (Exception e) {
            LOG.error("error while parsing dsp log: {}", input);
            LOG.error("detail", e);
        }
    }

    private Map<CharSequence, CharSequence> processMap(Map<String, String> m) {
        if (m == null) {
            return null;
        } else {
            Map<CharSequence, CharSequence> result = new HashMap<>(m.size());
            for (Map.Entry<String, String> kv : m.entrySet()) {
                result.put(kv.getKey(), kv.getValue());
            }

            return result;
        }
    }
}
