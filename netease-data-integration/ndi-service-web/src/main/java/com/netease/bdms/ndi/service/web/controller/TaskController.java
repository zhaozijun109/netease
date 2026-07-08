package com.netease.bdms.ndi.service.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.netease.bdms.ndi.service.web.config.ContextConstant;
import com.netease.bdms.ndi.service.web.config.NdiContext;
import com.netease.bdms.ndi.service.web.dto.DataSourceQuoteDto;
import com.netease.bdms.ndi.service.web.dto.task.DeleteTaskDTO;
import com.netease.bdms.ndi.service.web.dto.task.DevelopTaskDTO;
import com.netease.bdms.ndi.service.web.dto.task.ExecuteTaskDTO;
import com.netease.bdms.ndi.service.web.dto.task.GetTaskOwnerDTO;
import com.netease.bdms.ndi.service.web.dto.task.ListDevelopTasksDTO;
import com.netease.bdms.ndi.service.web.dto.task.ListOnlineTasksDTO;
import com.netease.bdms.ndi.service.web.dto.task.ModifyTasksOwnerDTO;
import com.netease.bdms.ndi.service.web.dto.task.OnlineTaskDTO;
import com.netease.bdms.ndi.service.web.dto.task.ParseHiveSQLDTO;
import com.netease.bdms.ndi.service.web.dto.task.SubmitTaskDTO;
import com.netease.bdms.ndi.service.web.dto.task.TaskDTO;
import com.netease.bdms.ndi.service.web.dto.task.TaskNameDTO;
import com.netease.bdms.ndi.service.web.exception.NdiException;
import com.netease.bdms.ndi.service.web.exception.TaskException;
import com.netease.bdms.ndi.service.web.facade.TaskDataSourceFacade;
import com.netease.bdms.ndi.service.web.param.task.CreateTaskParam;
import com.netease.bdms.ndi.service.web.param.task.DeleteTasksParam;
import com.netease.bdms.ndi.service.web.param.task.ExecuteTaskParam;
import com.netease.bdms.ndi.service.web.param.task.GetTaskEditInformationParam;
import com.netease.bdms.ndi.service.web.param.task.GetTaskOwnerParam;
import com.netease.bdms.ndi.service.web.param.task.ListTaskNamesParam;
import com.netease.bdms.ndi.service.web.param.task.ListTasksParam;
import com.netease.bdms.ndi.service.web.param.task.ModifyTaskParam;
import com.netease.bdms.ndi.service.web.param.task.ModifyTasksOwnerParam;
import com.netease.bdms.ndi.service.web.param.task.ParseHiveSQLParam;
import com.netease.bdms.ndi.service.web.param.task.SubmitTaskParam;
import com.netease.bdms.ndi.service.web.service.TaskService;
import com.netease.bdms.ndi.service.web.service.UserService;
import com.netease.bdms.ndi.service.web.util.AuthUtil;
import com.netease.bdms.ndi.service.web.util.ParamUtil;
import com.netease.bdms.ndi.service.web.util.ProcessStatusEnum;
import com.netease.bdms.ndi.service.web.util.ResponseResult;
import com.netease.bdms.ndi.service.web.util.constant.ResponseCodeConstant;
import com.netease.bdms.ndi.service.web.vo.DevelopTaskVO;
import com.netease.bdms.ndi.service.web.vo.ListDevelopTasksVO;
import com.netease.bdms.ndi.service.web.vo.ListOnlineTasksVO;
import com.netease.bdms.ndi.service.web.vo.ListTaskNamesVO;
import com.netease.bdms.ndi.service.web.vo.OnlineTaskVO;
import com.netease.bdms.ndi.service.web.vo.SubmitTaskVO;
import com.netease.bdms.ndi.service.web.vo.TaskOwnerVO;
import com.netease.bdms.ndi.service.web.vo.TaskVO;
import com.netease.bdms.ndi.service.web.vo.UserVO;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName TaskController
 * @Description 任务Controller
 * @Author Min Zhao
 * @Version 1.0
 **/
@RestController
@RequestMapping(value = "/api/v1/task")
public class TaskController {
  @Autowired
  private TaskService taskService;
  @Autowired
  private UserService userService;
  @Autowired
  private AuthUtil authUtil;
  @Autowired
  private HttpServletRequest request;

  @Autowired
  private TaskDataSourceFacade taskDataSourceFacade;

  @PostMapping(value = "/create")
  public ResponseResult create(@RequestBody CreateTaskParam createTaskParam) {
    ParamUtil.validate(createTaskParam);
    TaskDTO taskDTO = null;
    taskDTO = taskService.create(createTaskParam);
    TaskVO taskVO = TaskVO.developTaskDTO2VO(taskDTO);
    return ResponseResult.createBySuccess(taskVO);
  }

