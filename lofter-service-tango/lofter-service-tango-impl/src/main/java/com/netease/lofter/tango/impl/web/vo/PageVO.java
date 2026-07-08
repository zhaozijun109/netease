package com.netease.lofter.tango.impl.web.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PageVO implements Serializable {

    private static final long serialVersionUID = -1134318858714545124L;

    /**
     * 必选
     * 当前页面
     */
    private Integer current = 1;

    /**
     * 必选
     * 每页数量
     */
    private Integer size = 10;

    /**
     * 必选
     * 总记录数
     */
    private Integer recordCount = 0;

    /**
     * 必选
     * 总页数
     */
    private Integer total = 0;

    /*************************************以下可选**************************************/

    /**
     * 可选
     * 游标，默认返回本页列表最后一条数据的游标值
     * 举例： 用户列表分页查询场景，使用userId作为游标； 假设本次查询结果中返回的userId列表按顺序分别为：[1001,1002,1003,1004,1005] 则游标可以为为1005
     */
    private String cursor;

    /**
     * 可选
     * 最小可选页码
     */
    private Integer min;

    /**
     * 可选
     * 最大可选页码
     */
    private Integer max;

}
