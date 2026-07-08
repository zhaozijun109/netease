package com.netease.lofter.tango.impl.web.query;

import com.netease.lofter.tango.impl.web.ro.BaseQuery;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class TangoAppQuery extends BaseQuery implements Serializable {

    private static final long serialVersionUID = 7906458931963675409L;
    private String appId;

}