  @PostMapping(value = "/modify")
  public ResponseResult modify(@RequestBody ModifyTaskParam modifyTaskParam) {
    ParamUtil.validate(modifyTaskParam);
    TaskDTO taskDTO = null;
    taskDTO = taskService.modify(modifyTaskParam);
    TaskVO taskVO = TaskVO.developTaskDTO2VO(taskDTO);
    return ResponseResult.createBySuccess(taskVO);
  }

  // TODO: type: develop; online
  @PostMapping(value = "/delete")
  public ResponseResult delete(@RequestBody DeleteTasksParam deleteTasksParam) {
    ParamUtil.validate(deleteTasksParam);
    String product = NdiContext.get(ContextConstant.PRODUCT);
    String clusterId = NdiContext.get(ContextConstant.CLUSTER_ID);
    DataSourceQuoteDto dataSourceQuoteDto = taskDataSourceFacade.taskQuote(product, clusterId, deleteTasksParam.getTaskIds(), "", 1, 10000);
    if (dataSourceQuoteDto != null && CollectionUtils.isNotEmpty(dataSourceQuoteDto.getTaskList())) {
      throw new TaskException(ResponseCodeConstant.TASK_QUOTED, "任务被猛犸任务引用，请先解除引用");
    }
    taskService.delete(deleteTasksParam);
    return ResponseResult.createBySuccess();
  }

  // TODO:
  @PostMapping(value = "/develop")
  public ResponseResult listDevelop(@RequestBody ListTasksParam listTasksParam) {
    ParamUtil.validate(listTasksParam);
    ListDevelopTasksDTO listDevelopTasksDTO = null;
    listDevelopTasksDTO = taskService.listDevelopTasks(listTasksParam);
    Integer total = listDevelopTasksDTO.getTotal();
    List<DevelopTaskDTO> developTaskDTOList = listDevelopTasksDTO.getDevelopTaskDTOList();
    String product = NdiContext.get(ContextConstant.PRODUCT);
    List<DevelopTaskVO> developTaskVOList = developTaskDTOList.parallelStream()
        .map(item -> DevelopTaskVO.developTaskDTO2VO(item,
            userService.getProductUsername(product, item.getOwner()),
            userService.getProductUsername(product, item.getModifier()),
            userService.getProductUsername(product, item.getCreator())))
        .collect(Collectors.toList());
    ListDevelopTasksVO listDevelopTasksVO = new ListDevelopTasksVO();
    listDevelopTasksVO.setTotal(total);
    listDevelopTasksVO.setPageNum(listTasksParam.getPageNum());
    listDevelopTasksVO.setPageSize(listTasksParam.getPageSize());
    listDevelopTasksVO.setTasks(developTaskVOList);
    return ResponseResult.createBySuccess(listDevelopTasksVO);
  }

  @PostMapping(value = "/online")
  public ResponseResult listOnline(@RequestBody ListTasksParam listTasksParam) {
    ParamUtil.validate(listTasksParam);
    ListOnlineTasksDTO listOnlineTasksDTO = null;
    listOnlineTasksDTO = taskService.listOnlineTasks(listTasksParam);
    List<OnlineTaskDTO> onlineTaskDTOList = listOnlineTasksDTO.getOnlineTaskDTOList();
    String product = NdiContext.get(ContextConstant.PRODUCT);
    List<OnlineTaskVO> tasks = onlineTaskDTOList.parallelStream()
        .map(item -> OnlineTaskVO.onlineTaskDTO2VO(item,
            userService.getProductUsername(product, item.getOwner()),
            userService.getProductUsername(product, item.getModifier()),
            userService.getProductUsername(product, item.getCreator())))
        .collect(Collectors.toList());
    ListOnlineTasksVO listOnlineTasksVO = new ListOnlineTasksVO();
    listOnlineTasksVO.setTotal(listOnlineTasksDTO.getTotal());
    listOnlineTasksVO.setPageNum(listTasksParam.getPageNum());
    listOnlineTasksVO.setPageSize(listTasksParam.getPageSize());
    listOnlineTasksVO.setTasks(tasks);
    return ResponseResult.createBySuccess(listOnlineTasksVO);
  }

  @PostMapping(value = "/parse")
  public ResponseResult parseHiveSQL(@RequestBody ParseHiveSQLParam parseHiveSQLParam) {
    ParamUtil.validate(parseHiveSQLParam);
    ParseHiveSQLDTO parseHiveSQLDTO = taskService.parseHiveSQL(parseHiveSQLParam);
    return ResponseResult.createBySuccess(parseHiveSQLDTO.getResult());
  }

  @PostMapping(value = "/modifyOwner")
  public ResponseResult modifyOwner(@RequestBody ModifyTasksOwnerParam modifyTasksOwnerParam) {
    ParamUtil.validate(modifyTasksOwnerParam);
    ModifyTasksOwnerDTO modifyTasksOwnerDTO = taskService.modifyTaskOwner(modifyTasksOwnerParam);
    return ResponseResult.createBySuccess(modifyTasksOwnerDTO.getTotal());
  }

