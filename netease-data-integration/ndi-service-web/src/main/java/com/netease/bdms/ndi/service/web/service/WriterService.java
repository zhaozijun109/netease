package com.netease.bdms.ndi.service.web.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.netease.bdms.ndi.service.web.exception.NdiException;
import com.netease.bdms.ndi.service.web.param.User;
import com.netease.bdms.ndi.service.web.param.task.OracleWriterDto;
import com.netease.bdms.ndi.service.web.param.task.Writer;
import com.netease.bdms.ndi.service.web.pojo.*;
import com.netease.bdms.ndi.service.web.util.DataSourceConstant;
import com.netease.bdms.ndi.service.web.util.ParamUtil;
import com.netease.bdms.ndi.service.web.util.ProcessStatusEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName WriterService
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
public interface WriterService {

  Map insertHiveWriter(JSONObject jsonObject);

  Map updateHiveWriter(JSONObject writer, Long writerId);

  Map insertDdbDbiWriter(JSONObject jsonObject);

  Map updateDdbDbiWriter(JSONObject writer, Long writerId);

  Map insertDdbQsWriter(JSONObject jsonObject);

  Map updateDdbQsWriter(JSONObject writer, Long writerId);

  Map insertMySQLWriter(JSONObject jsonObject);

  Map updateMySQLWriter(JSONObject writer, Long writerId);

  Map insertOracleWriter(JSONObject writer);

  Map updateOracleWriter(JSONObject writer, Long writerId);

  Writer getOracleWriter(Long writerId);

  Writer getHiveWriter(Long writerId);

  Writer getMySQLWriter(Long writerId);

  Writer getDBIWriter(Long writerId);

  Writer getQsWriter(Long writerId);

  Long insertOnlineWriter(Byte writerType, Long writerId);

  Boolean deleteMySQLWriter(Long writerId);

  Boolean deleteDBIWriter(Long writerId);

  Boolean deleteQSWriter(Long writerId);

  Boolean deleteHiveWriter(Long writerId);

  Boolean deleteOracleWriter(Long writerId);

  WriterMySQLDO getWriterMySQLDO(Long writerId);

  WriterDdbDbiDO getWriterDBIDO(Long writerId);

  WriterDdbQsDO getWriterQSDO(Long writerId);

  WriterHiveDO getWriterHiveDO(Long writerId);

  WriterOracleDO getWriterOracleDO(Long writerId);

  JSONObject getWriterWithConnectionInfo(User user, Byte writerType, Long writerId);
}
