package com.netease.bdms.ndi.service.web.dto.datasource;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName DeleteDataSourceReqDto
 * @Description 批量删除数据源请求参数
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
@ToString
public class DeleteDataSourceReqDto {
  private List<Long> id;
}
