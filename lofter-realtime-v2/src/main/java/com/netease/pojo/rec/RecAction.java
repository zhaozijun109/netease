package com.netease.pojo.rec;

import com.lofter.rs.basic.bean.dto.upload.AlgInfoExtDto;
import com.lofter.rs.basic.bean.dto.upload.ExtraData;

import java.util.Map;

/** Recommend action data. */
public class RecAction {
    private String appName;
    private Boolean logFile;
    private String scene;
    private String account;
    private String itemId;
    private String itemType;
    private Integer rating;
    private String text;
    private Long time;
    private Long cost;
    private Double progress;
    private Integer platform;
    private String algInfo;
    private AlgInfoExtDto algInfoExtDto;
    private String recId;
    private String extraData;
    private ExtraData extData;
    private Map<String, String> itemExtInfo;
    private String appVersion;
    private String source;
    private String refer;
    private String sourceLink;
    private String eventId;
    private String deviceId;
    private String originMsg;
}
