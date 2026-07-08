package com.netease.bdms.ndi.service.web.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.netease.bdms.ndi.service.web.dao.ReaderDdbDbiDOMapper;
import com.netease.bdms.ndi.service.web.dao.ReaderDdbQsDOMapper;
import com.netease.bdms.ndi.service.web.dao.ReaderHiveDOMapper;
import com.netease.bdms.ndi.service.web.dao.ReaderMySQLDOMapper;
import com.netease.bdms.ndi.service.web.dao.ReaderOracleDOMapper;
import com.netease.bdms.ndi.service.web.exception.NdiException;
import com.netease.bdms.ndi.service.web.param.User;
import com.netease.bdms.ndi.service.web.param.task.*;
import com.netease.bdms.ndi.service.web.pojo.ReaderDdbDbiDO;
import com.netease.bdms.ndi.service.web.pojo.ReaderDdbQsDO;
import com.netease.bdms.ndi.service.web.pojo.ReaderHiveDO;
import com.netease.bdms.ndi.service.web.pojo.ReaderMySQLDO;
import com.netease.bdms.ndi.service.web.pojo.ReaderOracleDO;
import com.netease.bdms.ndi.service.web.service.DataSourceService;
import com.netease.bdms.ndi.service.web.service.ReaderService;
import com.netease.bdms.ndi.service.web.util.DataSourceTypeEnum;
import com.netease.bdms.ndi.service.web.util.ParamUtil;
import com.netease.bdms.ndi.service.web.util.ProcessStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.*;

/**
 * @ClassName ReaderServiceImpl
 * @Description Reader服务实现类
 * @Author Min Zhao
 * @Version 1.0
 **/
@Slf4j
@Service
public class ReaderServiceImpl implements ReaderService {

  @Autowired
  private DataSourceService dataSourceService;
  @Autowired
  private ReaderMySQLDOMapper readerMySQLDOMapper;
  @Autowired
  private ReaderHiveDOMapper readerHiveDOMapper;
  @Autowired
  private ReaderDdbDbiDOMapper readerDdbDbiDOMapper;
  @Autowired
  private ReaderDdbQsDOMapper readerDdbQsDOMapper;
  @Autowired
  private ReaderOracleDOMapper readerOracleDOMapper;

  public static final String DATA_SOURCE_ID_LIST = "dataSourceIdList";

  public static final String ID = "id";

  public static final String TABLE_NAME = "tableName";

  public static final String READER_URL = "readerUrl";

