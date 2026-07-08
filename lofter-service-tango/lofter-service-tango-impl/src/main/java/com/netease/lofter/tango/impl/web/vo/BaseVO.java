package com.netease.lofter.tango.impl.web.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class BaseVO implements Serializable {
    private static final long serialVersionUID = -6308602271838173522L;
    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;
}
