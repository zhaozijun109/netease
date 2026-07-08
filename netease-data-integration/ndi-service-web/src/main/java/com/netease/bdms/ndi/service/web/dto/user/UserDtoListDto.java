package com.netease.bdms.ndi.service.web.dto.user;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @ClassName UserDtoListDto
 * @Description 用户信息dto列表
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
@NoArgsConstructor
public class UserDtoListDto {
  private List<UserDto> users;

  public UserDtoListDto(List<UserDto> users) {
    this.users = users;
  }
}
