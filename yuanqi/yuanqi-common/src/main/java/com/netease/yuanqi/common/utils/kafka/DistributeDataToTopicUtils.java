package com.netease.yuanqi.common.utils.kafka;

import java.util.HashMap;
import java.util.Map;

public class DistributeDataToTopicUtils {
    private static final Map<String, String> APPKEY_TO_TOPIC_MAP = new HashMap<>();

    static {
        // lofter.mda.online
        APPKEY_TO_TOPIC_MAP.put("MA-A4FE-A88932E7A98F", "lofter.mda.online");
        APPKEY_TO_TOPIC_MAP.put("MA-9A4C-437494F370B3", "lofter.mda.online");
        APPKEY_TO_TOPIC_MAP.put("MA-88DF-03AA6989372E", "lofter.mda.online");

        // lofter.wap.online
        APPKEY_TO_TOPIC_MAP.put("MA-B4E8-3BEB9540671E", "lofter.wap.online");

        // lofter.web.online
        APPKEY_TO_TOPIC_MAP.put("MA-BFD7-963BF6846668", "lofter.web.online");

        // lofter.miniprogram.online
        APPKEY_TO_TOPIC_MAP.put("MA-9A8D-CD9AF494B5CA", "lofter.miniprogram.online");
        APPKEY_TO_TOPIC_MAP.put("MA-95F8-11579327B719", "lofter.miniprogram.online");
        APPKEY_TO_TOPIC_MAP.put("MA-80EA-80B5DDA3EF06", "lofter.miniprogram.online");

        // lofter.bookstore-miniprogram.online
        APPKEY_TO_TOPIC_MAP.put("MA-B5BE-6CA81F076C77", "lofter.bookstore-miniprogram.online");
        APPKEY_TO_TOPIC_MAP.put("MA-8252-5F00D58458E9", "lofter.bookstore-miniprogram.online");

        // snail.weixin.online
        APPKEY_TO_TOPIC_MAP.put("MA-9FC9-CF599AC253A7", "snail.weixin.online");

        // snail.mda.online
        APPKEY_TO_TOPIC_MAP.put("MA-BA75-98D2E529299D", "snail.mda.online");
        APPKEY_TO_TOPIC_MAP.put("MA-863E-0AF9334EB8D9", "snail.mda.online");
        APPKEY_TO_TOPIC_MAP.put("MA-A3BA-C19FB112C5D2", "snail.mda.online");
        APPKEY_TO_TOPIC_MAP.put("MA-986A-4C0D07A03EE5", "snail.mda.online");

        // snail.web.online
        APPKEY_TO_TOPIC_MAP.put("MA-9691-1BA279D56416", "snail.web.online");

        // snail.wap.online
        APPKEY_TO_TOPIC_MAP.put("MA-9573-98C8C258F232", "snail.wap.online");

        // ycy.mda.online
        APPKEY_TO_TOPIC_MAP.put("MA-B7CD-1E5D39021C73", "ycy.mda.online");
        APPKEY_TO_TOPIC_MAP.put("MA-A63A-A16B4F5E9AD2", "ycy.mda.online");

        // ycy.na.mda.online
        APPKEY_TO_TOPIC_MAP.put("MA-D611-6274DD96BBDD", "ycy.na.mda.online");
        APPKEY_TO_TOPIC_MAP.put("MA-F0B6-BAE6BABF813A", "ycy.na.mda.online");

        // vc.mda.online
        APPKEY_TO_TOPIC_MAP.put("MA-BA51-F8C059C0C618", "vc.mda.online");
        APPKEY_TO_TOPIC_MAP.put("MA-AA46-CB3D4835ED29", "vc.mda.online");

        // vc.wap.online
        APPKEY_TO_TOPIC_MAP.put("MA-BD70-EE13FDC8DB51", "vc.wap.online");

        // ruyuan.miniprogram.online
        APPKEY_TO_TOPIC_MAP.put("MA-A575-70E1FDC999E8", "ruyuan.miniprogram.online");

        // yuedu.mda.online
        APPKEY_TO_TOPIC_MAP.put("MA-A022-DCDF53857BD0", "yuedu.mda.online");
        APPKEY_TO_TOPIC_MAP.put("MA-9740-AF61BF5E1416", "yuedu.mda.online");

        // vc game
        APPKEY_TO_TOPIC_MAP.put("MA-BF04-E6819DFA8632", "vc.game.web.online");
    }

    public static String getUselessMdaLogTopic() {
        return "hubble.useless.online";
    }

    public static String getUnexpectedMdaLogTopic() {
        return "hubble.unexpected.online";
    }

    public static String getMdaLogTopicWithEventType(String eventType) {
        if ("LofterPushEvent".equals(eventType)) {
            return "lofter.push.reach";
        }
        if ("LofterExceptionEvent".equals(eventType)) {
            return "lofter_exception_log";
        }
        if ("LofterMonitorEvent".equals(eventType)) {
            return "lofter_simulation_log";
        }
        if ("AdEvent".equals(eventType)) {
            return "lofter_ad_log";
        }
        return getUselessMdaLogTopic();
    }

    public static String getMdaLogTopicWithAppKey(String appKey) {
        return APPKEY_TO_TOPIC_MAP.getOrDefault(appKey, getUselessMdaLogTopic());
    }
}
