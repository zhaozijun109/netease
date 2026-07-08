package com.netease.bdms.ndi.service.web.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.compress.utils.Lists;
import scala.Int;

/**
 * @ClassName DataSourceQuoteDto
 * @Description 数据源引用dto
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
public class DataSourceQuoteDto {
  private String creator;
  private List<TaskNodeDto> taskList;
  private Integer total;

  public DataSourceQuoteDto() {
    this.creator = "";
    this.total = 0;
    this.taskList = Lists.newArrayList();
  }

  @Getter
  @Setter
  public static class TaskNodeDto{
    private AzkabanNodeDto node;
    private AzkabanTaskDto task;
    private String owner;

    public TaskNodeDto() {
    }

    public TaskNodeDto(AzkabanNodeDto azkabanNodeDto, AzkabanTaskDto azkabanTaskDto, String owner) {
      this.node = azkabanNodeDto;
      this.task = azkabanTaskDto;
      this.owner = owner;
    }
  }

  @Getter
  @Setter
  public static class AzkabanNodeDto{
    private String name;
    private String address;

    public AzkabanNodeDto() {
    }

    public AzkabanNodeDto(String name, String address) {
      this.name = name;
      this.address = address;
    }
  }

  @Getter
  @Setter
  public static class AzkabanTaskDto{
    private String name;
    private String type;
    private String address;

    public AzkabanTaskDto() {
    }

    public AzkabanTaskDto(String name, String type, String address) {
      this.name = name;
      this.type = type;
      this.address = address;
    }
  }
}
