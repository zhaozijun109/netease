package com.netease.yuanqi.lofter.operator.ads.ad;

import com.alibaba.druid.pool.DruidDataSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.yuanqi.lofter.pojo.ads.ad.AdDspEvent;
import com.netease.yuanqi.lofter.pojo.ads.ad.AdRequestRecord;
import com.netease.yuanqi.lofter.pojo.ads.ad.AdSlotMeta;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import javax.sql.DataSource;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdRequestFlatMapFunction extends RichFlatMapFunction<String, AdRequestRecord> {
    private static final Logger LOG = LoggerFactory.getLogger(AdRequestFlatMapFunction.class);

    private ObjectMapper objectMapper;

    private volatile Long lastUpdateTime = 0L;
    private volatile Map<String, AdSlotMeta> slotMap;
    private static final ScheduledExecutorService scheduledExecutorService =
            new ScheduledThreadPoolExecutor(1);

    private Long updateInterval = 600 * 1000L;

    private DataSource dataSource;

    private static final List<Integer> SDK_AD_SOURCE =
            Arrays.asList(1016, 1027, 1022, 1028, 1037, 1042);

    private Long toLong(String input) {
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            LOG.error("parse ad positionId error got: {}", input);
            return 0L;
        }
    }

    private Integer toInt(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            LOG.error("parse ad dspId error got: {}", input);
            return 0;
        }
    }

    @Override
    public void flatMap(String input, Collector<AdRequestRecord> collector) throws Exception {
        Long nextUpdateTime = (System.currentTimeMillis() / updateInterval + 1) * updateInterval;
        if (nextUpdateTime > this.lastUpdateTime) {
            updateSlotMeta();
            this.lastUpdateTime = nextUpdateTime;
        }

        try {
            AdDspEvent dspEvent = objectMapper.readValue(input, AdDspEvent.class);
            String slotId = dspEvent.getSlotId();

            if (Objects.equals(dspEvent.getAppId(), "6ED29071")) {
                AdRequestRecord result = new AdRequestRecord();
                result.setAction("request");
                result.setReqUid(dspEvent.getUuid());
                result.setReqId(dspEvent.getReqid());
                result.setTime(dspEvent.getRequestTime());
                result.setPositionId(toLong(dspEvent.getPositionId()));
                result.setPositionName(dspEvent.getPositionName());
                result.setSlotId(dspEvent.getSlotId());
                result.setSlotType(
                        slotMap.containsKey(slotId) ? slotMap.get(slotId).getSlotType() : "0");
                result.setDspId(dspEvent.getDspId());
                result.setUserId(dspEvent.getBlogId());
                result.setBidPrice(dspEvent.getPrice());
                result.setBidFactor(dspEvent.getBidFactor());
                result.setEcpm(slotMap.containsKey(slotId) ? slotMap.get(slotId).getEcpm() : .0);
                result.setServerWin(dspEvent.getWinFlag());
                result.setOs(dspEvent.getOs());
                result.setLabels(
                        dspEvent.getExt() != null ? dspEvent.getExt().get("labels") : null);
                String userSlot =
                        dspEvent.getExt() != null ? dspEvent.getExt().get("userSlot") : null;
                String userId = String.valueOf(dspEvent.getBlogId());
                Map<String, String> extInfo = new LinkedHashMap<>(2);
                extInfo.put("userId", userId);
                extInfo.put("userSlot", userSlot);
                result.setExtInfo(extInfo);
                result.setAppVersion(dspEvent.getVersion());

                if (result.getReqUid() != null
                        && result.getReqId() != null
                        && result.getSlotId() != null
                        && !result.getReqUid().isEmpty()
                        && !result.getReqId().isEmpty()
                        && !result.getSlotId().isEmpty()) {
                    collector.collect(result);
                }
            }
        } catch (Exception e) {
            LOG.error("error while parsing dsp log: {}", input);
            LOG.error("detail", e);
        }
    }

    @Override
    public void open(OpenContext openContext) throws Exception {
        super.open(openContext);
        objectMapper = new ObjectMapper();

        {
            DruidDataSource ds = new DruidDataSource();
            ds.setDriverClassName("com.netease.lbd.LBDriver");
            ds.setUrl(
                    "jdbc:mysql:ddb://10.59.186.122:6000,10.59.186.123:6000,10.59.186.124:6000/lofter_yaolu_online?connectTimeout=5000&socketTimeout=1800000&characterEncoding=utf-8&user=lofter_bi_gy_rw&password=BEAl4@@v6");
            ds.setLoginTimeout(5000);
            ds.setFailFast(true);
            dataSource = ds;
        }

        updateSlotMeta();
    }

    private void updateSlotMeta() {
        try {
            LOG.info("start updating slot meta data");

            Connection conn = dataSource.getConnection();
            String sql =
                    "select externalPositionId as slotId, cpmUnitPrice as ecpm, updateTime from AD_DspPosition where extJson like '%biddingType\":3%' ";
            ResultSet resultSet = conn.prepareStatement(sql).executeQuery();

            Map<String, AdSlotMeta> slotMap = new HashMap<>(1024);
            while (resultSet.next()) {
                String slotId = resultSet.getString(1);
                Double ecpm = resultSet.getDouble(2);
                Long updateTime = resultSet.getLong(3);
                String slotType = "1";

                if (!slotMap.containsKey(slotId)
                        || slotMap.get(slotId).getUpdateTime() < updateTime) {
                    slotMap.put(slotId, new AdSlotMeta(slotId, slotType, ecpm, updateTime));
                }
            }

            resultSet.close();
            conn.close();

            this.slotMap = slotMap;

            LOG.info("end updating slot meta data, size: {}", this.slotMap.size());
        } catch (SQLException e) {
            LOG.error("load slot meta error", e);
        }
    }
}
