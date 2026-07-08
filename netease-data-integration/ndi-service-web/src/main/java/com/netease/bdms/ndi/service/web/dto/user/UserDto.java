package com.netease.bdms.ndi.service.web.dto.user;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName UserDTO
 * @Description 用户信息dto
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
public class UserDto {

  /**
   * 用户email
   */
  private String email;

  /**
   * 用户姓名
   */
  private String username;

  public UserDto() { }

  public UserDto(String email, String username) {
    this.email = email;
    this.username = username;
  }
}
