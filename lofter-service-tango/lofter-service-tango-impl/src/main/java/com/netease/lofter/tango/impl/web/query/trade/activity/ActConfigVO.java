package com.netease.lofter.tango.impl.web.query.trade.activity;

import com.netease.lofter.tango.impl.web.vo.BaseVO;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class ActConfigVO extends BaseVO {

    private static final long serialVersionUID = -8849839592923903229L;
    /**
     * 配置key
     */
    private String configKey;

    /**
     * 配置value
     */
    private String configValue;
}
