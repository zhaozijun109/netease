package com.netease.lofter.tango.impl.web.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 下拉选择模型
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SelectVO implements Serializable {

    private static final long serialVersionUID = 1224436363359249185L;

    /**
     * 外显标签值
     */
    private String label;

    /**
     * 隐藏实际值
     */
    private String value;
}
