package com.netease.lofter.tango.impl.web.vo;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
public class PrimiaryKey implements Serializable {
    private static final long serialVersionUID = -4959284068845802749L;
    @NotNull(message = "id missing")
    private Long id;
}
