package com.netease.bdms.ndi.service.web.param;

import lombok.Data;

@Data
public class User {
  private String email;
  private String product;
  private String cluster;

  public User() {
  }

  public User(String email, String product){
    this.email = email;
    this.product = product;
  }
}
