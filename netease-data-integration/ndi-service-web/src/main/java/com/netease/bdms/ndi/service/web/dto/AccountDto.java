package com.netease.bdms.ndi.service.web.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName AccountDto
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
@ToString
public class AccountDto {
  private List<ProductDto> productDtoList;

  public AccountDto() {
  }

  public AccountDto(List<ProductDto> productDtoList) {
    this.productDtoList = productDtoList;
  }

  @Getter
  @Setter
  @ToString
  public static class ProductDto {
    private Integer productId;
    private String product;
    private List<ClusterDto> clusterDtoList;

    public ProductDto() {
    }

    public ProductDto(Integer productId, String product, List<ClusterDto> clusterDtoList) {
      this.productId = productId;
      this.product = product;
      this.clusterDtoList = clusterDtoList;
    }
  }

  @Getter
  @Setter
  @ToString
  public static class ClusterDto {
    private String clusterId;
    private String cluster;

    public ClusterDto() {
    }

    public ClusterDto(String clusterId, String cluster) {
      this.clusterId = clusterId;
      this.cluster = cluster;
    }
  }
}
