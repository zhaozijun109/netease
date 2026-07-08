package com.netease.bdms.ndi.service.web.util.enums;

import com.netease.bdms.ndi.service.web.exception.NdiException;

import com.netease.bdms.ndi.service.web.util.ProcessStatusEnum;

/**
 * @ClassName AzkabanTaskType
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
public enum AzkabanTaskTypeEnum {

  PROJECT(0),
  FLOW(1),
  NODE(2);

  private int code;

  AzkabanTaskTypeEnum() {
  }

  AzkabanTaskTypeEnum(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public void setCode(byte code) {
    this.code = code;
  }

  public String getName() {
    return this.name().toLowerCase();
  }

  /**
   * 是否为该枚举
   *
   * @param type 枚举类型
   * @return 是否为该枚举
   */
  public boolean equalWith(byte type) {
    return this.getCode() == type;
  }

  /**
   * 根据类型找到对应的枚举
   */
  public static int valueOfType(String type) {
    for (AzkabanTaskTypeEnum typeEnum : AzkabanTaskTypeEnum.values()) {
      if (typeEnum.name().equalsIgnoreCase(type)) {
        return typeEnum.getCode();
      }
    }
    throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(),
        type + " can not be converted to AzkabanTaskTypeEnum");
  }

  public static String nameOfType(byte type) {
    for (AzkabanTaskTypeEnum typeEnum : AzkabanTaskTypeEnum.values()) {
      if (typeEnum.getCode() == type) {
        return typeEnum.name().toLowerCase();
      }
    }
    throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(),
        type + " can not be converted to AzkabanTaskTypeEnum");
  }
}