  @Override
  public Map insertHiveReader(JSONObject reader) {
    ParamUtil.validate(reader);
    HiveReaderDto hiveReaderDto = JSONObject.parseObject(reader.toString(), new TypeReference<HiveReaderDto>(){}.getType());
    JSONObject dataSource = hiveReaderDto.getDataSource();
    String conditions = hiveReaderDto.getConditions();
    JSONArray conf = hiveReaderDto.getConf();
    ReaderHiveDO readerHiveDO = new ReaderHiveDO(dataSource, conditions, conf.toJSONString());

    if (readerHiveDOMapper.insert(readerHiveDO) != 1) {
      log.error(ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
      throw new NdiException(ProcessStatusEnum.DB_WRITE_ERROR.getCode(),
          ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
    }
    JSONArray dataSources = new JSONArray();
    dataSources.add(dataSource);
    Long readerId = readerHiveDO.getId();
    Map<String, Object> readerResult = handleWriterResult(readerId, "", "", Lists.newArrayList());
    return readerResult;
  }

  @Override
  public Map updateHiveReader(JSONObject reader, Long readerId) {
    ParamUtil.validate(reader);
    HiveReaderDto hiveReaderDto = JSONObject.parseObject(reader.toString(), new TypeReference<HiveReaderDto>(){}.getType());
    JSONObject dataSource = hiveReaderDto.getDataSource();
    String conditions = hiveReaderDto.getConditions();
    JSONArray conf = hiveReaderDto.getConf();
    ReaderHiveDO readerHiveDO = new ReaderHiveDO(readerId, dataSource, conditions, conf.toJSONString());
    if (readerHiveDOMapper.updateByPrimaryKeySelective(readerHiveDO) != 1) {
      log.error(ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
      throw new NdiException(ProcessStatusEnum.DB_WRITE_ERROR.getCode(),
          ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
    }
    JSONArray dataSources = new JSONArray();
    dataSources.add(dataSource);
    Map<String, Object> readerResult = handleWriterResult(readerId, "", "", Lists.newArrayList());
    return readerResult;
  }

  @Override
  public Reader getHiveReader(Long readerId) {
    ReaderHiveDO readerHiveDO = readerHiveDOMapper.selectByPrimaryKey(readerId);
    if (readerHiveDO == null) {
      log.warn("Reader为null, WriterId: {}", readerId);
      return null;
    }
    HiveReaderDto hiveReaderDto = new HiveReaderDto();
    hiveReaderDto.setDataSource(readerHiveDO.getDataSource());
    hiveReaderDto.setConditions(readerHiveDO.getConditions());
    hiveReaderDto.setConf(JSONArray.parseArray(readerHiveDO.getConf()));
    return hiveReaderDto;
  }

  @Override
  public Reader getMySQLReader(Long readerId){
    MySQLReaderDto mySQLReaderDto = new MySQLReaderDto();
    ReaderMySQLDO readerMySQLDO = readerMySQLDOMapper.selectByPrimaryKey(readerId);
    if (readerMySQLDO == null) {
      return null;
    }
    mySQLReaderDto.setConditions(readerMySQLDO.getConditions());
    mySQLReaderDto.setConf(JSONArray.parseArray(readerMySQLDO.getConf()));
    JSONArray dataSources = readerMySQLDO.getDataSources();
    mySQLReaderDto.setDataSources(dataSources);
    return mySQLReaderDto;
  }

  @Override
  public Reader getOracleReader(Long readerId) {
    OracleReaderDto oracleReaderDto = new OracleReaderDto();
    ReaderOracleDO oracleDO = readerOracleDOMapper.selectByPrimaryKey(readerId);
    if (oracleDO == null) {
      log.warn("Reader为null, WriterId: {}", readerId);
      return null;
    }
    oracleReaderDto.setDataSources(oracleDO.getDataSources());
    oracleReaderDto.setConditions(oracleDO.getConditions());
    oracleReaderDto.setConf(JSONArray.parseArray(oracleDO.getConf()));
    return oracleReaderDto;
  }

  @Override
  public Map<String, Object> insertDdbDbiReader(JSONObject reader) {
    ParamUtil.validate(reader);
    DBIReaderDto dbiReaderDto = JSONObject.parseObject(reader.toString(), new TypeReference<DBIReaderDto>(){}.getType());
    JSONArray dataSources = dbiReaderDto.getDataSources();
    String conditions = dbiReaderDto.getConditions();
    JSONArray conf = dbiReaderDto.getConf();
    ReaderDdbDbiDO readerDdbDbiDO = new ReaderDdbDbiDO(dataSources, conditions, conf.toJSONString());
    if (readerDdbDbiDOMapper.insert(readerDdbDbiDO) != 1) {
      log.error(ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
      throw new NdiException(ProcessStatusEnum.DB_WRITE_ERROR.getCode(),
          ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
    }
    List<Long> dataSourceIds = getDataSourceIds(dataSources);
    String dataSourceUrl = getDataSourceUrls(dataSourceIds);
    Long readerId = readerDdbDbiDO.getId();
    Map<String, Object> readerResult = handleWriterResult(readerId, "", dataSourceUrl, dataSourceIds);
    return readerResult;
  }

  private Map<String, Object> handleWriterResult(Long readerId, String tableName, String readerUrl, List<Long> dataSourceList) {
    Map<String, Object> readerResult = new HashMap<>();
    readerResult.put(ID, readerId);
    readerResult.put(TABLE_NAME, "");
    readerResult.put(READER_URL, readerUrl);
    readerResult.put(DATA_SOURCE_ID_LIST, dataSourceList);
    return readerResult;
  }

  @Override
  public Map updateDdbDbiReader(JSONObject reader, Long readerId) {
    ParamUtil.validate(reader);
    DBIReaderDto dbiReaderDto = JSONObject.parseObject(reader.toString(), new TypeReference<DBIReaderDto>(){}.getType());
    JSONArray dataSources = dbiReaderDto.getDataSources();
    String conditions = dbiReaderDto.getConditions();
    JSONArray conf = dbiReaderDto.getConf();
    ReaderDdbDbiDO readerDdbDbiDO = new ReaderDdbDbiDO(readerId, dataSources, conditions, conf.toJSONString());
    if (readerDdbDbiDOMapper.updateByPrimaryKeySelective(readerDdbDbiDO) != 1) {
      log.error(ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
      throw new NdiException(ProcessStatusEnum.DB_WRITE_ERROR.getCode(),
          ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
    }

    List<Long> dataSourceIds = getDataSourceIds(dataSources);
    String dataSourceUrl = getDataSourceUrls(dataSourceIds);
    Map<String, Object> readerResult = handleWriterResult(readerId, "", dataSourceUrl, dataSourceIds);
    return readerResult;
  }

  @Override
  public Reader getDBIReader(Long readerId) {
    DBIReaderDto dbiReaderDto = new DBIReaderDto();
    ReaderDdbDbiDO readerDdbDbiDO = readerDdbDbiDOMapper.selectByPrimaryKey(readerId);
    if (readerDdbDbiDO == null) {
      log.warn("Reader为null, WriterId: {}", readerId);
      return null;
    }
    dbiReaderDto.setDataSources(readerDdbDbiDO.getDataSources());
    dbiReaderDto.setConditions(readerDdbDbiDO.getConditions());
    dbiReaderDto.setConf(JSONArray.parseArray(readerDdbDbiDO.getConf()));
    return dbiReaderDto;
  }


  @Override
  public Map insertDdbQsReader(JSONObject reader) {
    ParamUtil.validate(reader);
    QSReaderDto qsReaderDto = JSONObject.parseObject(reader.toString(), new TypeReference<QSReaderDto>(){}.getType());
    JSONArray dataSources = qsReaderDto.getDataSources();
    String conditions = qsReaderDto.getConditions();
    JSONArray conf = qsReaderDto.getConf();
    ReaderDdbQsDO readerDdbQsDO = new ReaderDdbQsDO(dataSources, conditions, conf.toJSONString());

    if (readerDdbQsDOMapper.insert(readerDdbQsDO) != 1) {
      log.error(ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
      throw new NdiException(ProcessStatusEnum.DB_WRITE_ERROR.getCode(),
          ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
    }
    List<Long> dataSourceIds = getDataSourceIds(dataSources);
    String dataSourceUrl = getDataSourceUrls(dataSourceIds);
    Long readerId = readerDdbQsDO.getId();
    Map<String, Object> readerResult = handleWriterResult(readerId, "", dataSourceUrl, dataSourceIds);
    return readerResult;
  }

  @Override
  public Map updateDdbQsReader(JSONObject reader, final Long readerId) {
    ParamUtil.validate(reader);
    QSReaderDto qsReaderDto = JSONObject.parseObject(reader.toString(), new TypeReference<QSReaderDto>(){}.getType());
    JSONArray dataSources = qsReaderDto.getDataSources();
    String conditions = qsReaderDto.getConditions();
    JSONArray conf = qsReaderDto.getConf();
    ReaderDdbQsDO readerDdbQsDO = new ReaderDdbQsDO(readerId, dataSources, conditions, conf.toJSONString());

    if (readerDdbQsDOMapper.updateByPrimaryKeySelective(readerDdbQsDO) != 1) {
      log.error(ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
      throw new NdiException(ProcessStatusEnum.DB_WRITE_ERROR.getCode(),
          ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
    }
    List<Long> dataSourceIds = getDataSourceIds(dataSources);
    String dataSourceUrl = getDataSourceUrls(dataSourceIds);
    Map<String, Object> readerResult = handleWriterResult(readerId, "", dataSourceUrl, dataSourceIds);
    return readerResult;
  }

  @Override
  public Reader getQSReader(Long readerId) {
    QSReaderDto qsReaderDto = new QSReaderDto();
    ReaderDdbQsDO readerDdbQsDO = readerDdbQsDOMapper.selectByPrimaryKey(readerId);
    if (readerDdbQsDO == null) {
      log.warn("Reader为null, WriterId: {}", readerId);
      return null;
    }
    qsReaderDto.setDataSources(readerDdbQsDO.getDataSources());
    qsReaderDto.setConditions(readerDdbQsDO.getConditions());
    qsReaderDto.setConf(JSONArray.parseArray(readerDdbQsDO.getConf()));
    return qsReaderDto;
  }

  @Override
  public Map insertMySQLReader(JSONObject reader) {
    ParamUtil.validate(reader);
    Type readerType = new TypeReference<MySQLReaderDto>() {}.getType();
    MySQLReaderDto mySQLReaderDto = JSONObject.parseObject(reader.toString(), readerType);

    JSONArray dataSources = mySQLReaderDto.getDataSources();
    String conditions = mySQLReaderDto.getConditions();
    JSONArray conf = mySQLReaderDto.getConf();
    ReaderMySQLDO readerMySQLDO = new ReaderMySQLDO(dataSources, conditions, conf.toJSONString());
    List<Long> dataSourceIdList = getDataSourceIds(dataSources);
    String dataSourceUrl = getDataSourceUrls(dataSourceIdList);
    if (readerMySQLDOMapper.insert(readerMySQLDO) != 1) {
      log.error(ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
      throw new NdiException(ProcessStatusEnum.DB_WRITE_ERROR.getCode(),
          ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
    }
    Long readerId = readerMySQLDO.getId();
    Map<String, Object> readerResult = handleWriterResult(readerId, "", dataSourceUrl, dataSourceIdList);
    return readerResult;
  }

  @Override
  public Map updateMySQLReader(JSONObject reader, Long readerId) {
    ParamUtil.validate(reader);
    Type readerType = new TypeReference<MySQLReaderDto>() {}.getType();
    MySQLReaderDto mySQLReaderDto = JSONObject.parseObject(reader.toString(), readerType);
    JSONArray dataSources = mySQLReaderDto.getDataSources();
    String conditions = mySQLReaderDto.getConditions();
    JSONArray conf = mySQLReaderDto.getConf();
    ReaderMySQLDO readerMySQLDO = new ReaderMySQLDO(readerId, dataSources, conditions, conf.toJSONString());

    List<Long> dataSourceIds = getDataSourceIds(dataSources);
    String dataSourceUrl = getDataSourceUrls(dataSourceIds);
    int result = readerMySQLDOMapper.updateByPrimaryKeySelective(readerMySQLDO);
    if (result != 1) {
      log.error(ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
      throw new NdiException(ProcessStatusEnum.DB_WRITE_ERROR.getCode(),
          ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
    }
    Map<String, Object> readerResult = handleWriterResult(readerId, "", dataSourceUrl, dataSourceIds);
    return readerResult;
  }

  private List<Long> getDataSourceIds(JSONArray dataSources){
    List<Long> dataSourceIds = new ArrayList<>();
    if (dataSources != null && dataSources.size() > 0) {
      for (int i = 0; i < dataSources.size(); i++) {
        JSONObject dataSource = dataSources.getJSONObject(i);
        Long dataSourceId = dataSource.getLong("dataSourceId");
        if (dataSourceId == null){
          throw new IllegalArgumentException("The dataSource don't exist id. DataSource: " + dataSource.toJSONString());
        }
        dataSourceIds.add(dataSourceId);
      }
    }
    return dataSourceIds;
  }

  private String getDataSourceUrls(List<Long> dataSourceIds){
    StringBuilder stringBuilder = new StringBuilder();
    if (dataSourceIds != null && dataSourceIds.size() > 0) {
      for (int i = 0; i < dataSourceIds.size(); i++) {
        String url = dataSourceService.getDataSourceUrl(dataSourceIds.get(i));
        stringBuilder.append(url).append("; ");
      }
    }
    String readerUrls = stringBuilder.substring(0, stringBuilder.length() - 2);
    return readerUrls;
  }

  @Override
  public Map insertOracleReader(JSONObject reader) {
    ParamUtil.validate(reader);
    OracleReaderDto oracleReaderDto = JSONObject.parseObject(reader.toString(), new TypeReference<OracleReaderDto>(){}.getType());
    JSONArray dataSources = oracleReaderDto.getDataSources();
    String conditions = oracleReaderDto.getConditions();
    JSONArray conf = oracleReaderDto.getConf();
    ReaderOracleDO readerOracleDO = new ReaderOracleDO(dataSources, conditions, conf.toJSONString());

    if (readerOracleDOMapper.insert(readerOracleDO) != 1) {
      log.error(ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
      throw new NdiException(ProcessStatusEnum.DB_WRITE_ERROR.getCode(),
          ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
    }
    List<Long> dataSourceIds = getDataSourceIds(dataSources);
    String dataSourceUrl = getDataSourceUrls(dataSourceIds);
    Long readerId = readerOracleDO.getId();
    Map<String, Object> readerResult = handleWriterResult(readerId, "", dataSourceUrl, dataSourceIds);
    return readerResult;
  }

  @Override
  public Map updateOracleReader(JSONObject reader, Long readerId) {
    ParamUtil.validate(reader);
    OracleReaderDto oracleReaderDto = JSONObject.parseObject(reader.toString(), new TypeReference<OracleReaderDto>(){}.getType());
    JSONArray dataSources = oracleReaderDto.getDataSources();
    String conditions = oracleReaderDto.getConditions();
    JSONArray conf = oracleReaderDto.getConf();
    ReaderOracleDO readerOracleDO = new ReaderOracleDO(readerId, dataSources, conditions, conf.toJSONString());

    if (readerOracleDOMapper.updateByPrimaryKeySelective(readerOracleDO) != 1) {
      log.error(ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
      throw new NdiException(ProcessStatusEnum.DB_WRITE_ERROR.getCode(),
          ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
    }
    List<Long> dataSourceIds = getDataSourceIds(dataSources);
    String dataSourceUrl = getDataSourceUrls(dataSourceIds);
    Map<String, Object> readerResult = handleWriterResult(readerId, "", dataSourceUrl, dataSourceIds);
    return readerResult;
  }

  @Override
  public Long insertOnlineReader(Byte readerType, Long readerId) {
    if (DataSourceTypeEnum.MySQL.equalWith(readerType)) {
      ReaderMySQLDO readerMySQLDO = readerMySQLDOMapper.selectByPrimaryKey(readerId);
      readerMySQLDO.setId(null);
      readerMySQLDOMapper.insert(readerMySQLDO);
      return readerMySQLDO.getId();
    } else if (DataSourceTypeEnum.HIVE.equalWith(readerType)) {
      ReaderHiveDO readerHiveDO = readerHiveDOMapper.selectByPrimaryKey(readerId);
      readerHiveDO.setId(null);
      readerHiveDOMapper.insert(readerHiveDO);
      return readerHiveDO.getId();
    } else if (DataSourceTypeEnum.DDB.equalWith(readerType)) {
      ReaderDdbDbiDO readerDdbDbiDO = readerDdbDbiDOMapper.selectByPrimaryKey(readerId);
      readerDdbDbiDO.setId(null);
      readerDdbDbiDOMapper.insert(readerDdbDbiDO);
      return readerDdbDbiDO.getId();
    } else if (DataSourceTypeEnum.DDBQS.equalWith(readerType)) {
      ReaderDdbQsDO readerDdbQsDO = readerDdbQsDOMapper.selectByPrimaryKey(readerId);
      readerDdbQsDO.setId(null);
      readerDdbQsDOMapper.insert(readerDdbQsDO);
      return readerDdbQsDO.getId();
    } else if (DataSourceTypeEnum.ORACLE.equalWith(readerType)) {
      ReaderOracleDO readerOracleDO = readerOracleDOMapper.selectByPrimaryKey(readerId);
      readerOracleDO.setId(null);
      readerOracleDOMapper.insert(readerOracleDO);
      return readerOracleDO.getId();
    }
    throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "不支持的数据源类型");
  }

  @Override
  public ReaderOracleDO gerReaderOracleDO(Long readerId) {
    return readerOracleDOMapper.selectByPrimaryKey(readerId);
  }

  @Override
  public ReaderMySQLDO getReaderMySQLDO(Long readerId) {
    return readerMySQLDOMapper.selectByPrimaryKey(readerId);
  }

  @Override
  public ReaderDdbDbiDO getReaderDBIDO(Long readerId) {
    return readerDdbDbiDOMapper.selectByPrimaryKey(readerId);
  }

  @Override
  public ReaderDdbQsDO getReaderQSDO(Long readerId) {
    return readerDdbQsDOMapper.selectByPrimaryKey(readerId);
  }

  @Override
  public ReaderHiveDO getReaderHiveDO(Long readerId) {
    return readerHiveDOMapper.selectByPrimaryKey(readerId);
  }

  @Override
  public Boolean deleteReaderOracleDO(Long readerId) {
    return readerOracleDOMapper.deleteByPrimaryKey(readerId) != 0;
  }

  @Override
  public Boolean deleteReaderMySQLDO(Long readerId) {
    return readerMySQLDOMapper.deleteByPrimaryKey(readerId) != 0;
  }

  @Override
  public Boolean deleteReaderDBIDO(Long readerId) {
    return readerDdbDbiDOMapper.deleteByPrimaryKey(readerId) != 0;
  }

  @Override
  public Boolean deleteReaderQSDO(Long readerId) {
    return readerDdbQsDOMapper.deleteByPrimaryKey(readerId) != 0;
  }

  @Override
  public Boolean deleteReaderHiveDO(Long readerId) {
    return readerHiveDOMapper.deleteByPrimaryKey(readerId) != 0;
  }

  @Override
  public JSONObject getReaderWithConnectionInfo(User user, Byte readerType, Long readerId) {
    JSONObject reader = new JSONObject();
    if (DataSourceTypeEnum.MySQL.equalWith(readerType)) {
      ReaderMySQLDO readerMySQLDO = getReaderMySQLDO(readerId);
      JSONArray readerDataSources = readerMySQLDO.getDataSources();
      if (readerDataSources != null && readerDataSources.size() > 0) {
        for (int i = 0; i < readerDataSources.size(); i++) {
          JSONObject dataSource = readerDataSources.getJSONObject(i);
          JSONObject connectionInfo = getConnectionInfo(user, dataSource);
          dataSource.put("connectionInformation", connectionInfo);
          readerDataSources.remove(i);
          readerDataSources.add(i, dataSource);
        }
      }
      reader.put("dataSources", readerDataSources);
      reader.put("conditions", readerMySQLDO.getConditions());
      reader.put("conf", JSONArray.parseArray(readerMySQLDO.getConf()));
    } else if (DataSourceTypeEnum.HIVE.equalWith(readerType)) {
      ReaderHiveDO readerHiveDO = getReaderHiveDO(readerId);
      JSONObject dataSource = readerHiveDO.getDataSource();
      JSONObject connectionInfo = getConnectionInfo(user, dataSource);
      dataSource.put("connectionInformation", connectionInfo);
      reader.put("dataSources", readerHiveDO.getDataSource());
      reader.put("conditions", readerHiveDO.getConditions());
      reader.put("conf", JSONArray.parseArray(readerHiveDO.getConf()));
    } else if (DataSourceTypeEnum.DDB.equalWith(readerType)) {
      ReaderDdbDbiDO readerDdbDbiDO = getReaderDBIDO(readerId);
      JSONArray readerDataSources = readerDdbDbiDO.getDataSources();
      if (readerDataSources != null && readerDataSources.size() > 0) {
        for (int i = 0; i < readerDataSources.size(); i++) {
          JSONObject dataSource = readerDataSources.getJSONObject(i);
          JSONObject connectionInfo = getConnectionInfo(user, dataSource);
          dataSource.put("connectionInformation", connectionInfo);
          readerDataSources.remove(i);
          readerDataSources.add(i, dataSource);
        }
      }
      reader.put("dataSources", readerDataSources);
      reader.put("conditions", readerDdbDbiDO.getConditions());
      reader.put("conf", JSONArray.parseArray(readerDdbDbiDO.getConf()));
    } else if (DataSourceTypeEnum.DDBQS.equalWith(readerType)) {
      ReaderDdbQsDO readerDdbQsDO = getReaderQSDO(readerId);
      JSONArray readerDataSources = readerDdbQsDO.getDataSources();
      if (readerDataSources != null && readerDataSources.size() > 0) {
        for (int i = 0; i < readerDataSources.size(); i++) {
          JSONObject dataSource = readerDataSources.getJSONObject(i);
          JSONObject connectionInfo = getConnectionInfo(user, dataSource);
          dataSource.put("connectionInformation", connectionInfo);
          readerDataSources.remove(i);
          readerDataSources.add(i, dataSource);
        }
      }
      reader.put("dataSources", readerDataSources);
      reader.put("conditions", readerDdbQsDO.getConditions());
      reader.put("conf", JSONArray.parseArray(readerDdbQsDO.getConf()));
    } else if (DataSourceTypeEnum.ORACLE.equalWith(readerType)) {
      ReaderOracleDO readerOracleDO = gerReaderOracleDO(readerId);
      JSONArray readerDataSources = readerOracleDO.getDataSources();
      if (readerDataSources != null && readerDataSources.size() > 0) {
        for (int i = 0; i < readerDataSources.size(); i++) {
          JSONObject dataSource = readerDataSources.getJSONObject(i);
          JSONObject connectionInfo = getConnectionInfo(user, dataSource);
          dataSource.put("connectionInformation", connectionInfo);
          readerDataSources.remove(i);
          readerDataSources.add(i, dataSource);
        }
      }
      reader.put("dataSources", readerDataSources);
      reader.put("conditions", readerOracleDO.getConditions());
      reader.put("conf", JSONArray.parseArray(readerOracleDO.getConf()));
    }
    return reader;
  }

  private JSONObject getConnectionInfo(User user, JSONObject dataSource) {
    Long dataSourceId = dataSource.getLong("dataSourceId");
    JSONObject getDataSourceParam = new JSONObject();
    getDataSourceParam.put("user", user);
    getDataSourceParam.put("id", dataSourceId);
    JSONObject getDataSourceResult = dataSourceService.get(getDataSourceParam);
    JSONObject connectionInfo = getDataSourceResult.getJSONObject("connectionInformation");
    if (connectionInfo == null) {
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "数据源连接信息为空");
    }
    String dbUser = connectionInfo.getString("user");
    String dbUserName = connectionInfo.getString("userName");
    if (!StringUtils.isBlank(dbUser)) {
      connectionInfo.put("userName", dbUser);
    } else if (!StringUtils.isBlank(dbUserName)) {
      connectionInfo.put("userName", dbUserName);
    } else {
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "数据源user和userName均为空");
    }
    return connectionInfo;
  }
}
