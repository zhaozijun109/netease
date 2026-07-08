package com.netease.bdms.ndi.service.web.vo;

import com.netease.bdms.ndi.service.web.dto.datasource.CreateDataSourceDto;
import com.netease.bdms.ndi.service.web.dto.datasource.DataSourceDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ModifyDataSourceDto;
import com.netease.bdms.ndi.service.web.util.ParamUtil;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
public class DataSourceVO {
  private Long id;
  private String name;
  private String owner;
  private String createTime;
  private String type;
  private Long modifyTime;
  private String modifier;
  private Object connectionInformation;

  public static DataSourceVO dataSourceDTO2VO(DataSourceDto dataSourceDTO) {
    ParamUtil.validate(dataSourceDTO);
    DataSourceVO dataSourceVO = new DataSourceVO();
    BeanUtils.copyProperties(dataSourceDTO, dataSourceVO);
    return dataSourceVO;
  }

  public static DataSourceVO createDataSourceDTO2VO(CreateDataSourceDto createDataSourceDTO) {
    ParamUtil.validate(createDataSourceDTO, createDataSourceDTO.getDataSourceDto());
    DataSourceDto dataSourceDTO = createDataSourceDTO.getDataSourceDto();
    DataSourceVO dataSourceVO = new DataSourceVO();
    BeanUtils.copyProperties(dataSourceDTO, dataSourceVO);
    return dataSourceVO;
  }

  public static DataSourceVO modifyDataSourceDTO2VO(ModifyDataSourceDto modifyDataSourceDTO) {
    ParamUtil.validate(modifyDataSourceDTO, modifyDataSourceDTO.getDataSourceDto());
    DataSourceDto dataSourceDTO = modifyDataSourceDTO.getDataSourceDto();
    DataSourceVO dataSourceVO = dataSourceDTO2VO(dataSourceDTO);
    return dataSourceVO;
  }

}
