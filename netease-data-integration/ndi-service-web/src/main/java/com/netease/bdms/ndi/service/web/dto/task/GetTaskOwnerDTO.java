package com.netease.bdms.ndi.service.web.dto.task;

import com.netease.bdms.ndi.service.web.vo.UserVO;
import lombok.Data;

import java.util.List;

/**
 * @ClassName GetTaskOwnerDTO
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class GetTaskOwnerDTO {
  private Integer total;
  private List<String> owners;
}
