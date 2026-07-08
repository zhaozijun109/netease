package com.netease.lofter.tango.impl.web.query.trade.slot;

import com.netease.lofter.tango.impl.web.ro.BaseQuery;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>Title:  </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>@author: jetbi</p>
 * <p>@Create Time: 2024-07-22 21:05:49</p>
 * 此实体PO对应表:  Luck_Prize
 */
@Getter
@Setter
public class LuckPrizeQuery extends BaseQuery implements Serializable {
    /**
     * ID
     */
    private Long id;

    /**
     * 产品标识
     */
    private String appKey;

    /**
     * 活动标识
     */
    private String activityId;

    /**
     * 奖品名称
     */
    private String name;

    /**
     * 奖品图片
     */
    private String picUrl;

    /**
     * 0=实物，1=兑换码，2=红包，3=默认奖品
     */
    private Integer prizeType;

    /**
     * 兑换码使用网址
     */
    private String thirdpartLink;

    /**
     * 奖品数量
     */
    private Integer count;

    /**
     * 是否显示
     */
    private Integer showFlag;

    /**
     * 奖品中奖者在前台显示的数量，包含虚假中奖者
     */
    private Integer showCount;

    /**
     * 排序
     */
    private Integer indexId;

    /**
     * 价格
     */
    private Integer price;

    /**
     * 奖品每日发送数量
     */
    private Integer dailyBingoLimit;

    /**
     * 每人中奖上限
     */
    private Integer userBingoLimit;

    /**
     * 是否有自定义中奖限制：0:不限制，1:限制
     */
    private Byte bingoCheckFlag;

    /**
     * 奖品中奖通知文案
     */
    private String bingoNoticeContent;

    /**
     * 已中奖数量
     */
    private Integer bingoCount;

    /**
     * 产品自定义透传json数据
     */
    private String customInfo;

    private Long createTimeBegin;

    private Long createTimeEnd;

    private static final long serialVersionUID = -7823058507126577907L;
}