  @PostMapping(value = "/editInformation")
  public ResponseResult<TaskDTO> getEditInformation(@RequestBody GetTaskEditInformationParam param) {
    ParamUtil.validate(param);
    // TODO: 对已删除任务的处理
    TaskDTO taskDTO = taskService.getTaskDTO(param.getTaskId(), param.getTaskType());
    return ResponseResult.createBySuccess(taskDTO);
  }

  @PostMapping(value = "/submit")
  public ResponseResult submit(@RequestBody SubmitTaskParam submitTaskParam) {
    ParamUtil.validate(submitTaskParam);
    SubmitTaskDTO submitTaskDTO = taskService.submitTask(submitTaskParam);
    SubmitTaskVO submitTaskVO = SubmitTaskVO.submitTaskDTO2VO(submitTaskDTO);
    return ResponseResult.createBySuccess(submitTaskVO);
  }

  /**
   * 提供client的接口
   *
   * @param executeTaskParam
   * @return
   */
  @PostMapping(value = "/client/task")
  public ResponseResult getTask(@RequestBody ExecuteTaskParam executeTaskParam) {
    ParamUtil.validate(executeTaskParam);
    Boolean checkResult = authUtil.checkSignature(request);
    if (!checkResult) {
      throw new NdiException(ProcessStatusEnum.AUTH_ERROR.getCode(), "没有通过认证，请联系管理员");
    }
    ExecuteTaskDTO executeTaskDTO = taskService.getExecuteTask(executeTaskParam);
    return ResponseResult.createBySuccess(executeTaskDTO);
  }

  @PostMapping(value = "/owner")
  public ResponseResult getOwners(@RequestBody GetTaskOwnerParam getTaskOwnerParam) {
    ParamUtil.validate(getTaskOwnerParam);
    String product = NdiContext.get(ContextConstant.PRODUCT);
    GetTaskOwnerDTO getTaskOwnerDTO = taskService.getTaskOwner(getTaskOwnerParam);
    List<UserVO> userVOList = getTaskOwnerDTO.getOwners().parallelStream()
        .map(item -> UserVO.email2UserVO(item, userService.getProductUsername(product, item)))
        .collect(Collectors.toList());
    TaskOwnerVO taskOwnerVO = new TaskOwnerVO();
    taskOwnerVO.setUserVOList(userVOList);
    taskOwnerVO.setTotal(getTaskOwnerDTO.getTotal());
    taskOwnerVO.setPageNum(getTaskOwnerParam.getPageNum());
    taskOwnerVO.setPageSize(getTaskOwnerParam.getPageSize());
    return ResponseResult.createBySuccess(taskOwnerVO);
  }

  /**
   * 提供猛犸的接口
   * TODO：增加接口认证
   *
   * @param listTaskNamesParam
   * @return
   */
  @PostMapping(value = "/taskName")
  public ResponseResult listExecuteTaskNames(@RequestBody ListTaskNamesParam listTaskNamesParam) {
    ParamUtil.validate(listTaskNamesParam);
    JSONObject jsonObject = taskService.listTaskNames(listTaskNamesParam);
    ListTaskNamesVO listTaskNamesVO = new ListTaskNamesVO();
    Integer total = jsonObject.getInteger("total");
    listTaskNamesVO.setTotal(total);
    listTaskNamesVO.setPageNum(listTaskNamesParam.getPageNum());
    listTaskNamesVO.setPageSize(listTaskNamesParam.getPageSize());
    listTaskNamesVO.setTaskNameVOList((Set<TaskNameDTO>) jsonObject.get("taskNames"));
    return ResponseResult.createBySuccess(listTaskNamesVO);
  }

//  @PostMapping(value = "/hql")
//  public ResponseResult getHql(@RequestBody JSONObject getHqlParam) {
//    ParamUtil.validate(getHqlParam);
//    String result = taskService.getCreateHQL(getHqlParam);
//    return ResponseResult.createBySuccess(result);
//  }

  /**
   * 检测任务名是否在项目和集群下已经存在
   *
   * @param taskName 任务名称
   * @param taskId 任务id
   * @return 是否存在
   */
  @GetMapping(value = "/taskName/exists")
  public ResponseResult<Boolean> taskNameExists(@RequestParam(value = "taskName") String taskName,
                                                @RequestParam(value = "taskId", required = false) String taskId) {
    String product = NdiContext.get(ContextConstant.PRODUCT);
    Preconditions.checkArgument(StringUtils.isNotBlank(product), "获取产品账号失败，请重新登录");
    String cluster = NdiContext.get(ContextConstant.CLUSTER);
    Preconditions.checkArgument(StringUtils.isNoneBlank(cluster), "获取集群信息失败，请重新登录");
    return ResponseResult.createBySuccess(taskService.taskNameExists(product, cluster, taskName, taskId));
  }
}
