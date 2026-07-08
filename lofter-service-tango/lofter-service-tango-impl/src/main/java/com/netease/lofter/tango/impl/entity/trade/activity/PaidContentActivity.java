package com.netease.lofter.tango.impl.entity.trade.activity;

import com.netease.yaolu.commons.spring.mybatis.annotation.Column;
import com.netease.yaolu.commons.spring.mybatis.annotation.Key;
import com.netease.yaolu.commons.spring.mybatis.annotation.Table;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

/**
 * <p>Title:  </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>@author: guleiyang</p>
 * <p>@Create Time: 2024-07-12 19:28:00</p>
 * 此实体PO对应表:  PaidContent_Activity
 */
@Getter
@Setter
@FieldNameConstants
@Table("`PaidContent_Activity`")
public class PaidContentActivity implements Serializable {
    /**
     * 主键id
     */
    @Key()
    @Column(name="id", value = "seq")
    private Long id;

    /**
     * 活动
     */
    private String name;

    /**
     * 活动编码，唯一
     */
    private String activityCode;

    /**
     * 主活动id
     */
    private Long parentActId;

    /**
     * 配图
     */
    private String img;

    /**
     * 活动类型
     */
    private Integer type;

    /**
     * 状态 0:正常; -1:停止（处理一些突发情况）
     */
    private Byte status;

    /**
     * 活动开始时间
     */
    private Long startTime;

    /**
     * 活动结束时间
     */
    private Long endTime;

    /**
     * 创建时间
     */
    private Date dbCreateTime;

    private static final long serialVersionUID = 2414352114309158705L;
}