package com.netease.yuanqi.common.utils.kafka;

import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DsLogArchiveFormatUtils {
    private static final Logger LOG = LoggerFactory.getLogger(DsLogArchiveFormatUtils.class);
    public static final Set<String> DS_JSON_LOG_TOPIC_SET = new HashSet<>();
    public static final Set<String> DS_NO_HEAD_JSON_LOG_TOPIC_SET = new HashSet<>();
    public static final Set<String> DS_LOG_TOPIC_SET = new HashSet<>();

    static {
        // Initialize topic set for different data processing logic
        DS_JSON_LOG_TOPIC_SET.add("rec_ds_article_image_feature");

        DS_NO_HEAD_JSON_LOG_TOPIC_SET.add("adx.newlinkup.online");
        DS_NO_HEAD_JSON_LOG_TOPIC_SET.add("LOFTER.COMMON.ATTRIBUTION.regsiter");
        DS_NO_HEAD_JSON_LOG_TOPIC_SET.add("lofter.push.anti.addiction");
        DS_NO_HEAD_JSON_LOG_TOPIC_SET.add("lofter-web-ddos");
        DS_NO_HEAD_JSON_LOG_TOPIC_SET.add("lofter.outerlinkup.online");
        DS_NO_HEAD_JSON_LOG_TOPIC_SET.add("lofter.creator.attribution");
        DS_NO_HEAD_JSON_LOG_TOPIC_SET.add("lofter.creator.potential");

        DS_LOG_TOPIC_SET.add("lofter.creator-stimulus-pm.online");
        DS_LOG_TOPIC_SET.add("rec.scene.ctr");
        DS_LOG_TOPIC_SET.add("lofter.session.time");
    }
}
