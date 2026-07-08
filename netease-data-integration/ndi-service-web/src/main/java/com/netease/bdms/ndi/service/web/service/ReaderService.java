package com.netease.bdms.ndi.service.web.service;

import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.param.User;
import com.netease.bdms.ndi.service.web.param.task.Reader;
import com.netease.bdms.ndi.service.web.pojo.ReaderDdbDbiDO;
import com.netease.bdms.ndi.service.web.pojo.ReaderDdbQsDO;
import com.netease.bdms.ndi.service.web.pojo.ReaderHiveDO;
import com.netease.bdms.ndi.service.web.pojo.ReaderMySQLDO;
import com.netease.bdms.ndi.service.web.pojo.ReaderOracleDO;

import java.util.Map;

/**
 * @ClassName ReaderService
 * @Description Reader 服务
 * @Author Min Zhao
 * @Version 1.0
 **/
public interface ReaderService {

  Map insertHiveReader(JSONObject jsonObject);

  Map updateHiveReader(JSONObject jsonObject, Long readerId);

  Map insertDdbDbiReader(JSONObject jsonObject);

  Map updateDdbDbiReader(JSONObject jsonObject, Long readerId);

  Map insertDdbQsReader(JSONObject jsonObject);

  Map updateDdbQsReader(JSONObject jsonObject, Long readerId);

  Map insertMySQLReader(JSONObject jsonObject);

  Map updateMySQLReader(JSONObject jsonObject, Long readerId);

  Map insertOracleReader(JSONObject jsonObject);

  Map updateOracleReader(JSONObject jsonObject, Long readerId);

  Reader getOracleReader(Long readerId);

  Reader getMySQLReader(Long readerId);

  Reader getHiveReader(Long readerId);

  Reader getDBIReader(Long readerId);

  Reader getQSReader(Long readerId);

  Long insertOnlineReader(Byte readerType, Long readerId);

  ReaderOracleDO gerReaderOracleDO(Long readerId);

  ReaderMySQLDO getReaderMySQLDO(Long readerId);

  ReaderDdbDbiDO getReaderDBIDO(Long readerId);

  ReaderDdbQsDO getReaderQSDO(Long readerId);

  ReaderHiveDO getReaderHiveDO(Long readerId);

  Boolean deleteReaderOracleDO(Long readerId);

  Boolean deleteReaderMySQLDO(Long readerId);

  Boolean deleteReaderDBIDO(Long readerId);

  Boolean deleteReaderQSDO(Long readerId);

  Boolean deleteReaderHiveDO(Long readerId);

  JSONObject getReaderWithConnectionInfo(User user, Byte readerType, Long readerId);

}
