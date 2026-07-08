package com.netease.bdms.ndi.service.web.vo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.dto.task.OnlineTaskDTO;
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
public class OnlineTaskVO {
  private String taskName;
  private String readerType;
  private String writerType;
  private Object reader;
  private Object writer;
  private String owner;
  private String modifyTime;
  private String createTime;
  private String modifier;
  private String creator;
  private String taskId;

  public static OnlineTaskVO onlineTaskDTO2VO(OnlineTaskDTO onlineTaskDTO, String owner, String modifier, String creator) {
    ParamUtil.validate(onlineTaskDTO);
    OnlineTaskVO onlineTaskVO = new OnlineTaskVO();
    BeanUtils.copyProperties(onlineTaskDTO, onlineTaskVO);
    onlineTaskVO.setOwner(owner);
    onlineTaskVO.setModifier(modifier);
    onlineTaskVO.setCreator(creator);
    return onlineTaskVO;
  }
}
