package com.netease.lofter.tango.impl.web.ro;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class ConfigEnvRO implements Serializable {

    private static final long serialVersionUID = 720920404685451883L;

    @NotNull(message = "configId不能为空")
    private Long configId;

    private Set<String> envs = new HashSet<>();

    /**
     * 最后操作人
     */
    private String operator;
}
