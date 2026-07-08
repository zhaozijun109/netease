package com.netease.bdms.ndi.service.web.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.netease.bdms.ndi.service.web.dao.WriterDdbDbiDOMapper;
import com.netease.bdms.ndi.service.web.dao.WriterDdbQsDOMapper;
import com.netease.bdms.ndi.service.web.dao.WriterHiveDOMapper;
import com.netease.bdms.ndi.service.web.dao.WriterMySQLDOMapper;
import com.netease.bdms.ndi.service.web.dao.WriterOracleDOMapper;
import com.netease.bdms.ndi.service.web.exception.NdiException;
import com.netease.bdms.ndi.service.web.param.User;
import com.netease.bdms.ndi.service.web.param.task.*;
import com.netease.bdms.ndi.service.web.pojo.*;
import com.netease.bdms.ndi.service.web.service.DataSourceService;
import com.netease.bdms.ndi.service.web.service.WriterService;
import com.netease.bdms.ndi.service.web.util.DataSourceConstant;
import com.netease.bdms.ndi.service.web.util.DataSourceTypeEnum;
import com.netease.bdms.ndi.service.web.util.ParamUtil;
import com.netease.bdms.ndi.service.web.util.ProcessStatusEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.*;

/**
 * @ClassName WriterServiceImpl
 * @Description Writer服务实现
 * @Author Min Zhao
 * @Version 1.0
 **/
@Service
public class WriterServiceImpl implements WriterService {
  private static final Logger log = LoggerFactory.getLogger(WriterService.class);
  @Autowired
  private WriterHiveDOMapper writerHiveDOMapper;
  @Autowired
  private WriterMySQLDOMapper writerMySQLDOMapper;
  @Autowired
  private WriterDdbDbiDOMapper writerDdbDbiDOMapper;
  @Autowired
  private WriterDdbQsDOMapper writerDdbQsDOMapper;
  @Autowired
  private DataSourceService dataSourceService;
  @Autowired
  private WriterOracleDOMapper writerOracleDOMapper;

  public static final String DATA_SOURCE_ID = "dataSourceId";

  public static final String ID = "id";

  public static final String TABLE_NAME = "tableName";

  public static final String WRITER_URL = "writerUrl";

