package com.netease.bdms.ndi.service.web.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.netease.bdms.ndi.service.web.config.ContextConstant;
import com.netease.bdms.ndi.service.web.config.NdiContext;
import com.netease.bdms.ndi.service.web.controller.interceptor.Worker;
import com.netease.bdms.ndi.service.web.dao.*;
import com.netease.bdms.ndi.service.web.dto.task.*;
import com.netease.bdms.ndi.service.web.exception.NdiException;
import com.netease.bdms.ndi.service.web.exception.TaskException;
import com.netease.bdms.ndi.service.web.param.User;
import com.netease.bdms.ndi.service.web.param.task.*;
import com.netease.bdms.ndi.service.web.pojo.*;
import com.netease.bdms.ndi.service.web.service.*;
import com.netease.bdms.ndi.service.web.util.*;
import com.netease.bdms.ndi.service.web.util.constant.CommonConstants;
import com.netease.bdms.ndi.service.web.util.constant.ResponseCodeConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.spark.sql.catalyst.TableIdentifier;
import org.apache.spark.sql.execution.SparkSqlParser;
import org.apache.spark.sql.execution.datasources.CreateTable;
import org.apache.spark.sql.internal.SQLConf;
import org.apache.spark.sql.types.StructField;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scala.collection.JavaConverters;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName TaskServiceImpl
 * @Description 任务服务实现类
 * @Author Min Zhao
 * @Version 1.0
 **/
@Slf4j
@Service
public class TaskServiceImpl implements TaskService {
  @Autowired
  private TaskDevelopDOMapper taskDevelopDOMapper;
  @Autowired
  private TaskOnlineDOMapper taskOnlineDOMapper;
  @Autowired
  private TaskDataSourceService taskDataSourceService;
  @Autowired
  private ReaderService readerService;
  @Autowired
  private WriterService writerService;

  @Override
  public ListDevelopTasksDTO listDevelopTasks(ListTasksParam param) {
    Worker worker = NdiContext.get(ContextConstant.WORKER);
    Preconditions.checkArgument(worker != null, "获取用户信息失败, 请重新登录");
    ListDevelopTasksDTO listDevelopTasksDTO = new ListDevelopTasksDTO();
    Byte readerTypeInt = null;
    Byte writerTypeInt = null;
    String readerType = param.getReaderType();
    String writerType = param.getWriterType();
    if (!StringUtils.isEmpty(readerType)) {
      readerTypeInt = DataSourceTypeEnum.valueOfType(readerType);
    }
    if (!StringUtils.isEmpty(writerType)) {
      writerTypeInt = DataSourceTypeEnum.valueOfType(writerType);
    }

    String searchType = param.getSearchType();

    PageHelper.startPage(param.getPageNum(), param.getPageSize());
    List<TaskDevelopDO> taskDevelopDOList = taskDevelopDOMapper.selectTaskList(
        readerTypeInt, writerTypeInt, worker.getProduct(),
        worker.getCluster(), searchType, param.getSearchKey(), param.getSortType(), param.getSortBy());
    Integer total = taskDevelopDOMapper.selectTaskCount(
        readerTypeInt, writerTypeInt, worker.getProduct(), worker.getCluster(), searchType, param.getSearchKey());
    List<DevelopTaskDTO> developTaskDTOList = taskDevelopDOList.parallelStream()
        .map(item -> developTaskDO2DTO(item)).collect(Collectors.toList());
    PageInfo pageInfo = new PageInfo(developTaskDTOList);
    listDevelopTasksDTO.setTotal(total);
    listDevelopTasksDTO.setDevelopTaskDTOList(pageInfo.getList());
    return listDevelopTasksDTO;
  }

  private DevelopTaskDTO developTaskDO2DTO(TaskDevelopDO taskDevelopDO) {
    ParamUtil.validate(taskDevelopDO);
    DevelopTaskDTO developTaskDTO = new DevelopTaskDTO();
    Byte readerType = taskDevelopDO.getReaderType();
    Long readerId = taskDevelopDO.getReaderId();
    Reader reader = getReader(readerType, readerId);
    developTaskDTO.setReader(JSONObject.toJSON(reader));
    Byte writerType = taskDevelopDO.getWriterType();
    Long writerId = taskDevelopDO.getWriterId();
    Writer writer = getWriter(writerType, writerId);
    developTaskDTO.setWriter(JSONObject.toJSON(writer));
    developTaskDTO.setTaskStatus(TaskStatus.getName(taskDevelopDO.getStatus()));
    developTaskDTO.setTaskName(taskDevelopDO.getTaskName());
    developTaskDTO.setOwner(taskDevelopDO.getOwner());
    developTaskDTO.setModifyTime(DateUtil.format(taskDevelopDO.getModifyTime()));
    developTaskDTO.setModifier(taskDevelopDO.getModifier());
    developTaskDTO.setCreator(taskDevelopDO.getCreator());
    developTaskDTO.setCreateTime(DateUtil.format(taskDevelopDO.getCreateTime()));
    developTaskDTO.setReaderType(DataSourceTypeEnum.nameOfType(taskDevelopDO.getReaderType()));
    developTaskDTO.setWriterType(DataSourceTypeEnum.nameOfType(taskDevelopDO.getWriterType()));
    developTaskDTO.setTaskId(taskDevelopDO.getTaskId());
    return developTaskDTO;
  }

