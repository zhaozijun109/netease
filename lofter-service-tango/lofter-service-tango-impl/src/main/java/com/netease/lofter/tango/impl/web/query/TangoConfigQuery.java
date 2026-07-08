package com.netease.lofter.tango.impl.web.query;

import com.netease.lofter.tango.impl.web.ro.BaseQuery;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class TangoConfigQuery extends BaseQuery {
    private static final long serialVersionUID = -531783808047255842L;
    @NotBlank(message = "appId missing")
    private String appId;
    private String configKey;
}
