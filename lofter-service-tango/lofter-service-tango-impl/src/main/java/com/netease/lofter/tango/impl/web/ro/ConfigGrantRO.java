package com.netease.lofter.tango.impl.web.ro;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class ConfigGrantRO implements Serializable {

    private static final long serialVersionUID = 720920404685451883L;

    @NotNull(message = "configId不能为空")
    private Long configId;

    private List<String> emails;
}
