package com.netease.bdms.ndi.service.web.util.constant;

import org.apache.commons.lang3.StringUtils;

/**
 * @ClassName SortTypeEnum
 * @Description 排序类型枚举
 * @Author Min Zhao
 * @Version 1.0
 **/
public enum SortTypeEnum {

  /**
   * 升序
   */
  ASCEND(1),

  /**
   * 降序
   */
  DESCEND(2);

  private int code;

  SortTypeEnum() {
  }

  SortTypeEnum(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
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
  public boolean equalWith(int type) {
    return this.getCode() == type;
  }

  public boolean equalWith(String name) {
    return StringUtils.equalsIgnoreCase(this.getName(), name);
  }

  /**
   * 根据类型找到对应的枚举
   */
  public static int valueOfType(String type) {
    for (SortTypeEnum typeEnum : SortTypeEnum.values()) {
      if (typeEnum.name().equalsIgnoreCase(type)) {
        return typeEnum.getCode();
      }
    }

    throw new IllegalArgumentException(type + " 没有被支持");
  }

  public static String nameOfType(int type) {
    for (SortTypeEnum typeEnum : SortTypeEnum.values()) {
      if (typeEnum.getCode() == type) {
        return typeEnum.name().toLowerCase();
      }
    }
    throw new IllegalArgumentException(type + " 没有被支持");
  }
}
