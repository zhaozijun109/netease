package com.netease.lofter.tango.impl.web.vo.config;

import com.netease.lofter.tango.impl.web.vo.BaseVO;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class TangoConfigVO extends BaseVO {


    private static final long serialVersionUID = -8849839592923903229L;

    /**
     * 应用ID
     */
    @NotBlank(message = "appId missing")
    private String appId;

    /**
     * 配置key
     */
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9\\-_.]*$", message = "configKey illegal")
    @NotBlank(message = "configKey missing")
    private String configKey;

    /**
     * 配置value
     */
    @NotBlank(message = "configValue missing")
    private String configValue;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 最后操作人
     */
    private String operator;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 配置元数据
     */
    private String configMeta;


    private Set<String> allowUsers = new HashSet<>();


    private boolean canGrant;

    private boolean canEdit;

    private boolean canSyncOnline;


}
