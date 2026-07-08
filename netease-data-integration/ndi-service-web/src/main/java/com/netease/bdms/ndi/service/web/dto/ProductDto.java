package com.netease.bdms.ndi.service.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName ProductDto
 * @Description 猛犸产品账号
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
public class ProductDto {
  private Integer productId;
  private String product;

  public ProductDto() {
  }

  public ProductDto(Integer productId, String product) {
    this.productId = productId;
    this.product = product;
  }
}
