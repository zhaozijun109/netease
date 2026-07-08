package com.netease.lofter.tango.impl.web.query;

import com.netease.lofter.tango.impl.web.ro.BaseQuery;
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
public class TangoConfigPermissionQuery extends BaseQuery implements Serializable {
    private static final long serialVersionUID = 5328027333091830082L;
    /**
     * 主键
     */
    private Long id;
    /**
     * 配置key对应的Id
     */
    private Long configId;
    /**
     * 操作人
     */
    private String operator;
    private Long createTimeBegin;
    private Long createTimeEnd;
}