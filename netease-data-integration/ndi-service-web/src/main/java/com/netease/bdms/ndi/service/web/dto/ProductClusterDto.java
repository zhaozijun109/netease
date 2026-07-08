package com.netease.bdms.ndi.service.web.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName ProductClusterDto
 * @Description 产品账号和集群
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
public class ProductClusterDto {

  private String product;

  private List<String> clusters;

  public ProductClusterDto() {
  }

  public ProductClusterDto(String product, List<String> clusters) {
    this.product = product;
    this.clusters = clusters;
  }
}
