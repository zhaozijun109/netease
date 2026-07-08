package com.netease.lofter.tango.impl.web.query.trade.activity;

import com.netease.lofter.tango.impl.web.ro.BaseQuery;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class ActConfigQuery extends BaseQuery {
    private static final long serialVersionUID = -531783808047255842L;
    private String configKey;
}
