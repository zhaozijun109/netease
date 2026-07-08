package com.netease.bdms.ndi.service.web.dto.datasource;

import java.util.List;

import com.netease.bdms.ndi.service.web.util.DataSourceConstant;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @ClassName ConnectivityResultRspDto
 * @Description 连通性结果响应
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
public class ConnectivityResultRspDto {
  /**
   * 元数据中心连通性结果
   */
  private MetahubConnectivityResult metahub;

  /**
   * Azkaban连通性结果
   */
  private List<AzkabanConnectivityResult> azkaban;

  public ConnectivityResultRspDto() {
  }

  public ConnectivityResultRspDto(MetahubConnectivityResult metahub, List<AzkabanConnectivityResult> azkaban) {
    this.metahub = metahub;
    this.azkaban = azkaban;
  }

  @Getter
  @Setter
  @Accessors(chain = true)
  public static class MetahubConnectivityResult {
    private String status;
    private String updateTime;
    private Integer failedNum;
    private Integer totalNum;
    private List<ConnectivityResultDetail> details;

    public MetahubConnectivityResult() {
      this.status = DataSourceConstant.ConnectivityResultEnum.UNCHECKED.name().toLowerCase();
    }
  }

  @Getter
  @Setter
  @Accessors(chain = true)
  public static class AzkabanConnectivityResult {
    private String cluster;
    private String clusterId;
    private String status;
    private String updateTime;
    private Integer failedNum;
    private Integer totalNum;
    private List<ConnectivityResultDetail> details;

    public AzkabanConnectivityResult() {
    }

    public AzkabanConnectivityResult(String clusterId, String cluster) {
      this.cluster = cluster;
      this.clusterId = clusterId;
      this.status = DataSourceConstant.ConnectivityResultEnum.UNCHECKED.name().toLowerCase();
    }
  }

  @Getter
  @Setter
  public static class ConnectivityResultDetail {
    private String host;
    private String message;

    public ConnectivityResultDetail() {
    }

    public ConnectivityResultDetail(String host, String message) {
      this.host = host;
      this.message = message;
    }
  }
}
