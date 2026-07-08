package com.netease.bdms.ndi.service.web.dto.datasource;

import com.netease.bdms.ndi.service.web.util.MetaHubConstant;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName ListDSReqDto
 * @Description 元数据中心获取数据源列表请求参数
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
@ToString
public class ListDSReqDto {
  private Integer accountId;
  private String name;
  private String type;
  private Integer offset;
  private Integer limit;
  private String sortBy = MetaHubConstant.DB_CREATE_TIME;
  private String order = MetaHubConstant.DESC;

  public ListDSReqDto() {
  }

  public ListDSReqDto(Integer accountId, String name, String type,
                      Integer offset, Integer limit, String sortBy, String order) {
    this.accountId = accountId;
    this.name = name;
    this.type = type;
    this.offset = offset;
    this.limit = limit;
    this.sortBy = sortBy;
    this.order = order;
  }
}
