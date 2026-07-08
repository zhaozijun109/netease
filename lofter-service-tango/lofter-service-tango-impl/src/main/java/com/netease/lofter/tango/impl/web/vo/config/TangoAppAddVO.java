package com.netease.lofter.tango.impl.web.vo.config;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Getter
@Setter
public class TangoAppAddVO implements Serializable {

    private static final long serialVersionUID = 4943607302063306576L;
    /**
     * 应用ID
     */
    @NotBlank(message = "appId missing")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9-]*$", message = "appId illegal")
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
