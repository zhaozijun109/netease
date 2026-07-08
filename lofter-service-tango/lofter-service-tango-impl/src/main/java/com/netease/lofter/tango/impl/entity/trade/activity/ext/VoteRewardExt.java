package com.netease.lofter.tango.impl.entity.trade.activity.ext;

import lombok.Data;

import java.util.List;

@Data
public class VoteRewardExt {
    private String activityId;
    private List<Long> rankIds;
}
