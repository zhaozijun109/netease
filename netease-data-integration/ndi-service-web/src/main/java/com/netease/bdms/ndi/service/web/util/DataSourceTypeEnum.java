package com.netease.bdms.ndi.service.web.util;

import com.netease.bdms.ndi.service.web.exception.NdiException;
import org.apache.commons.lang3.StringUtils;

/**
 * @ClassName DataSourceType
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
public enum DataSourceTypeEnum {
  MySQL((byte) 1),
  HIVE((byte) 2),
  DDB((byte)3),
  DDBQS((byte)4),
  ORACLE((byte)5);
  private byte code;

  DataSourceTypeEnum() {
  }

  DataSourceTypeEnum(byte code) {
    this.code = code;
  }

  public byte getCode() {
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

  public boolean equalWith(String type) {
    return StringUtils.equalsIgnoreCase(this.name(), type);
  }

  /**
   * 根据类型找到对应的枚举
   */
  public static byte valueOfType(String type) {
    for (DataSourceTypeEnum typeEnum : DataSourceTypeEnum.values()) {
      if (typeEnum.name().equalsIgnoreCase(type)) {
        return typeEnum.getCode();
      }
    }
    throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(),
        type + " can not be converted to DataSourceTypeEnum");
  }

  public static String nameOfType(byte type) {
    for (DataSourceTypeEnum typeEnum : DataSourceTypeEnum.values()) {
      if (typeEnum.getCode() == type) {
        return typeEnum.name().toLowerCase();
      }
    }
    throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(),
        type + " can not be converted to DataSourceTypeEnum");
  }


  public static void main(String[] args) {
    System.out.println(DDB.getName());
  }
}
