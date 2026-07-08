package com.netease.lofter.tango.impl.web.vo.clickhouse;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ClickhousePostExcelVO extends ClickhousePostVO {

    private static final long serialVersionUID = 4160379692317821152L;

    private String ifShuaReStr;
     private String recTrafficRatioStr;
     private String coldStartTrafficRatioStr;
     private String tagRecTrafficRatioStr;
     private String feedRecTrafficRatioStr;

    public String formatPercent(Double recTrafficRatio) {
        if(recTrafficRatio == null) {
            return "0.0";
        }
        return String.format("%.2f", recTrafficRatio * 100) + "%";
    }
}
