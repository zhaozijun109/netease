package com.netease.lofter.tango.impl.web.vo.config;

import com.netease.lofter.tango.impl.web.vo.BaseVO;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class TangoAppVO extends BaseVO {

    private static final long serialVersionUID = -7962899814302263948L;

    /**
     * 应用ID
     */
    @NotBlank(message = "appId missing")
    private String appId;

    /**
     * 应用名称
     */
    @NotBlank(message = "appName missing")
    private String appName;

    /**
     * 操作人
     */
    private String operator;

}