  @Override
  public Map insertHiveWriter(JSONObject writer) {
    ParamUtil.validate(writer);
    Type writerType = new TypeReference<HiveWriterDto>() {}.getType();
    HiveWriterDto hiveWriterDto = JSONObject.parseObject(writer.toString(), writerType);
    WriterHiveDO writerHiveDO = new WriterHiveDO();
    writerHiveDO.setCreateTime(new Date());
    writerHiveDO.setModifyTime(new Date());
    JSONObject dataSource = (JSONObject) JSONObject.toJSON(hiveWriterDto.getDataSource());
    writerHiveDO.setDataSource(dataSource);
    writerHiveDO.setInsertType(DataSourceConstant.DataSourceInsertTypeEnum.valueOfType(hiveWriterDto.getInsertType()));
    writerHiveDO.setConf(hiveWriterDto.getConf().toJSONString());
    writerHiveDO.setPartitionList(hiveWriterDto.getPartitionList().toJSONString());
    int result = writerHiveDOMapper.insert(writerHiveDO);
    if (result != 1) {
      log.error(ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
      throw new NdiException(ProcessStatusEnum.DB_WRITE_ERROR.getCode(),
          ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
    }
    Long writerId = writerHiveDO.getId();
    String writerUrl = "";
    Long dataSourceId = getDataSourceId(dataSource);
    Map<String, Object> writerResult = handleWriterResult(writerId, "", writerUrl, dataSourceId);
    return writerResult;
  }

  @Override
  public Map updateHiveWriter(JSONObject writer, Long writerId) {
    ParamUtil.validate(writer);
    Type writerType = new TypeReference<HiveWriterDto>() {}.getType();
    HiveWriterDto hiveWriterDto = JSONObject.parseObject(writer.toString(), writerType);
    WriterHiveDO writerHiveDO = new WriterHiveDO();
    writerHiveDO.setModifyTime(new Date());
    JSONObject dataSource = (JSONObject) JSONObject.toJSON(hiveWriterDto.getDataSource());
    writerHiveDO.setDataSource(dataSource);
    writerHiveDO.setInsertType(DataSourceConstant.DataSourceInsertTypeEnum.valueOfType(hiveWriterDto.getInsertType()));
    writerHiveDO.setConf(hiveWriterDto.getConf().toJSONString());
    writerHiveDO.setPartitionList(hiveWriterDto.getPartitionList().toJSONString());
    writerHiveDO.setId(writerId);
    int result = writerHiveDOMapper.updateByPrimaryKeySelective(writerHiveDO);
    if (result != 1) {
      log.error(ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
      throw new NdiException(ProcessStatusEnum.DB_WRITE_ERROR.getCode(),
          ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
    }

    Long dataSourceId = getDataSourceId(dataSource);
    String writerUrl = "";
    Map<String, Object> writerResult = handleWriterResult(writerId, "", writerUrl, dataSourceId);
    return writerResult;
  }

  /**
   * DDB(DBI)
   *
   * dataSource
   * preSQL
   * postSQL
   * insertType
   * conf
   * @param writer
   * @return
   */
  @Override
  public Map insertDdbDbiWriter(JSONObject writer) {
    ParamUtil.validate(writer);
    DBIWriterDto dbiWriterDto = JSONObject.parseObject(writer.toString(), new TypeReference<DBIWriterDto>(){}.getType());
    JSONObject dataSource = dbiWriterDto.getDataSource();
    Integer insertType = DataSourceConstant.DataSourceInsertTypeEnum.valueOfType(dbiWriterDto.getInsertType());
    List<String> preSQL = dbiWriterDto.getPreSQL();
    List<String> postSQL = dbiWriterDto.getPostSQL();
    String conf = dbiWriterDto.getConf().toJSONString();
    WriterDdbDbiDO writerDdbDbiDO = new WriterDdbDbiDO(dataSource, insertType, JSONObject.toJSONString(preSQL),
        JSONObject.toJSONString(postSQL), conf);

    int result = writerDdbDbiDOMapper.insert(writerDdbDbiDO);
    if (result != 1) {
      log.error(ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
      throw new NdiException(ProcessStatusEnum.DB_WRITE_ERROR.getCode(),
          ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
    }
    Long dataSourceId = getDataSourceId(dataSource);
    String writerUrl = getDataSourceUrl(dataSourceId);
    Long writerId = writerDdbDbiDO.getId();
    Map<String, Object> writerResult = handleWriterResult(writerId, "", writerUrl, dataSourceId);
    return writerResult;
  }

  @Override
  public Map updateDdbDbiWriter(JSONObject writer, Long writerId) {
    ParamUtil.validate(writer);
    DBIWriterDto dbiWriterDto = JSONObject.parseObject(writer.toString(), new TypeReference<DBIWriterDto>(){}.getType());
    JSONObject dataSource = dbiWriterDto.getDataSource();
    Integer insertType = DataSourceConstant.DataSourceInsertTypeEnum.valueOfType(dbiWriterDto.getInsertType());
    List<String> preSQL = dbiWriterDto.getPreSQL();
    List<String> postSQL = dbiWriterDto.getPostSQL();
    String conf = dbiWriterDto.getConf().toJSONString();
    WriterDdbDbiDO writerDdbDbiDO = new WriterDdbDbiDO(writerId, dataSource, insertType, JSONObject.toJSONString(preSQL),
        JSONObject.toJSONString(postSQL), conf);
    int result = writerDdbDbiDOMapper.updateByPrimaryKeySelective(writerDdbDbiDO);
    if (result != 1) {
      log.error(ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
      throw new NdiException(ProcessStatusEnum.DB_WRITE_ERROR.getCode(),
          ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
    }
    Long dataSourceId = getDataSourceId(dataSource);
    String writerUrl = getDataSourceUrl(dataSourceId);
    Map<String, Object> writerResult = handleWriterResult(writerId, "", writerUrl, dataSourceId);
    return writerResult;
  }

  @Override
  public Map insertDdbQsWriter(JSONObject writer) {
    ParamUtil.validate(writer);
    QSWriterDto qsWriterDto = JSONObject.parseObject(writer.toString(), new TypeReference<QSWriterDto>(){}.getType());
    JSONObject dataSource = qsWriterDto.getDataSource();
    Integer insertType = DataSourceConstant.DataSourceInsertTypeEnum.valueOfType(qsWriterDto.getInsertType());
    String preSQL = JSONObject.toJSONString(qsWriterDto.getPreSQL());
    String postSQL = JSONObject.toJSONString(qsWriterDto.getPostSQL());
    String conf = qsWriterDto.getConf().toJSONString();
    WriterDdbQsDO writerDdbQsDO = new WriterDdbQsDO(dataSource, insertType, preSQL, postSQL, conf);
    int result = writerDdbQsDOMapper.insert(writerDdbQsDO);
    if (result != 1) {
      log.error(ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
      throw new NdiException(ProcessStatusEnum.DB_WRITE_ERROR.getCode(),
          ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
    }
    Long dataSourceId = getDataSourceId(dataSource);
    String writerUrl = getDataSourceUrl(dataSourceId);
    Long writerId = writerDdbQsDO.getId();
    Map<String, Object> writerResult = handleWriterResult(writerId, "", writerUrl, dataSourceId);
    return writerResult;
  }

  @Override
  public Map insertOracleWriter(JSONObject writer) {
    OracleWriterDto oracleWriterDto = JSONObject.parseObject(writer.toString(), new TypeReference<OracleWriterDto>(){}.getType());
    JSONObject dataSource = oracleWriterDto.getDataSource();
    Integer insertType = DataSourceConstant.DataSourceInsertTypeEnum.valueOfType(oracleWriterDto.getInsertType());
    String preSQL = JSONObject.toJSONString(oracleWriterDto.getPreSQL());
    String postSQL = JSONObject.toJSONString(oracleWriterDto.getPostSQL());
    String conf = oracleWriterDto.getConf().toJSONString();
    WriterOracleDO writerOracleDO = new WriterOracleDO(dataSource, insertType, preSQL, postSQL, conf);
    int result = writerOracleDOMapper.insert(writerOracleDO);
    if (result != 1) {
      log.error(ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
      throw new NdiException(ProcessStatusEnum.DB_WRITE_ERROR.getCode(),
          ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
    }
    Long dataSourceId = getDataSourceId(dataSource);
    String writerUrl = getDataSourceUrl(dataSourceId);
    Long writerId = writerOracleDO.getId();
    Map<String, Object> writerResult = handleWriterResult(writerId, "", writerUrl, dataSourceId);
    return writerResult;
  }

  @Override
  public Map updateOracleWriter(JSONObject writer, Long writerId) {
    ParamUtil.validate(writer);
    OracleWriterDto oracleWriterDto = JSONObject.parseObject(writer.toString(), new TypeReference<OracleWriterDto>(){}.getType());
    JSONObject dataSource = oracleWriterDto.getDataSource();
    Integer insertType = DataSourceConstant.DataSourceInsertTypeEnum.valueOfType(oracleWriterDto.getInsertType());
    String preSQL = JSONObject.toJSONString(oracleWriterDto.getPreSQL());
    String postSQL = JSONObject.toJSONString(oracleWriterDto.getPostSQL());
    String conf = oracleWriterDto.getConf().toJSONString();
    WriterOracleDO writerOracleDO = new WriterOracleDO(writerId, dataSource, insertType, preSQL, postSQL, conf);
    int result = writerOracleDOMapper.updateByPrimaryKeySelective(writerOracleDO);
    if (result != 1) {
      log.error(ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
      throw new NdiException(ProcessStatusEnum.DB_WRITE_ERROR.getCode(),
          ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
    }
    Long dataSourceId = getDataSourceId(dataSource);
    String writerUrl = getDataSourceUrl(dataSourceId);
    Map<String, Object> writerResult = handleWriterResult(writerId, "", writerUrl, dataSourceId);
    return writerResult;
  }

  @Override
  public Map updateDdbQsWriter(JSONObject writer, Long writerId) {
    ParamUtil.validate(writer);
    QSWriterDto qsWriterDto = JSONObject.parseObject(writer.toString(), new TypeReference<QSWriterDto>(){}.getType());
    JSONObject dataSource = qsWriterDto.getDataSource();
    Integer insertType = DataSourceConstant.DataSourceInsertTypeEnum.valueOfType(qsWriterDto.getInsertType());
    String preSQL = JSONObject.toJSONString(qsWriterDto.getPreSQL());
    String postSQL = JSONObject.toJSONString(qsWriterDto.getPostSQL());
    String conf = qsWriterDto.getConf().toJSONString();
    WriterDdbQsDO writerDdbQsDO = new WriterDdbQsDO(writerId, dataSource, insertType, preSQL, postSQL, conf);
    int result = writerDdbQsDOMapper.updateByPrimaryKeySelective(writerDdbQsDO);
    if (result != 1) {
      log.error(ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
      throw new NdiException(ProcessStatusEnum.DB_WRITE_ERROR.getCode(),
          ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
    }
    Long dataSourceId = getDataSourceId(dataSource);
    String writerUrl = getDataSourceUrl(dataSourceId);
    Map<String, Object> writerResult = handleWriterResult(writerId, "", writerUrl, dataSourceId);
    return writerResult;
  }

  private Map<String, Object> handleWriterResult(Long writerId, String tableName, String writerUrl, Long dataSourceId) {
    Map<String, Object> insertWriterResult = new HashMap<>();
    insertWriterResult.put(ID, writerId);
    insertWriterResult.put(TABLE_NAME, tableName);
    insertWriterResult.put(WRITER_URL, writerUrl);
    insertWriterResult.put(DATA_SOURCE_ID, dataSourceId);
    return insertWriterResult;
  }

  @Override
  public Map insertMySQLWriter(JSONObject writer) {
    ParamUtil.validate(writer);
    MySQLWriterDto mySQLWriterDto = JSONObject.parseObject(writer.toString(), new TypeReference<MySQLWriterDto>(){}.getType());
    JSONObject dataSource = mySQLWriterDto.getDataSource();
    Integer insertTypeInteger = DataSourceConstant.DataSourceInsertTypeEnum
        .valueOfType(mySQLWriterDto.getInsertType());
    List<String> preSQL = mySQLWriterDto.getPreSQL();
    List<String> postSQL = mySQLWriterDto.getPostSQL();
    JSONArray conf = mySQLWriterDto.getConf();
    WriterMySQLDO writerMySQLDO = new WriterMySQLDO(dataSource, insertTypeInteger, JSONObject.toJSONString(preSQL),
        JSONObject.toJSONString(postSQL), conf.toJSONString());
    int result = writerMySQLDOMapper.insert(writerMySQLDO);
    if (result != 1) {
      log.error(ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
      throw new NdiException(ProcessStatusEnum.DB_WRITE_ERROR.getCode(),
          ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
    }
    Long dataSourceId = getDataSourceId(dataSource);
    String writerUrl = getDataSourceUrl(dataSourceId);
    Long writerId = writerMySQLDO.getId();
    Map<String, Object> writerResult = handleWriterResult(writerId, "", writerUrl, dataSourceId);
    return writerResult;
  }

  @Override
  public Map updateMySQLWriter(JSONObject writer, Long writerId) {
    ParamUtil.validate(writer);
    MySQLWriterDto mySQLWriterDto = JSONObject.parseObject(writer.toString(), new TypeReference<MySQLWriterDto>(){}.getType());
    JSONObject dataSource = mySQLWriterDto.getDataSource();
    Integer insertTypeInteger = DataSourceConstant.DataSourceInsertTypeEnum
        .valueOfType(mySQLWriterDto.getInsertType());
    List<String> preSQL = mySQLWriterDto.getPreSQL();
    List<String> postSQL = mySQLWriterDto.getPostSQL();
    JSONArray conf = mySQLWriterDto.getConf();
    WriterMySQLDO writerMySQLDO = new WriterMySQLDO(writerId, dataSource, insertTypeInteger,
        JSONObject.toJSONString(preSQL),
        JSONObject.toJSONString(postSQL), conf.toJSONString());
    int result = writerMySQLDOMapper.updateByPrimaryKeySelective(writerMySQLDO);
    if (result != 1) {
      log.error(ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
      throw new NdiException(ProcessStatusEnum.DB_WRITE_ERROR.getCode(),
          ProcessStatusEnum.DB_WRITE_ERROR.getMessage());
    }
    Long dataSourceId = getDataSourceId(dataSource);
    String writerUrl = getDataSourceUrl(dataSourceId);
    Map<String, Object> writerResult = handleWriterResult(writerId, "", writerUrl, dataSourceId);
    return writerResult;
  }

  @Override
  public Writer getHiveWriter(Long writerId) {
    HiveWriterDto hiveWriterDto = new HiveWriterDto();
    WriterHiveDO writerHiveDO = writerHiveDOMapper.selectByPrimaryKey(writerId);
    if (writerHiveDO == null) {
      log.warn("Writer为null, WriterId: {}", writerId);
      return null;
    }
    hiveWriterDto.setInsertType(DataSourceConstant.DataSourceInsertTypeEnum.nameOfType(writerHiveDO.getInsertType()));
    hiveWriterDto.setDataSource(writerHiveDO.getDataSource());
    hiveWriterDto.setConf(JSONArray.parseArray(writerHiveDO.getConf()));
    hiveWriterDto.setPartitionList(JSONArray.parseArray(writerHiveDO.getPartitionList()));
    return hiveWriterDto;
  }

  @Override
  public Writer getMySQLWriter(Long writerId) {
    WriterMySQLDO writerMySQLDO = writerMySQLDOMapper.selectByPrimaryKey(writerId);
    if (writerMySQLDO == null) {
      log.warn("Writer为null, WriterId: {}", writerId);
      return null;
    }
    MySQLWriterDto mySQLWriterDto = new MySQLWriterDto();
    mySQLWriterDto.setPreSQL(JSONArray.parseArray(writerMySQLDO.getPreSql(), String.class));
    mySQLWriterDto.setPostSQL(JSONArray.parseArray(writerMySQLDO.getPostSql(), String.class));
    mySQLWriterDto.setInsertType(DataSourceConstant.DataSourceInsertTypeEnum.nameOfType(writerMySQLDO.getInsertType()));
    mySQLWriterDto.setDataSource(writerMySQLDO.getDataSource());
    mySQLWriterDto.setConf(JSONArray.parseArray(writerMySQLDO.getConf()));
    return mySQLWriterDto;
  }

  @Override
  public Writer getDBIWriter(Long writerId) {
    WriterDdbDbiDO writerDdbDbiDO = writerDdbDbiDOMapper.selectByPrimaryKey(writerId);
    if (writerDdbDbiDO == null) {
      log.warn("Writer为null, WriterId: {}", writerId);
      return null;
    }
    DBIWriterDto dbiWriterDto = new DBIWriterDto();
    dbiWriterDto.setPreSQL(JSONArray.parseArray(writerDdbDbiDO.getPreSql(), String.class));
    dbiWriterDto.setPostSQL(JSONArray.parseArray(writerDdbDbiDO.getPostSql(), String.class));
    dbiWriterDto.setInsertType(DataSourceConstant.DataSourceInsertTypeEnum.nameOfType(writerDdbDbiDO.getInsertType()));
    dbiWriterDto.setDataSource(writerDdbDbiDO.getDataSource());
    dbiWriterDto.setConf(JSONArray.parseArray(writerDdbDbiDO.getConf()));
    return dbiWriterDto;
  }

  @Override
  public Writer getQsWriter(Long writerId) {
    QSWriterDto qsWriterDto = new QSWriterDto();
    WriterDdbQsDO writerDdbQsDO = writerDdbQsDOMapper.selectByPrimaryKey(writerId);
    if (writerDdbQsDO == null) {
      log.warn("Writer为null, WriterId: {}", writerId);
      return null;
    }
    qsWriterDto.setPreSQL(JSONArray.parseArray(writerDdbQsDO.getPreSql(), String.class));
    qsWriterDto.setPostSQL(JSONArray.parseArray(writerDdbQsDO.getPostSql(), String.class));
    qsWriterDto.setInsertType(DataSourceConstant.DataSourceInsertTypeEnum.nameOfType(writerDdbQsDO.getInsertType()));
    qsWriterDto.setDataSource(writerDdbQsDO.getDataSource());
    qsWriterDto.setConf(JSONArray.parseArray(writerDdbQsDO.getConf()));
    return qsWriterDto;
  }

  @Override
  public Writer getOracleWriter(Long writerId) {
    OracleWriterDto oracleWriterDto = new OracleWriterDto();
    WriterOracleDO writerOracleDO = writerOracleDOMapper.selectByPrimaryKey(writerId);
    if (writerOracleDO == null) {
      log.warn("Writer为null, WriterId: {}", writerId);
      return null;
    }
    oracleWriterDto.setPreSQL(JSONArray.parseArray(writerOracleDO.getPreSql(), String.class));
    oracleWriterDto.setPostSQL(JSONArray.parseArray(writerOracleDO.getPostSql(), String.class));
    oracleWriterDto.setInsertType(DataSourceConstant.DataSourceInsertTypeEnum.nameOfType(writerOracleDO.getInsertType()));
    oracleWriterDto.setDataSource(writerOracleDO.getDataSource());
    oracleWriterDto.setConf(JSONArray.parseArray(writerOracleDO.getConf()));
    return oracleWriterDto;
  }

  private Long getDataSourceId(JSONObject dataSource){
    ParamUtil.validate(dataSource);
    Long dataSourceId = dataSource.getLong("dataSourceId");
    if (dataSourceId == null){
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(),
          "The dataSource don't exist id. DataSource: " + dataSource.toString());
    }
    return dataSourceId;
  }

  private String getDataSourceUrl(Long dataSourceId){
    return dataSourceService.getDataSourceUrl(dataSourceId);
  }

  @Override
  public Long insertOnlineWriter(Byte writerType, Long writerId) {
    if (DataSourceTypeEnum.MySQL.equalWith(writerType)) {
      WriterMySQLDO writerMySQLDO = writerMySQLDOMapper.selectByPrimaryKey(writerId);
      writerMySQLDO.setId(null);
      writerMySQLDOMapper.insert(writerMySQLDO);
      return writerMySQLDO.getId();
    } else if (DataSourceTypeEnum.HIVE.equalWith(writerType)) {
      WriterHiveDO writerHiveDO = writerHiveDOMapper.selectByPrimaryKey(writerId);
      writerHiveDO.setId(null);
      writerHiveDOMapper.insert(writerHiveDO);
      return writerHiveDO.getId();
    } else if (DataSourceTypeEnum.DDB.equalWith(writerType)) {
      WriterDdbDbiDO writerDdbDbiDO = writerDdbDbiDOMapper.selectByPrimaryKey(writerId);
      writerDdbDbiDO.setId(null);
      writerDdbDbiDOMapper.insert(writerDdbDbiDO);
      return writerDdbDbiDO.getId();
    } else if (DataSourceTypeEnum.DDBQS.equalWith(writerType)) {
      WriterDdbQsDO writerDdbQsDO = writerDdbQsDOMapper.selectByPrimaryKey(writerId);
      writerDdbQsDO.setId(null);
      writerDdbQsDOMapper.insert(writerDdbQsDO);
      return writerDdbQsDO.getId();
    } else if (DataSourceTypeEnum.ORACLE.equalWith(writerType)) {
      WriterOracleDO writerOracleDO = writerOracleDOMapper.selectByPrimaryKey(writerId);
      writerOracleDO.setId(null);
      writerOracleDOMapper.insert(writerOracleDO);
      return writerOracleDO.getId();
    }
    throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "不支持的数据源类型");
  }

  @Override
  public Boolean deleteMySQLWriter(Long writerId) {
    return writerMySQLDOMapper.deleteByPrimaryKey(writerId) != 0;
  }

  @Override
  public Boolean deleteDBIWriter(Long writerId) {
    return writerDdbDbiDOMapper.deleteByPrimaryKey(writerId) != 0;
  }

  @Override
  public Boolean deleteQSWriter(Long writerId) {
    return writerDdbQsDOMapper.deleteByPrimaryKey(writerId) != 0;
  }

  @Override
  public Boolean deleteHiveWriter(Long writerId) {
    return writerHiveDOMapper.deleteByPrimaryKey(writerId) != 0;
  }

  @Override
  public Boolean deleteOracleWriter(Long writerId) {
    return writerOracleDOMapper.deleteByPrimaryKey(writerId) != 0;
  }

  @Override
  public WriterMySQLDO getWriterMySQLDO(Long writerId) {
    return writerMySQLDOMapper.selectByPrimaryKey(writerId);
  }

  @Override
  public WriterDdbDbiDO getWriterDBIDO(Long writerId) {
    return writerDdbDbiDOMapper.selectByPrimaryKey(writerId);
  }

  @Override
  public WriterDdbQsDO getWriterQSDO(Long writerId) {
    return writerDdbQsDOMapper.selectByPrimaryKey(writerId);
  }

  @Override
  public WriterHiveDO getWriterHiveDO(Long writerId) {
    return writerHiveDOMapper.selectByPrimaryKey(writerId);
  }

  @Override
  public WriterOracleDO getWriterOracleDO(Long writerId) {
    return writerOracleDOMapper.selectByPrimaryKey(writerId);
  }

  @Override
  public JSONObject getWriterWithConnectionInfo(User user, Byte writerType, Long writerId) {
    JSONObject writer = new JSONObject();
    if (DataSourceTypeEnum.HIVE.equalWith(writerType)) {
      WriterHiveDO writerHiveDO = getWriterHiveDO(writerId);
      JSONObject dataSource = writerHiveDO.getDataSource();
      JSONObject connectionInfo = getConnectionInfo(user, dataSource);
      dataSource.put("connectionInformation", connectionInfo);
      writer.put("dataSource", dataSource);
      writer.put("partition", JSONArray.parseArray(writerHiveDO.getPartitionList()));
      writer.put("conf", JSONArray.parseArray(writerHiveDO.getConf()));
      writer.put("insertType", DataSourceConstant.DataSourceInsertTypeEnum.nameOfType(writerHiveDO.getInsertType()));
    } else if (DataSourceTypeEnum.MySQL.equalWith(writerType)) {
      WriterMySQLDO writerMySQLDO = getWriterMySQLDO(writerId);
      JSONObject writerDataSource = writerMySQLDO.getDataSource();
      JSONObject connectionInfo = getConnectionInfo(user, writerDataSource);
      writerDataSource.put("connectionInformation", connectionInfo);
      writer.put("dataSource", writerDataSource);
      writer.put("preSQL", JSONArray.parseArray(writerMySQLDO.getPreSql()));
      writer.put("postSQL", JSONArray.parseArray(writerMySQLDO.getPostSql()));
      writer.put("insertType", DataSourceConstant.DataSourceInsertTypeEnum.nameOfType(writerMySQLDO.getInsertType()));
      writer.put("conf", JSONArray.parseArray(writerMySQLDO.getConf()));
    } else if (DataSourceTypeEnum.DDB.equalWith(writerType)) {
      WriterDdbDbiDO writerDdbDbiDO = getWriterDBIDO(writerId);
      JSONObject writerDataSource = writerDdbDbiDO.getDataSource();
      JSONObject connectionInfo = getConnectionInfo(user, writerDataSource);
      writerDataSource.put("connectionInformation", connectionInfo);
      writer.put("dataSource", writerDataSource);
      writer.put("preSQL", JSONArray.parseArray(writerDdbDbiDO.getPreSql()));
      writer.put("postSQL", JSONArray.parseArray(writerDdbDbiDO.getPostSql()));
      writer.put("insertType", DataSourceConstant.DataSourceInsertTypeEnum.nameOfType(writerDdbDbiDO.getInsertType()));
      writer.put("conf", JSONArray.parseArray(writerDdbDbiDO.getConf()));
    } else if (DataSourceTypeEnum.DDBQS.equalWith(writerType)) {
      WriterDdbQsDO writerDdbQsDO = getWriterQSDO(writerId);
      JSONObject writerDataSource = writerDdbQsDO.getDataSource();
      JSONObject connectionInfo = getConnectionInfo(user, writerDataSource);
      writerDataSource.put("connectionInformation", connectionInfo);
      writer.put("dataSource", writerDataSource);
      writer.put("preSQL", JSONArray.parseArray(writerDdbQsDO.getPreSql()));
      writer.put("postSQL", JSONArray.parseArray(writerDdbQsDO.getPostSql()));
      writer.put("insertType", DataSourceConstant.DataSourceInsertTypeEnum.nameOfType(writerDdbQsDO.getInsertType()));
      writer.put("conf", JSONArray.parseArray(writerDdbQsDO.getConf()));
    } else if (DataSourceTypeEnum.ORACLE.equalWith(writerType)) {
      WriterOracleDO writerOracleDO = getWriterOracleDO(writerId);
      JSONObject writerDataSource = writerOracleDO.getDataSource();
      JSONObject connectionInfo = getConnectionInfo(user, writerDataSource);
      writerDataSource.put("connectionInformation", connectionInfo);
      writer.put("dataSource", writerDataSource);
      writer.put("preSQL", JSONArray.parseArray(writerOracleDO.getPreSql()));
      writer.put("postSQL", JSONArray.parseArray(writerOracleDO.getPostSql()));
      writer.put("insertType", DataSourceConstant.DataSourceInsertTypeEnum.nameOfType(writerOracleDO.getInsertType()));
      writer.put("conf", JSONArray.parseArray(writerOracleDO.getConf()));
    }
    return writer;
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
