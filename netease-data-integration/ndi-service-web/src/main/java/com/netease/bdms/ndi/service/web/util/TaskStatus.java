package com.netease.bdms.ndi.service.web.util;

/**
 * @ClassName TaskStatus
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
public enum TaskStatus {
  NO_SUBMIT((byte) 1, "未提交"),
  SUBMIT((byte) 2, "已提交"),
  MODIFY_NO_SUBMIT((byte) 3, "修改未提交");

  private Byte code;
  private String name;

  TaskStatus(Byte code) {
    this.code = code;
  }

  TaskStatus(Byte code, String name) {
    this.code = code;
    this.name = name;
  }

  public static String getName(Byte code) {
    TaskStatus[] taskStatuses = values();
    if (taskStatuses != null) {
      for (int i = 0; i < taskStatuses.length; i++) {
        if (taskStatuses[i].getCode().equals(code)) {
          return taskStatuses[i].name();
        }
      }
    }
    return "";
  }

  public Byte getCode() {
    return code;
  }

  public void setCode(Byte code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public static void main(String[] args) {
    System.out.println(getName((byte) 1));
  }
}