  @Override
  public ListOnlineTasksDTO listOnlineTasks(ListTasksParam param) {
    ParamUtil.validate(param);
    Worker worker = NdiContext.get(ContextConstant.WORKER);
    Preconditions.checkArgument(worker != null, "获取用户信息失败");
    ListOnlineTasksDTO listOnlineTasksDTO = new ListOnlineTasksDTO();
    Byte readerType = null;
    Byte writerType = null;
    String readerTypeStr = param.getReaderType();
    String writerTypeStr = param.getWriterType();
    if (!StringUtils.isEmpty(readerTypeStr)) {
      readerType = DataSourceTypeEnum.valueOfType(readerTypeStr);
    }
    if (!StringUtils.isEmpty(writerTypeStr)) {
      writerType = DataSourceTypeEnum.valueOfType(writerTypeStr);
    }

    PageHelper.startPage(param.getPageNum(), param.getPageSize());
    List<TaskOnlineDO> taskOnlineDOList = taskOnlineDOMapper.selectTaskList(
        readerType, writerType, worker.getProduct(),
        worker.getCluster(), param.getSearchType(), param.getSearchKey(), param.getSortType(), param.getSortBy());

    List<OnlineTaskDTO> onlineTaskDTOS = taskOnlineDOList.parallelStream()
        .map((TaskOnlineDO item) -> onlineTaskDO2TaskDTO(item)).collect(Collectors.toList());
    Integer total = taskOnlineDOMapper.selectTaskCount(
        readerType, writerType, worker.getProduct(), worker.getCluster(), param.getSearchType(), param.getSearchKey());
    listOnlineTasksDTO.setTotal(total);
    listOnlineTasksDTO.setOnlineTaskDTOList(onlineTaskDTOS);
    return listOnlineTasksDTO;
  }

  @Override
  public ModifyTasksOwnerDTO modifyTaskOwner(ModifyTasksOwnerParam modifyTasksOwnerParam) {
    ParamUtil.validate(modifyTasksOwnerParam);
    String product = NdiContext.get(ContextConstant.PRODUCT);
    ModifyTasksOwnerDTO modifyTasksOwnerDTO = new ModifyTasksOwnerDTO();
    modifyTasksOwnerDTO.setTotal(0);
    List<String> taskIds = modifyTasksOwnerParam.getTaskIds();
    if (taskIds != null && taskIds.size() > 0) {
      int result = taskDevelopDOMapper.updateTasksOwnerByTaskIds(taskIds, modifyTasksOwnerParam.getNewOwner(), product);
      modifyTasksOwnerDTO.setTotal(result);
    }
    return modifyTasksOwnerDTO;
  }

  @Override
  @Transactional
  public SubmitTaskDTO submitTask(SubmitTaskParam submitTaskParam) {
    ParamUtil.validate(submitTaskParam);
    Worker worker = NdiContext.get(ContextConstant.WORKER);
    Preconditions.checkArgument(worker != null, "获取用户信息失败");
    SubmitTaskDTO submitTaskDTO = new SubmitTaskDTO();
    TaskDevelopDO taskDevelopDO = taskDevelopDOMapper.selectByTaskId(submitTaskParam.getTaskId());
    if (taskDevelopDO == null) {
      throw new TaskException(ResponseCodeConstant.TASK_NO_EXIST, "任务已删除");
    }
    TaskOnlineDO taskOnlineDO = taskDevelopDO2OnlineDO(taskDevelopDO);
    Byte taskStatus = taskDevelopDO.getStatus();
    String email = worker.getEmail();
    Date currentDate = new Date();
    if (TaskStatus.NO_SUBMIT.getCode().equals(taskStatus)) {
      taskOnlineDO.setCreator(email);
      taskOnlineDO.setCreateTime(currentDate);
      taskOnlineDO.setVersion(1);
    } else if (TaskStatus.MODIFY_NO_SUBMIT.getCode().equals(taskStatus) || TaskStatus.SUBMIT.getCode().equals(taskStatus)) {
      TaskOnlineDO oldTaskOnlineDO = taskOnlineDOMapper.selectByTaskId(taskDevelopDO.getTaskId());
      taskOnlineDO.setCreator(oldTaskOnlineDO.getCreator());
      taskOnlineDO.setCreateTime(oldTaskOnlineDO.getCreateTime());
      taskOnlineDO.setVersion(oldTaskOnlineDO.getVersion() + 1);
    }
    taskOnlineDO.setModifier(email);
    taskOnlineDO.setModifyTime(currentDate);
    taskOnlineDO.setStatus((byte) -1);

    Byte readerType = taskDevelopDO.getReaderType();
    Long developReaderId = taskDevelopDO.getReaderId();
    Long onlineReaderId = readerService.insertOnlineReader(readerType, developReaderId);
    taskOnlineDO.setReaderId(onlineReaderId);

    Byte writerType = taskDevelopDO.getWriterType();
    Long developWriterId = taskDevelopDO.getWriterId();
    Long onlineWriterId = writerService.insertOnlineWriter(writerType, developWriterId);
    taskOnlineDO.setWriterId(onlineWriterId);

    taskOnlineDOMapper.insert(taskOnlineDO);
    taskDevelopDOMapper.updateTaskStatusByTaskId(taskOnlineDO.getTaskId(), TaskStatus.SUBMIT.getCode());
    submitTaskDTO.setTaskId(submitTaskParam.getTaskId());
    return submitTaskDTO;
  }

  private TaskOnlineDO taskDevelopDO2OnlineDO(TaskDevelopDO taskDevelopDO) {
    ParamUtil.validate(taskDevelopDO);
    TaskOnlineDO taskOnlineDO = new TaskOnlineDO();
    BeanUtils.copyProperties(taskDevelopDO, taskOnlineDO);
    taskOnlineDO.setId(null);
    return taskOnlineDO;
  }

