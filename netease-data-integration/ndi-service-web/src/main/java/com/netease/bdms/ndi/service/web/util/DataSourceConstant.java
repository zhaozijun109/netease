package com.netease.bdms.ndi.service.web.util;

import com.netease.bdms.ndi.service.web.exception.NdiException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @ClassName DataSourceConstant
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
public class DataSourceConstant {

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public enum DataSourceInsertTypeEnum {

    /**
     * 忽略
     */
    IGNORE(0),

    /**
     *
     */
    OVERWRITE(1),

    /**
     *
     */
    INTO(2);

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

    /**
     * 根据类型找到对应的枚举
     */
    public static Integer valueOfType(String type) {
      for (DataSourceInsertTypeEnum typeEnum : DataSourceInsertTypeEnum.values()) {
        if (typeEnum.name().equalsIgnoreCase(type)) {
          return typeEnum.getType();
        }
      }
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(),
          type + " can not be converted to TableDesignConstant.RelTypeEnum");
    }

    public static String nameOfType(Integer type) {
      for (DataSourceInsertTypeEnum typeEnum : DataSourceInsertTypeEnum.values()) {
        if (typeEnum.getType().equals(type)) {
          return typeEnum.name().toLowerCase();
        }
      }
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(),
          type + " can not be converted to DataSourceConstant.DataSourceInsertTypeEnum");
    }
  }

  /**
   * 数据源连通性检查结果
   *
   *
   */
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public enum ConnectivityResultEnum {

    /**
     * 成功
     */
    SUCCESS(0),

    /**
     * 部分通过
     */
    PORTION_SUCCESS(1),

    /**
     * 失败
     */
    FAILED(2),

    /**
     * 检查中
     *
     */
    CHECKING(3),

    /**
     * 未检查
     */
    UNCHECKED(4),

    /**
     * 完成
     */
    FINISHED(5);

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

    public boolean equalWith(ConnectivityResultEnum connectivityResultEnum) {
      return connectivityResultEnum.equalWith(this.getType());
    }

    public String getName() {
      return this.name().toLowerCase();
    }

    /**
     * 根据类型找到对应的枚举
     */
    public static Integer valueOfType(String type) {
      for (ConnectivityResultEnum typeEnum : ConnectivityResultEnum.values()) {
        if (typeEnum.name().equalsIgnoreCase(type)) {
          return typeEnum.getType();
        }
      }
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(),
          type + " can not be converted to TableDesignConstant.RelTypeEnum");
    }

    public static String nameOfType(Integer type) {
      for (ConnectivityResultEnum typeEnum : ConnectivityResultEnum.values()) {
        if (typeEnum.getType().equals(type)) {
          return typeEnum.name().toLowerCase();
        }
      }
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(),
          type + " can not be converted to DataSourceConstant.ConnectivityResultEnum");
    }

    public static ConnectivityResultEnum valueOfType(Integer type) {
      for (ConnectivityResultEnum typeEnum : ConnectivityResultEnum.values()) {
        if (typeEnum.getType().equals(type)) {
          return typeEnum;
        }
      }
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(),
          type + " can not be converted to DataSourceConstant.ConnectivityResultEnum");
    }
  }

}
