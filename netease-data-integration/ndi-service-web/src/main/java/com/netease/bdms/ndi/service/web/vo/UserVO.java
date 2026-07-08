package com.netease.bdms.ndi.service.web.vo;

import com.netease.bdms.ndi.service.web.util.ParamUtil;
import lombok.Data;

/**
 * @ClassName UserVO
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class UserVO {
  private String email;
  private String userName;

  public static UserVO email2UserVO(String email, String userName) {
    ParamUtil.validate(email);
    UserVO userVO = new UserVO();
    userVO.setEmail(email);
    userVO.setUserName(userName);
    //TODO:
    return userVO;
  }
}