  /**
   * 任务在创建和修改时，写入数据源和任务的关联关系
   * 当删除任务管理时，检查是否存在线上任务，不存在时删除数据源和任务的关联关系
   * 当删除线上任务时，检查是否存在任务管理，不存在时删除数据源和任务的关联关系
   *
   * @param param
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(DeleteTasksParam param) {
    ParamUtil.validate(param);
    Worker worker = NdiContext.get(ContextConstant.WORKER);
    Preconditions.checkArgument(worker != null, "获取用户信息失败");
    String product = worker.getProduct();
    String cluster = worker.getCluster();
    List<String> taskIds = param.getTaskIds();
    if (CollectionUtils.isEmpty(taskIds)) {
      return;
    }
    if (TaskConstant.TaskTypeEnum.ONLINE.equalWith(param.getTaskType())) {
      //online tasks
      //删除线上任务要变更开发任务为未提交
      for (String taskId : taskIds) {
        TaskOnlineDO taskOnlineDO = taskOnlineDOMapper.selectByTaskId(taskId);
        if (taskOnlineDO != null) {
          // delete reader
          deleteReader(taskOnlineDO.getReaderId(), taskOnlineDO.getReaderType());
          // delete writer
          deleteWriter(taskOnlineDO.getWriterId(), taskOnlineDO.getWriterType());
        }
        TaskDevelopDO taskDevelopDO = taskDevelopDOMapper.selectByTaskId(taskId);
        if (taskDevelopDO != null) {
          taskDevelopDO.setStatus(TaskStatus.NO_SUBMIT.getCode());
          taskDevelopDOMapper.updateByPrimaryKeySelective(taskDevelopDO);
        } else {
          deleteTaskDataSource(taskId, product, cluster);
        }

        // delete online task
        taskOnlineDOMapper.deleteByTaskId(taskId, product, cluster);
      }
    } else if (TaskConstant.TaskTypeEnum.DEVELOP.equalWith(param.getTaskType())) {
      // develop tasks
      for (String taskId : taskIds) {
        TaskDevelopDO taskDevelopDO = taskDevelopDOMapper.selectByTaskId(taskId);
        if (taskDevelopDO != null) {
          deleteReader(taskDevelopDO.getReaderId(), taskDevelopDO.getReaderType());
          deleteWriter(taskDevelopDO.getWriterId(), taskDevelopDO.getWriterType());
        }
        taskDevelopDOMapper.deleteByTaskId(taskId, product, cluster);
        // 不存在线上任务时
        TaskOnlineDO taskOnlineDO = taskOnlineDOMapper.selectByTaskId(taskId);
        if (taskOnlineDO == null) {
          deleteTaskDataSource(taskId, product, cluster);
        }
      }
    }
  }

  @Transactional
  @Override
  public TaskDTO create(CreateTaskParam param) {
    ParamUtil.validate(param);
    TaskDevelopDO taskDevelopDO = createTaskParam2TaskDO(param);

    Map insertReaderResult = insertReader(param.getReaderType(), param.getReader());
    Long readerId = (Long) insertReaderResult.get(ReaderServiceImpl.ID);
    String readerUrl = (String) insertReaderResult.get(ReaderServiceImpl.READER_URL);

    taskDevelopDO.setReaderId(readerId);
    taskDevelopDO.setReaderUrl(readerUrl);
    taskDevelopDO.setReaderTableName("");

    String readerType = param.getReaderType();
    String writerType = param.getWriterType();
    Map insertWriterResult = insertWriter(param.getWriterType(), param.getWriter());
    Long writerId = (Long) insertWriterResult.get(WriterServiceImpl.ID);
    String writerTableName = (String) insertWriterResult.get(WriterServiceImpl.TABLE_NAME);
    String writerUrl = (String) insertWriterResult.get(WriterServiceImpl.WRITER_URL);

    taskDevelopDO.setWriterId(writerId);
    taskDevelopDO.setWriterUrl(writerUrl);
    taskDevelopDO.setWriterTableName(writerTableName);

    int result = 0;
    try {
      result = taskDevelopDOMapper.insert(taskDevelopDO);
    } catch (DuplicateKeyException e) {
      throw new TaskException(ResponseCodeConstant.TASK_NAME_EXIST, "任务名以存在");
    }
    if (result != 1) {
      log.error(ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
      throw new NdiException(ProcessStatusEnum.DB_WRITE_ERROR.getCode(),
          ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
    }
    Long id = taskDevelopDO.getId();
    String taskId = taskDevelopDOMapper.selectTaskIdByPrimaryKey(id);

    Set<Long> dataSourceIdSet = Sets.newHashSet();
    if (!StringUtils.equalsIgnoreCase(readerType, DataSourceTypeEnum.HIVE.getName())) {
      List<Long> readerDataSourceIdList = (List<Long>) insertReaderResult.get(ReaderServiceImpl.DATA_SOURCE_ID_LIST);
      dataSourceIdSet.addAll(readerDataSourceIdList);
    }

    if (!StringUtils.equalsIgnoreCase(writerType, DataSourceTypeEnum.HIVE.getName())) {
      Long writerDataSourceId = (Long) insertWriterResult.get(WriterServiceImpl.DATA_SOURCE_ID);
      dataSourceIdSet.add(writerDataSourceId);
    }
    if (CollectionUtils.isNotEmpty(dataSourceIdSet)) {
      String product = NdiContext.get(ContextConstant.PRODUCT);
      String cluster = NdiContext.get(ContextConstant.CLUSTER);
      saveTaskDataSource(taskId, dataSourceIdSet, product, cluster);
    }
    TaskDTO taskDTO = getTaskDTO(taskId, CommonConstants.TASK_TYPE_DEVELOP);
    return taskDTO;
  }

  private void saveTaskDataSource(String taskId, Set<Long> dataSourceIdList, String product, String cluster) {
    for (Long dataSourceId : dataSourceIdList) {
      taskDataSourceService.saveTaskDataSource(dataSourceId, taskId, product, cluster);
    }
  }

  private void updateTaskDataSource(String taskId, Set<Long> dataSourceIdList, String product, String cluster) {
    taskDataSourceService.deleteByTaskId(taskId, product, cluster);
    for (Long dataSourceId : dataSourceIdList) {
      taskDataSourceService.saveTaskDataSource(dataSourceId, taskId, product, cluster);
    }
  }

  private void deleteTaskDataSource(String taskId, String product, String cluster) {
    taskDataSourceService.deleteByTaskId(taskId, product, cluster);
  }

  private TaskDevelopDO createTaskParam2TaskDO(CreateTaskParam param) {
    ParamUtil.validate(param);
    Worker worker = NdiContext.get(ContextConstant.WORKER);
    TaskDevelopDO taskDevelopDO = new TaskDevelopDO();
    JSONArray handlerJSONArray = new JSONArray(param.getHandlers());
    taskDevelopDO.setHandlers(handlerJSONArray.toJSONString());
    taskDevelopDO.setModifier(worker.getEmail());
    taskDevelopDO.setOwner(worker.getEmail());
    taskDevelopDO.setProduct(worker.getProduct());
    taskDevelopDO.setCluster(worker.getCluster());
    taskDevelopDO.setVersion(1);
    taskDevelopDO.setTaskName(param.getName());
    taskDevelopDO.setTaskId(TaskIdUtil.get());
    taskDevelopDO.setTaskDescription(param.getDescription());
    taskDevelopDO.setStatus(TaskStatus.NO_SUBMIT.getCode());
    taskDevelopDO.setModifyTime(new Date());
    taskDevelopDO.setCreateTime(new Date());
    taskDevelopDO.setCreator(worker.getEmail());
    taskDevelopDO.setReaderType(DataSourceTypeEnum.valueOfType(param.getReaderType()));
    taskDevelopDO.setWriterType(DataSourceTypeEnum.valueOfType(param.getWriterType()));
    return taskDevelopDO;
  }

  @Transactional
  @Override
  public TaskDTO modify(ModifyTaskParam param) {
    ParamUtil.validate(param);
    TaskDevelopDO oldTaskDevelopDO = taskDevelopDOMapper.selectByTaskId(param.getTaskId());
    if (DataSourceTypeEnum.valueOfType(param.getReaderType()) != oldTaskDevelopDO.getReaderType()) {
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "Reader type don't support to modify");
    }
    if (DataSourceTypeEnum.valueOfType(param.getWriterType()) != oldTaskDevelopDO.getWriterType()) {
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "Writer type don't support to modify");
    }

    Long readerId = oldTaskDevelopDO.getReaderId();
    Long writerId = oldTaskDevelopDO.getWriterId();
    TaskDevelopDO taskDevelopDO = modifyTaskParam2TaskDO(param);
    //===
    Map updateReaderResult = null;
    try {
      updateReaderResult = updateReader(param.getReaderType(), param.getReader(), readerId);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "Failed to update reader");
    }

    taskDevelopDO.setReaderTableName("");
    Map updateWriterResult = null;
    String readerType = param.getReaderType();
    String writerType = param.getWriterType();
    try {
      updateWriterResult = updateWriter(param.getWriterType(), param.getWriter(), writerId);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "Failed to update writer");
    }

    taskDevelopDO.setWriterTableName("");
    //===
    int result = 0;
    try {
      result = taskDevelopDOMapper.updateByTaskIdSelective(taskDevelopDO);
    } catch (DuplicateKeyException e) {
      throw new TaskException(ResponseCodeConstant.TASK_NAME_EXIST, "任务名已存在");
    }
    if (result != 1) {
      log.error(ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
      throw new NdiException(ProcessStatusEnum.DB_WRITE_ERROR.getCode(),
          ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
    }

    List<Long> readerDataSourceIdList = (List<Long>) updateReaderResult.get("dataSourceIdList");
    Long writerDataSourceId = (Long) updateWriterResult.get(WriterServiceImpl.DATA_SOURCE_ID);
    Set<Long> dataSourceIdSet = Sets.newHashSet();
    if (!StringUtils.equalsIgnoreCase(readerType, "hive")) {
      dataSourceIdSet.addAll(readerDataSourceIdList);
    }

    if (!StringUtils.equalsIgnoreCase(writerType, "hive")) {
      dataSourceIdSet.add(writerDataSourceId);
    }
    if (CollectionUtils.isNotEmpty(dataSourceIdSet)) {
      String product = NdiContext.get(ContextConstant.PRODUCT);
      String cluster = NdiContext.get(ContextConstant.CLUSTER);
      updateTaskDataSource(param.getTaskId(), dataSourceIdSet, product, cluster);
    }

    TaskDTO taskDTO = getTaskDTO(param.getTaskId(), CommonConstants.TASK_TYPE_DEVELOP);
    return taskDTO;
  }

  @Override
  public TaskDTO getTaskDTO(String taskId, String taskType) {
    TaskDTO taskDTO = new TaskDTO();
    if (StringUtils.equalsIgnoreCase(taskType, CommonConstants.TASK_TYPE_DEVELOP)) {
      TaskDevelopDO taskDevelopDO = taskDevelopDOMapper.selectByTaskId(taskId);
      if (taskDevelopDO == null) {
        throw new TaskException(ResponseCodeConstant.TASK_NO_EXIST, "任务已删除");
      }
      taskDTO = developTaskDO2TaskDetailDTO(taskDevelopDO);
    } else if (StringUtils.equalsIgnoreCase(taskType, CommonConstants.TASK_TYPE_ONLINE)) {
      TaskOnlineDO taskOnlineDO = taskOnlineDOMapper.selectByTaskId(taskId);
      if (taskOnlineDO == null) {
        throw new TaskException(ResponseCodeConstant.TASK_NO_EXIST, "任务已删除");
      }
      taskDTO = onlineTaskDO2TaskDetailDTO(taskOnlineDO);
    }

    return taskDTO;
  }

  private TaskDTO developTaskDO2TaskDetailDTO(TaskDevelopDO taskDevelopDO) {
    ParamUtil.validate(taskDevelopDO);
    TaskDTO taskDTO = new TaskDTO();
    User user = new User();
    user.setEmail(taskDevelopDO.getOwner());
    user.setProduct(taskDevelopDO.getProduct());
    user.setCluster(taskDevelopDO.getCluster());
    taskDTO.setUser(user);
    taskDTO.setName(taskDevelopDO.getTaskName());
    taskDTO.setDescription(taskDevelopDO.getTaskDescription());

    taskDTO.setReaderId(taskDevelopDO.getReaderId());
    taskDTO.setReaderType(DataSourceTypeEnum.nameOfType(taskDevelopDO.getReaderType()));
    Object reader = getReader(taskDevelopDO.getReaderType(), taskDevelopDO.getReaderId());
    taskDTO.setReader(reader);

    taskDTO.setWriterId(taskDevelopDO.getWriterId());
    taskDTO.setWriterType(DataSourceTypeEnum.nameOfType(taskDevelopDO.getWriterType()));
    Object writer = getWriter(taskDevelopDO.getWriterType(), taskDevelopDO.getWriterId());
    taskDTO.setWriter(writer);

    JSONArray jsonArray = JSONArray.parseArray(taskDevelopDO.getHandlers());
    List<Object> handlers = JSONArray.parseArray(jsonArray.toJSONString(), Object.class);
    taskDTO.setHandlers(handlers);
    taskDTO.setTaskId(taskDevelopDO.getTaskId());
    return taskDTO;
  }

  private TaskDTO onlineTaskDO2TaskDetailDTO(TaskOnlineDO taskOnlineDO) {
    ParamUtil.validate(taskOnlineDO);
    TaskDTO taskDTO = new TaskDTO();
    User user = new User();
    user.setEmail(taskOnlineDO.getOwner());
    user.setProduct(taskOnlineDO.getProduct());
    user.setCluster(taskOnlineDO.getCluster());
    taskDTO.setUser(user);
    taskDTO.setName(taskOnlineDO.getTaskName());
    taskDTO.setDescription(taskOnlineDO.getTaskDescription());

    taskDTO.setReaderId(taskOnlineDO.getReaderId());
    taskDTO.setReaderType(DataSourceTypeEnum.nameOfType(taskOnlineDO.getReaderType()));
    Object reader = getReader(taskOnlineDO.getReaderType(), taskOnlineDO.getReaderId());
    taskDTO.setReader(reader);

    taskDTO.setWriterId(taskOnlineDO.getWriterId());
    taskDTO.setWriterType(DataSourceTypeEnum.nameOfType(taskOnlineDO.getWriterType()));
    Object writer = getWriter(taskOnlineDO.getWriterType(), taskOnlineDO.getWriterId());
    taskDTO.setWriter(writer);

    JSONArray jsonArray = JSONArray.parseArray(taskOnlineDO.getHandlers());
    List<Object> handlers = JSONArray.parseArray(jsonArray.toJSONString(), Object.class);
    taskDTO.setHandlers(handlers);
    taskDTO.setTaskId(taskOnlineDO.getTaskId());
    return taskDTO;
  }

  private OnlineTaskDTO onlineTaskDO2TaskDTO(TaskOnlineDO taskOnlineDO) {
    ParamUtil.validate(taskOnlineDO);
    OnlineTaskDTO onlineTaskDTO = new OnlineTaskDTO();
    Byte readerType = taskOnlineDO.getReaderType();
    Long readerId = taskOnlineDO.getReaderId();
    Reader reader = getReader(readerType, readerId);
    onlineTaskDTO.setReader(JSONObject.toJSON(reader));
    Byte writerType = taskOnlineDO.getWriterType();
    Long writerId = taskOnlineDO.getWriterId();
    Writer writer = getWriter(writerType, writerId);
    onlineTaskDTO.setWriter(JSONObject.toJSON(writer));
    onlineTaskDTO.setWriterType(DataSourceTypeEnum.nameOfType(taskOnlineDO.getWriterType()));
    onlineTaskDTO.setTaskName(taskOnlineDO.getTaskName());
    onlineTaskDTO.setReaderType(DataSourceTypeEnum.nameOfType(taskOnlineDO.getReaderType()));
    onlineTaskDTO.setOwner(taskOnlineDO.getOwner());
    onlineTaskDTO.setModifyTime(DateUtil.format(taskOnlineDO.getModifyTime()));
    onlineTaskDTO.setModifier(taskOnlineDO.getModifier());
    onlineTaskDTO.setTaskId(taskOnlineDO.getTaskId());
    onlineTaskDTO.setId(taskOnlineDO.getId());
    onlineTaskDTO.setCreator(taskOnlineDO.getCreator());
    onlineTaskDTO.setCreateTime(DateUtil.format(taskOnlineDO.getCreateTime()));
    return onlineTaskDTO;
  }

  @Override
  public ParseHiveSQLDTO parseHiveSQL(ParseHiveSQLParam hiveSQLParam) {
    ParamUtil.validate(hiveSQLParam);
    ParseHiveSQLDTO parseHiveSQLDTO = new ParseHiveSQLDTO();
    try {
      SparkSqlParser parser = new SparkSqlParser();
      String sql = hiveSQLParam.getHiveSQL();
      CreateTable plan = (CreateTable) parser.parsePlan(sql);
      Map<String, Object> result = new HashMap<>();

      if (plan.tableDesc().partitionSchema() != null) {
        List<Map<String, String>> partitions = new ArrayList<>();
        result.put("partitions", partitions);
        List<StructField> fields =
            JavaConverters.seqAsJavaListConverter(plan.tableDesc().partitionSchema().toList()).asJava();
        for (StructField field : fields) {
          Map<String, String> filedAttribute = new HashMap<>();
          partitions.add(filedAttribute);
          filedAttribute.put("datatype", field.dataType().typeName());
          filedAttribute.put("name", field.name());
        }
      } else {
        result.put("partitions", new ArrayList<Map<String, String>>());
      }
      TableIdentifier identifier = plan.tableDesc().identifier();
      String table = identifier.table();
      String[] splits = table.split("\\.", 2);
      if (splits.length == 1) {
        result.put("table", splits[0]);
      } else {
        result.put("database", splits[0]);
        result.put("table", splits[1]);
      }
      List<Map<String, String>> columns = new ArrayList<>();
      result.put("columns", columns);
      List<StructField> fields = JavaConverters.seqAsJavaListConverter(plan.tableDesc().dataSchema().toList()).asJava();
      for (StructField field : fields) {
        Map<String, String> filedAttribute = new HashMap<>();
        columns.add(filedAttribute);
        filedAttribute.put("datatype", field.dataType().typeName());
        filedAttribute.put("name", field.name());
      }
      parseHiveSQLDTO.setResult(result);
    } catch (Exception e) {
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "Failed to parse HSQL, please check SQL syntax");
    }
    return parseHiveSQLDTO;
  }

  @Override
  public ExecuteTaskDTO getExecuteTask(ExecuteTaskParam executeTaskParam) {
    ParamUtil.validate(executeTaskParam);
    Boolean develop = executeTaskParam.getDevelop();
    ExecuteTaskDTO executeTaskDTO = new ExecuteTaskDTO();
    if (develop) {
      TaskDevelopDO taskDevelopDO = taskDevelopDOMapper.selectByTaskId(executeTaskParam.getTaskId());
      if (taskDevelopDO == null) {
        throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "The task don't exist, please check whether the task has been deleted");
      }
      executeTaskDTO = getExecuteTaskDTO(taskDevelopDO);
    } else {
      TaskOnlineDO taskOnlineDO = taskOnlineDOMapper.selectByTaskId(executeTaskParam.getTaskId());
      if (taskOnlineDO == null) {
        TaskDevelopDO taskDevelopDO = taskDevelopDOMapper.selectByTaskId(executeTaskParam.getTaskId());
        if (taskDevelopDO == null) {
          throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(),
              "The task don't exist, please check whether the task has been deleted");
        } else {
          String taskName = taskDevelopDO.getTaskName();
          throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(),
              "The online task don't exist, please submit the task online first. The task name is " + taskName);
        }
      }
      executeTaskDTO = getExecuteTaskDTO(taskOnlineDO);
    }
    return executeTaskDTO;
  }

  private ExecuteTaskDTO getExecuteTaskDTO(TaskDevelopDO taskDevelopDO) {
    ParamUtil.validate(taskDevelopDO);
    ExecuteTaskDTO taskDTO = new ExecuteTaskDTO();
    User user = new User();
    user.setEmail(taskDevelopDO.getOwner());
    user.setProduct(taskDevelopDO.getProduct());
    user.setCluster(taskDevelopDO.getCluster());
    taskDTO.setUser(user);
    taskDTO.setName(taskDevelopDO.getTaskName());
    taskDTO.setDescription(taskDevelopDO.getTaskDescription());
    taskDTO.setReaderType(DataSourceTypeEnum.nameOfType(taskDevelopDO.getReaderType()));
    JSONObject reader = readerService.getReaderWithConnectionInfo(user, taskDevelopDO.getReaderType(), taskDevelopDO.getReaderId());
    taskDTO.setReader(reader);
    taskDTO.setReaderId(taskDevelopDO.getReaderId());
    taskDTO.setWriterType(DataSourceTypeEnum.nameOfType(taskDevelopDO.getWriterType()));
    JSONObject writer = writerService.getWriterWithConnectionInfo(user, taskDevelopDO.getWriterType(), taskDevelopDO.getWriterId());
    taskDTO.setWriter(writer);
    taskDTO.setWriterId(taskDevelopDO.getWriterId());
    JSONArray handlers = JSONArray.parseArray(taskDevelopDO.getHandlers());
    taskDTO.setHandlers(handlers);
    taskDTO.setTaskId(taskDevelopDO.getTaskId());
    return taskDTO;
  }

  private ExecuteTaskDTO getExecuteTaskDTO(TaskOnlineDO taskOnlineDO) {
    ParamUtil.validate(taskOnlineDO);
    ExecuteTaskDTO taskDTO = new ExecuteTaskDTO();
    User user = new User();
    user.setEmail(taskOnlineDO.getOwner());
    user.setProduct(taskOnlineDO.getProduct());
    user.setCluster(taskOnlineDO.getCluster());
    taskDTO.setUser(user);
    taskDTO.setName(taskOnlineDO.getTaskName());
    taskDTO.setDescription(taskOnlineDO.getTaskDescription());
    taskDTO.setReaderType(DataSourceTypeEnum.nameOfType(taskOnlineDO.getReaderType()));
    JSONObject reader = readerService.getReaderWithConnectionInfo(user, taskOnlineDO.getReaderType(), taskOnlineDO.getReaderId());
    taskDTO.setReader(reader);
    taskDTO.setReaderId(taskOnlineDO.getReaderId());
    taskDTO.setWriterType(DataSourceTypeEnum.nameOfType(taskOnlineDO.getWriterType()));
    JSONObject writer = writerService.getWriterWithConnectionInfo(user, taskOnlineDO.getWriterType(), taskOnlineDO.getWriterId());
    taskDTO.setWriter(writer);
    taskDTO.setWriterId(taskOnlineDO.getWriterId());
    JSONArray handlers = JSONArray.parseArray(taskOnlineDO.getHandlers());
    taskDTO.setHandlers(handlers);
    taskDTO.setTaskId(taskOnlineDO.getTaskId());
    return taskDTO;
  }

//  @Override
//  public String getCreateHQL(JSONObject createHQLParam) {
//    ParamUtil.validate(createHQLParam);
//    JSONObject getTableResponse = dataSourceService.getTable(createHQLParam);
//    JSONArray cols = getTableResponse.getJSONArray("columns");
//    StringBuilder sqlBuilder = new StringBuilder();
//    sqlBuilder.append("CREATE TABLE IF NOT EXISTS `your_table_name`(");
//    Map<String, Integer> columnTypes = HiveTypes.getColumnTypes();
//    if (cols != null && cols.size() > 0) {
//      boolean first = true;
//      for (int i = 0; i < cols.size(); i++) {
//        JSONObject col = cols.getJSONObject(i);
//        String name = col.getString("name");
//        String type = col.getString("type");
//        if (!first) {
//          sqlBuilder.append(", ");
//        }
//        first = false;
//        String formatType = type.substring(0, type.indexOf("(")).toUpperCase();
//        Integer colType = columnTypes.get(formatType);
//        String hiveColType = HiveTypes.toHiveTypeForParquet(colType);
//        sqlBuilder.append("`").append(name).append("` ").append(hiveColType);
//      }
//    }
//    sqlBuilder.append(") ");
//    sqlBuilder.append("COMMENT 'Imported by Mammut' ").append("STORED AS PARQUET");
//    return sqlBuilder.toString();
//  }

  @Override
  public GetTaskOwnerDTO getTaskOwner(GetTaskOwnerParam getTaskOwnerParam) {
    ParamUtil.validate(getTaskOwnerParam);
    GetTaskOwnerDTO getTaskOwnerDTO = new GetTaskOwnerDTO();
    List<String> owners = new ArrayList<>();
    Integer total = 0;
    String product = NdiContext.get(ContextConstant.PRODUCT);
    String cluster = NdiContext.get(ContextConstant.CLUSTER);
    if (TaskConstant.TaskTypeEnum.DEVELOP.equalWith(getTaskOwnerParam.getTaskType())) {
      if (getTaskOwnerParam.getPageNum() == null || getTaskOwnerParam.getPageSize() == null) {
        owners = taskDevelopDOMapper.selectOwnersByProductAndCluster(product, cluster);
      } else {
        PageHelper.startPage(getTaskOwnerParam.getPageNum(), getTaskOwnerParam.getPageSize());
        owners = taskDevelopDOMapper.selectOwnersByProductAndCluster(product, cluster);
      }
      total = taskDevelopDOMapper.selectOwnersCountByProductAndCluster(product, cluster);
    } else if (TaskConstant.TaskTypeEnum.ONLINE.equalWith(getTaskOwnerParam.getTaskType())) {
      if (getTaskOwnerParam.getPageSize() == null || getTaskOwnerParam.getPageNum() == null) {
        owners = taskOnlineDOMapper.selectOwnersByProductAndCluster(product, cluster);
      } else {
        PageHelper.startPage(getTaskOwnerParam.getPageNum(), getTaskOwnerParam.getPageSize());
        owners = taskOnlineDOMapper.selectOwnersByProductAndCluster(product, cluster);
      }

      total = taskOnlineDOMapper.selectOwnersCountByProductAndCluster(product, cluster);
    }
    getTaskOwnerDTO.setTotal(total);
    getTaskOwnerDTO.setOwners(owners);
    return getTaskOwnerDTO;
  }

  @Override
  public JSONObject listTaskNames(ListTaskNamesParam listTaskNamesParam) {
    ParamUtil.validate(listTaskNamesParam);
    List<TaskOnlineDO> onlineDOList = null;
    List<TaskDevelopDO> developDOList = null;
    User user = listTaskNamesParam.getUser();
    String product = user.getProduct();
    String cluster = user.getCluster();
    String taskName = listTaskNamesParam.getTaskName();
    if (listTaskNamesParam.getPageNum() == null || listTaskNamesParam.getPageSize() == null) {
      onlineDOList = taskOnlineDOMapper.selectByProductAndClusterAndTaskName(product, cluster, taskName);
      developDOList = taskDevelopDOMapper.selectByProductAndClusterAndTaskName(product, cluster, taskName);
    } else {
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "Don't split page");
    }

    List<TaskNameDTO> listOnlineTaskNamesDTO = onlineDOList.parallelStream()
        .map(item -> onlineDO2TaskNames(item))
        .collect(Collectors.toList());

    List<TaskNameDTO> listDevelopTaskNamesDTO = developDOList.parallelStream()
        .map(item -> developDO2TaskNames(item))
        .collect(Collectors.toList());
    Set<TaskNameDTO> taskNameDTOSet = new HashSet<>();
    if (listDevelopTaskNamesDTO != null && listDevelopTaskNamesDTO.size() > 0) {
      for (TaskNameDTO taskNameDTO : listDevelopTaskNamesDTO) {
        taskNameDTOSet.add(taskNameDTO);
      }
    }
    if (listOnlineTaskNamesDTO != null && listOnlineTaskNamesDTO.size() > 0) {
      for (TaskNameDTO taskNameDTO : listOnlineTaskNamesDTO) {
        taskNameDTOSet.add(taskNameDTO);
      }
    }
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("total", taskNameDTOSet.size());
    jsonObject.put("taskNames", taskNameDTOSet);
    return jsonObject;
  }

  @Override
  public List<String> listTasksByDataSource(String product, String cluster, Long dataSourceId) {
    List<String> taskList = taskDataSourceService.listTaskListByDataSource(product, cluster, dataSourceId);
    return taskList;
  }

  @Override
  public boolean taskNameExists(String product, String cluster, String taskName, String taskId) {
    return taskDevelopDOMapper.selectTaskNameCount(product, cluster, taskName, taskId) == 1;
  }

  private TaskNameDTO onlineDO2TaskNames(TaskOnlineDO taskOnlineDO) {
    ParamUtil.validate(taskOnlineDO);
    TaskNameDTO taskNameDTO = new TaskNameDTO();
    taskNameDTO.setTaskName(taskOnlineDO.getTaskName());
    taskNameDTO.setTaskId(taskOnlineDO.getTaskId());
    return taskNameDTO;
  }

  private TaskNameDTO developDO2TaskNames(TaskDevelopDO taskDevelopDO) {
    ParamUtil.validate(taskDevelopDO);
    TaskNameDTO taskNameDTO = new TaskNameDTO();
    taskNameDTO.setTaskName(taskDevelopDO.getTaskName());
    taskNameDTO.setTaskId(taskDevelopDO.getTaskId());
    return taskNameDTO;
  }

  private TaskDevelopDO modifyTaskParam2TaskDO(ModifyTaskParam param) {
    Worker worker = NdiContext.get(ContextConstant.WORKER);
    ParamUtil.validate(param);
    TaskDevelopDO taskDevelopDO = new TaskDevelopDO();
    taskDevelopDO.setTaskId(param.getTaskId());
    TaskDevelopDO oldTaskDO = taskDevelopDOMapper.selectByTaskId(param.getTaskId());
    if (oldTaskDO == null) {
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), ProcessStatusEnum.ILLEGAL_ARGUMENT.getMessage());
    }

    JSONArray jsonArray = new JSONArray(param.getHandlers());
    taskDevelopDO.setHandlers(jsonArray.toJSONString());
    taskDevelopDO.setModifier(worker.getEmail());
    taskDevelopDO.setModifyTime(new Date());
    taskDevelopDO.setTaskName(param.getName());
    taskDevelopDO.setTaskDescription(param.getDescription());

    Byte taskStatus = taskDevelopDOMapper.selectTaskStatusByTaskId(param.getTaskId(),
        worker.getProduct(), worker.getCluster());
    if (TaskStatus.NO_SUBMIT.getCode().equals(taskStatus)) {
      taskDevelopDO.setStatus(TaskStatus.NO_SUBMIT.getCode());
    } else if (TaskStatus.SUBMIT.getCode().equals(taskStatus) || TaskStatus.MODIFY_NO_SUBMIT.getCode().equals(taskStatus)) {
      taskDevelopDO.setStatus(TaskStatus.MODIFY_NO_SUBMIT.getCode());
    } else {
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "The product don't have the task");
    }
    return taskDevelopDO;
  }

  private Map insertReader(String readerType, JSONObject reader) {
    switch (readerType.toLowerCase()) {
      case "mysql":
        return readerService.insertMySQLReader(reader);
      case "hive":
        return readerService.insertHiveReader(reader);
      case "ddb":
        return readerService.insertDdbDbiReader(reader);
      case "ddbqs":
        return readerService.insertDdbQsReader(reader);
      case "oracle":
        return readerService.insertOracleReader(reader);
      default:
        throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "The reader type isn't supported");
    }
  }

  private Map updateReader(String readerType, JSONObject reader, Long readerId) {
    switch (readerType.toLowerCase()) {
      case "mysql":
        return readerService.updateMySQLReader(reader, readerId);
      case "hive":
        return readerService.updateHiveReader(reader, readerId);
      case "ddb":
        return readerService.updateDdbDbiReader(reader, readerId);
      case "ddbqs":
        return readerService.updateDdbQsReader(reader, readerId);
      case "oracle":
        return readerService.updateOracleReader(reader, readerId);
      default:
        throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "The reader type isn't supported");
    }
  }

  private Map insertWriter(String writerType, JSONObject writer) {
    switch (writerType.toLowerCase()) {
      case "hive":
        return writerService.insertHiveWriter(writer);
      case "mysql":
        return writerService.insertMySQLWriter(writer);
      case "ddb":
        return writerService.insertDdbDbiWriter(writer);
      case "ddbqs":
        return writerService.insertDdbQsWriter(writer);
      case "oracle":
        return writerService.insertOracleWriter(writer);
      default:
        throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "The writer type isn't supported");
    }
  }

  private Map updateWriter(String writerType, JSONObject writer, Long writerId) {
    switch (writerType.toLowerCase()) {
      case "hive":
        return writerService.updateHiveWriter(writer, writerId);
      case "mysql":
        return writerService.updateMySQLWriter(writer, writerId);
      case "ddb":
        return writerService.updateDdbDbiWriter(writer, writerId);
      case "ddbqs":
        return writerService.updateDdbQsWriter(writer, writerId);
      case "oracle":
        return writerService.updateOracleWriter(writer, writerId);
      default:
        throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "The writer type isn't supported");
    }
  }

  private void deleteReader(Long readerId, Byte readerType) {
    if (DataSourceTypeEnum.MySQL.equalWith(readerType)) {
      readerService.deleteReaderMySQLDO(readerId);
    } else if (DataSourceTypeEnum.HIVE.equalWith(readerType)) {
      readerService.deleteReaderHiveDO(readerId);
    } else if (DataSourceTypeEnum.DDB.equalWith(readerType)) {
      readerService.deleteReaderDBIDO(readerId);
    } else if (DataSourceTypeEnum.DDBQS.equalWith(readerType)) {
      readerService.deleteReaderQSDO(readerId);
    } else if (DataSourceTypeEnum.ORACLE.equalWith(readerType)) {
      readerService.deleteReaderOracleDO(readerId);
    }
  }

  private void deleteWriter(Long writerId, Byte writerType) {
    if (DataSourceTypeEnum.MySQL.equalWith(writerType)) {
      writerService.deleteMySQLWriter(writerId);
    } else if (DataSourceTypeEnum.HIVE.equalWith(writerType)) {
      writerService.deleteHiveWriter(writerId);
    } else if (DataSourceTypeEnum.DDB.equalWith(writerType)) {
      writerService.deleteDBIWriter(writerId);
    } else if (DataSourceTypeEnum.DDBQS.equalWith(writerType)) {
      writerService.deleteQSWriter(writerId);
    } else if (DataSourceTypeEnum.ORACLE.equalWith(writerType)) {
      writerService.deleteOracleWriter(writerId);
    }
  }

  private Reader getReader(Byte readerType, Long readerId) {
    if (DataSourceTypeEnum.MySQL.equalWith(readerType)) {
      return readerService.getMySQLReader(readerId);
    } else if (DataSourceTypeEnum.DDB.equalWith(readerType)) {
      return readerService.getDBIReader(readerId);
    } else if (DataSourceTypeEnum.DDBQS.equalWith(readerType)) {
      return readerService.getQSReader(readerId);
    } else if (DataSourceTypeEnum.HIVE.equalWith(readerType)) {
      return readerService.getHiveReader(readerId);
    } else if (DataSourceTypeEnum.ORACLE.equalWith(readerType)) {
      return readerService.getOracleReader(readerId);
    }
    throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "Unknown reader type");
  }

  private Writer getWriter(Byte writerType, Long writerId) {
    if (DataSourceTypeEnum.HIVE.equalWith(writerType)) {
      return writerService.getHiveWriter(writerId);
    } else if (DataSourceTypeEnum.MySQL.equalWith(writerType)) {
      return writerService.getMySQLWriter(writerId);
    } else if (DataSourceTypeEnum.DDB.equalWith(writerType)) {
      return writerService.getDBIWriter(writerId);
    } else if (DataSourceTypeEnum.DDBQS.equalWith(writerType)) {
      return writerService.getQsWriter(writerId);
    } else if (DataSourceTypeEnum.ORACLE.equalWith(writerType)) {
      return writerService.getOracleWriter(writerId);
    }
    throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "Unknown writer type");
  }
}
