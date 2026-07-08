package com.netease.lofter.tango.impl.entity;

import com.netease.yaolu.commons.spring.mybatis.annotation.Key;
import com.netease.yaolu.commons.spring.mybatis.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

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
@FieldNameConstants
@Table("`tango_access_users`")
public class TangoAccessUsers implements Serializable {
    private static final long serialVersionUID = -1615772169871534290L;
    /**
     * 主键
     */
    @Key
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