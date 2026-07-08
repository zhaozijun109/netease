package com.netease.bdms.ndi.service.web.util;

import com.netease.bdms.ndi.service.web.exception.NdiException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @ClassName TaskConstant
 * @Description 任务相关常量
 * @Author Min Zhao
 * @Version 1.0
 **/
public class TaskConstant {
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public enum TaskTypeEnum {

    /**
     * 开发任务
     */
    DEVELOP(2),

    /**
     * 线上任务
     */
    ONLINE(1);

    /**
     * 状态值
     */
    private Integer type;


    /**
     * 是否为该枚举
     *
     * @param type 枚举类型
     * @return 是否为该枚举
     */
    public boolean equalWith(Integer type) {
      return this.getType().equals(type);
    }

    public String getName() {
      return this.name().toLowerCase();
    }

    /**
     * 根据类型找到对应的枚举
     */
    public static Integer valueOfType(String type) {
      for (TaskConstant.TaskTypeEnum typeEnum : TaskConstant.TaskTypeEnum.values()) {
        if (typeEnum.name().equalsIgnoreCase(type)) {
          return typeEnum.getType();
        }
      }
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(),
          type + " can not be converted to TaskConstant.TaskTypeEnum");
    }

    public static String nameOfType(Integer type) {
      for (TaskConstant.TaskTypeEnum typeEnum : TaskConstant.TaskTypeEnum.values()) {
        if (typeEnum.getType().equals(type)) {
          return typeEnum.name().toLowerCase();
        }
      }
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(),
          type + " can not be converted to TaskConstant.TaskTypeEnum");
    }
  }
}
