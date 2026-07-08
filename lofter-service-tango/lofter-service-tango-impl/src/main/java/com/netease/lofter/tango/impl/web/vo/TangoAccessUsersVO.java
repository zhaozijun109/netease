package com.netease.lofter.tango.impl.web.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * <p>Title:  </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>@author: guleiyang</p>
 * <p>@Create Time: 2024-06-13 18:23:26</p>
 * 此实体PO对应表:  tango_config_permission
 */
@Getter
@Setter
public class TangoAccessUsersVO implements Serializable {
    private static final long serialVersionUID = 7811464588520167773L;
    /**
     * 主键
     */
    private Long id;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long updateTime;
    /**
     * 操作人
     */
    private String email;
}