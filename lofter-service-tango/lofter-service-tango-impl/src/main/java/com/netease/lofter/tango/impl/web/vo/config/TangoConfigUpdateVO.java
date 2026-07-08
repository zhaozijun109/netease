package com.netease.lofter.tango.impl.web.vo.config;

import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
public class TangoConfigUpdateVO extends PrimiaryKey {

    private static final long serialVersionUID = 2985817939722044802L;

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
     * 配置元数据
     */
    private String configMeta;

    /**
     * 环境
     */
    private List<String> envs;

}
