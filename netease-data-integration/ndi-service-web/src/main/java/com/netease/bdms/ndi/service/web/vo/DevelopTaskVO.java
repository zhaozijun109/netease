package com.netease.bdms.ndi.service.web.vo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.dto.task.DevelopTaskDTO;
import com.netease.bdms.ndi.service.web.util.ParamUtil;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

/**
 * @ClassName OnlineTaskVO
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
@ToString
public class DevelopTaskVO {
  private String taskName;
  private String readerType;
  private String writerType;
  private Object reader;
  private Object writer;
  private String owner;
  private String taskStatus;
  private String modifyTime;
  private String createTime;
  private String modifier;
  private String creator;
  private String taskId;

  public DevelopTaskVO() {
  }

  public static DevelopTaskVO developTaskDTO2VO(DevelopTaskDTO developTaskDTO, String owner, String modifier, String creator) {
    ParamUtil.validate(developTaskDTO);
    DevelopTaskVO onlineTaskVO = new DevelopTaskVO();
    BeanUtils.copyProperties(developTaskDTO, onlineTaskVO);
    onlineTaskVO.setOwner(owner);
    onlineTaskVO.setCreator(creator);
    onlineTaskVO.setModifier(modifier);
    return onlineTaskVO;
  }

}
