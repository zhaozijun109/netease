package com.netease.bdms.ndi.service.web.controller.interceptor;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class Worker implements Serializable {

  private String email;
  private String username;
  private String product;
  private String cluster;
  private Integer productId;
  private String clusterId;

  public Worker() {
  }

  public Worker(String username, String email) {
    this.username = username;
    this.email = email;
  }

}
